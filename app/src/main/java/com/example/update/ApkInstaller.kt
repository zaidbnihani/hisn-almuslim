package com.example.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(val progress: Int) : DownloadState()
    data class Downloaded(val file: File) : DownloadState()
    data class NeedPermission(val file: File) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

object ApkInstaller {

    fun canInstallPackages(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun openInstallUnknownAppsSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        }
    }

    fun installApk(context: Context, apkFile: File): Boolean {
        if (!apkFile.exists()) return false

        return try {
            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun downloadApkFlow(context: Context, url: String, fileName: String = "app-update.apk"): Flow<DownloadState> = flow {
        emit(DownloadState.Downloading(0))

        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: context.filesDir

        val destinationFile = File(downloadsDir, fileName)
        if (destinationFile.exists()) {
            destinationFile.delete()
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        if (downloadManager == null) {
            emit(DownloadState.Error("خدمة التحميل غير متوفرة"))
            return@flow
        }

        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle("تحديث التطبيق")
            setDescription("جاري تنزيل ملف التحديث...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationUri(Uri.fromFile(destinationFile))
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }

        val downloadId = try {
            downloadManager.enqueue(request)
        } catch (e: Exception) {
            emit(DownloadState.Error("فشل بدء التحميل: ${e.localizedMessage}"))
            return@flow
        }

        var downloading = true
        while (downloading) {
            delay(400)
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)

            if (cursor != null && cursor.moveToFirst()) {
                val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val status = cursor.getInt(statusIndex)

                val bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                val bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

                val bytesDownloaded = if (bytesDownloadedIndex != -1) cursor.getLong(bytesDownloadedIndex) else 0L
                val bytesTotal = if (bytesTotalIndex != -1) cursor.getLong(bytesTotalIndex) else -1L

                val progress = if (bytesTotal > 0) {
                    ((bytesDownloaded * 100) / bytesTotal).toInt().coerceIn(0, 99)
                } else {
                    0
                }

                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        downloading = false
                        cursor.close()
                        emit(DownloadState.Downloading(100))

                        if (canInstallPackages(context)) {
                            emit(DownloadState.Downloaded(destinationFile))
                            installApk(context, destinationFile)
                        } else {
                            emit(DownloadState.NeedPermission(destinationFile))
                        }
                        return@flow
                    }

                    DownloadManager.STATUS_FAILED -> {
                        downloading = false
                        cursor.close()
                        emit(DownloadState.Error("فشل تحميل ملف التحديث"))
                        return@flow
                    }

                    else -> {
                        emit(DownloadState.Downloading(progress))
                    }
                }
                cursor.close()
            }
        }
    }.flowOn(Dispatchers.IO)
}
