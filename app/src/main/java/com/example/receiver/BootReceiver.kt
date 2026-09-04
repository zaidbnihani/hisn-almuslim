package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.service.AdhanAlarmScheduler

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val exactAllowed = AdhanAlarmScheduler.scheduleAllAlarms(context)
            if (!exactAllowed) {
                android.util.Log.w("BootReceiver", "Scheduled alarms inexactly because SCHEDULE_EXACT_ALARM permission is missing.")
            } else {
                android.util.Log.i("BootReceiver", "Scheduled exact alarms successfully after boot.")
            }
        }
    }
}
