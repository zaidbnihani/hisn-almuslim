package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AthkarData

class AdhanForegroundService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var assetFileDescriptor: android.content.res.AssetFileDescriptor? = null
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        const val ACTION_PLAY_ADHAN = "com.example.action.PLAY_ADHAN"
        const val ACTION_STOP_ADHAN = "com.example.action.STOP_ADHAN"
        const val ACTION_ADHAN_STOPPED = "com.example.action.ADHAN_STOPPED"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_MUEZZIN_ID = "extra_muezzin_id"
        private const val CHANNEL_ID = "adhan_foreground_channel"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP_ADHAN) {
            stopAdhanAndSelf()
            return START_NOT_STICKY
        }

        val prayerName = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: "الصلاة"
        val muezzinId = intent?.getStringExtra(EXTRA_MUEZZIN_ID) ?: "minshawi"
        var prayerKey = intent?.getStringExtra("extra_prayer_key") ?: ""

        if (prayerKey.isEmpty()) {
            prayerKey = when (prayerName) {
                "الفجر" -> "fajr"
                "الشروق" -> "sunrise"
                "الظهر" -> "dhuhr"
                "العصر" -> "asr"
                "المغرب" -> "maghrib"
                "العشاء" -> "isha"
                else -> ""
            }
        }

        acquireWakeLock()
        createNotificationChannel()

        val stopIntent = Intent(this, AdhanForegroundService::class.java).apply {
            this.action = ACTION_STOP_ADHAN
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openAppIntent = Intent(this, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
            .setContentTitle("حان الآن موعد أذان $prayerName")
            .setContentText("الله أكبر الله أكبر - اضغط لإيقاف الأذان")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "إيقاف الأذان", stopPendingIntent)
            .setSound(null)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        val prefs = getSharedPreferences("athkar_user_data", Context.MODE_PRIVATE)
        val isMuted = if (prayerKey.isNotEmpty()) prefs.getBoolean("prayer_muted_$prayerKey", false) else false

        if (!isMuted) {
            playAdhanAudio(muezzinId)
        }

        return START_NOT_STICKY
    }

    private fun playAdhanAudio(muezzinId: String) {
        stopAudioOnly()

        val muezzin = AthkarData.muezzinsList.find { it.id == muezzinId } ?: AthkarData.muezzinsList.first()

        try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setVolume(1.0f, 1.0f)
                
                if (muezzin.audioUrl.startsWith("asset:")) {
                    val path = muezzin.audioUrl.removePrefix("asset:")
                    val afd = assets.openFd(path)
                    assetFileDescriptor = afd
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                } else {
                    setDataSource(this@AdhanForegroundService, android.net.Uri.parse(muezzin.audioUrl))
                }
                setOnCompletionListener {
                    stopAdhanAndSelf()
                }
                setOnErrorListener { _, what, extra ->
                    android.util.Log.e("AdhanService", "MediaPlayer Error what: $what extra: $extra")
                    stopAdhanAndSelf()
                    true
                }
                setOnPreparedListener { it.start() }
                prepareAsync()
            }
            mediaPlayer = mp
        } catch (e: Exception) {
            android.util.Log.e("AdhanService", "playAdhanAudio Exception: ${e.message}", e)
            stopAdhanAndSelf()
        }
    }

    private fun stopAudioOnly() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        try {
            assetFileDescriptor?.close()
        } catch (_: Exception) {}
        mediaPlayer = null
        assetFileDescriptor = null
    }

    private fun cleanupAdhanResources() {
        stopAudioOnly()
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun stopAdhanAndSelf() {
        cleanupAdhanResources()

        // Broadcast to MainActivity that Adhan stopped
        val stoppedIntent = Intent(ACTION_ADHAN_STOPPED).apply {
            setPackage(packageName)
        }
        sendBroadcast(stoppedIntent)

        stopSelf()
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Athkar:AdhanWakeLock")
            wakeLock?.acquire(5 * 60 * 1000L) // 5 minutes max
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
        wakeLock = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "تنبيهات الأذان والتذكير",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تشغيل الأذان عند دخول وقت الصلاة"
                setSound(null, null)
                enableVibration(true)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        cleanupAdhanResources()
        super.onDestroy()
    }
}
