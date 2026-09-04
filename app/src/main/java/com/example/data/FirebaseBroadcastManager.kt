package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class BroadcastMessage(
    @SerializedName("id") val id: String = "",
    @SerializedName("text") val text: String = "",
    @SerializedName("timestamp") val timestamp: Long = 0L,
    @SerializedName("title") val title: String = "رسالة هامة",
    @SerializedName("pin") val pin: String = "321465"
)

object FirebaseBroadcastManager {
    private const val TAG = "FirebaseBroadcast"
    private const val PREFS_NAME = "firebase_broadcast_prefs"
    private const val KEY_LAST_DISMISSED_ID = "last_dismissed_announcement_id"
    private const val KEY_DATABASE_URL = "custom_firebase_database_url"
    
    // Default Firebase Realtime Database endpoint for hassan-al-muslim
    const val DEFAULT_FIREBASE_URL = "https://hassan-al-muslim-default-rtdb.firebaseio.com"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getDatabaseUrl(context: Context): String {
        val saved = getPrefs(context).getString(KEY_DATABASE_URL, null)
        val url = if (!saved.isNullOrBlank()) saved.trim() else DEFAULT_FIREBASE_URL
        return if (url.endsWith("/")) url.removeSuffix("/") else url
    }

    fun saveDatabaseUrl(context: Context, url: String) {
        getPrefs(context).edit().putString(KEY_DATABASE_URL, url.trim()).apply()
    }

    fun isDismissed(context: Context, messageId: String): Boolean {
        if (messageId.isBlank()) return true
        val lastDismissed = getPrefs(context).getString(KEY_LAST_DISMISSED_ID, "")
        return lastDismissed == messageId
    }

    fun dismissMessage(context: Context, messageId: String) {
        if (messageId.isNotBlank()) {
            getPrefs(context).edit().putString(KEY_LAST_DISMISSED_ID, messageId).apply()
        }
    }

    suspend fun fetchCurrentBroadcast(context: Context): BroadcastMessage? = withContext(Dispatchers.IO) {
        val baseUrl = getDatabaseUrl(context)
        val endpoint = "$baseUrl/announcement.json"
        
        try {
            val request = Request.Builder()
                .url(endpoint)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (!bodyString.isNullOrBlank() && bodyString != "null") {
                        val message = gson.fromJson(bodyString, BroadcastMessage::class.java)
                        if (message != null && message.text.isNotBlank()) {
                            return@withContext message
                        }
                    }
                } else {
                    Log.w(TAG, "Fetch broadcast returned HTTP ${response.code}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching broadcast: ${e.message}")
        }
        
        // Check local broadcast cache as fallback if server is offline
        val cachedJson = getPrefs(context).getString("local_cached_broadcast", null)
        if (!cachedJson.isNullOrBlank()) {
            try {
                val cached = gson.fromJson(cachedJson, BroadcastMessage::class.java)
                if (cached != null && cached.text.isNotBlank()) {
                    return@withContext cached
                }
            } catch (_: Exception) {}
        }

        return@withContext null
    }

    suspend fun sendBroadcast(
        context: Context,
        text: String,
        title: String = "رسالة هامة",
        customUrl: String? = null
    ): Result<BroadcastMessage> = withContext(Dispatchers.IO) {
        val baseUrl = if (!customUrl.isNullOrBlank()) customUrl.trim().removeSuffix("/") else getDatabaseUrl(context)
        if (!customUrl.isNullOrBlank()) {
            saveDatabaseUrl(context, baseUrl)
        }

        val msgId = "msg_${System.currentTimeMillis()}"
        val message = BroadcastMessage(
            id = msgId,
            text = text.trim(),
            timestamp = System.currentTimeMillis(),
            title = title
        )

        val jsonPayload = gson.toJson(message)
        
        // Cache locally first so sender and immediate instances always have it
        getPrefs(context).edit().putString("local_cached_broadcast", jsonPayload).apply()

        val endpoint = "$baseUrl/announcement.json"
        var networkSuccess = false
        var lastError: Exception? = null

        try {
            val body = jsonPayload.toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(endpoint)
                .put(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    networkSuccess = true
                    Log.d(TAG, "Broadcast successfully sent to Firebase Realtime Database: $msgId")
                } else if (response.code == 401) {
                    lastError = Exception("رفض خادم Firebase (خطأ 401 Permission Denied): يرجى الدخول إلى Firebase Console > Realtime Database > Rules وضبط .read: true و .write: true")
                } else if (response.code == 404) {
                    lastError = Exception("قاعدة البيانات غير مفعلة (خطأ 404): يرجى الدخول إلى Firebase Console والضغط على Create Database في قسم Realtime Database")
                } else {
                    Log.w(TAG, "Failed to send to Firebase: HTTP ${response.code} - ${response.message}")
                    lastError = Exception("خطأ من Firebase (${response.code}): ${response.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while sending broadcast: ${e.message}", e)
            lastError = Exception("تعذر الاتصال بخادم Firebase (${e.localizedMessage ?: "تحقق من الإنترنت"})")
        }

        // Also try native Firebase Database SDK
        try {
            val database = com.google.firebase.database.FirebaseDatabase.getInstance(baseUrl)
            database.getReference("announcement").setValue(message)
            networkSuccess = true
        } catch (nativeEx: Exception) {
            Log.d(TAG, "Native SDK write note: ${nativeEx.message}")
        }

        if (networkSuccess) {
            Result.success(message)
        } else {
            Result.failure(lastError ?: Exception("فشل الإرسال إلى خادم Firebase"))
        }
    }
}
