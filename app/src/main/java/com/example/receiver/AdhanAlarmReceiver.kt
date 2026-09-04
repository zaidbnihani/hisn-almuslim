package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.service.AdhanAlarmScheduler
import com.example.service.AdhanForegroundService

class AdhanAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_ALARM_FIRE = "com.example.action.ALARM_FIRE"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_MUEZZIN_ID = "extra_muezzin_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "الصلاة"
        val muezzinId = intent.getStringExtra(EXTRA_MUEZZIN_ID) ?: "minshawi"
        val prayerKey = intent.getStringExtra("extra_prayer_key") ?: ""

        val serviceIntent = Intent(context, AdhanForegroundService::class.java).apply {
            action = AdhanForegroundService.ACTION_PLAY_ADHAN
            putExtra(AdhanForegroundService.EXTRA_PRAYER_NAME, prayerName)
            putExtra(AdhanForegroundService.EXTRA_MUEZZIN_ID, muezzinId)
            putExtra("extra_prayer_key", prayerKey)
        }

        try {
            ContextCompat.startForegroundService(context, serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel("adhan_fallback_channel", "تنبيه الأذان", NotificationManager.IMPORTANCE_HIGH)
                nm.createNotificationChannel(channel)
            }
            val notification = NotificationCompat.Builder(context, "adhan_fallback_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("حان الآن موعد صلاة $prayerName")
                .setContentText("تعذّر تشغيل الأذان تلقائياً - افتح التطبيق")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
            nm.notify(9999, notification)
        }

        // Reschedule alarms for upcoming days
        AdhanAlarmScheduler.scheduleAllAlarms(context)
    }
}
