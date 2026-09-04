package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.AthkarData
import com.example.data.PrayerCalculator
import com.example.receiver.AdhanAlarmReceiver
import java.util.Calendar

object AdhanAlarmScheduler {

    fun scheduleAllAlarms(context: Context): Boolean {
        val prefs = context.getSharedPreferences("athkar_user_data", Context.MODE_PRIVATE)
        val soundEnabled = prefs.getBoolean("sound_enabled", true)
        val notifsEnabled = prefs.getBoolean("notifs_enabled", true)
        if (!soundEnabled || !notifsEnabled) {
            cancelAllAlarms(context)
            return true
        }

        val cityKey = prefs.getString("selected_city", "mecca") ?: "mecca"
        val methodKey = prefs.getString("selected_method", "auto") ?: "auto"
        val muezzinId = prefs.getString("selected_muezzin", "minshawi") ?: "minshawi"

        val customLatStr = prefs.getString("custom_lat", null)
        val customLonStr = prefs.getString("custom_lon", null)

        val cityPreset = AthkarData.presetCities.find { it.key == cityKey } ?: AthkarData.presetCities.first()
        val lat = customLatStr?.toDoubleOrNull() ?: cityPreset.lat
        val lon = customLonStr?.toDoubleOrNull() ?: cityPreset.lon
        val tz = if (customLatStr != null) {
            java.util.TimeZone.getDefault().getOffset(System.currentTimeMillis()) / (1000.0 * 60 * 60)
        } else {
            cityPreset.tz
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        var exactAlarmsAllowed = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            exactAlarmsAllowed = alarmManager.canScheduleExactAlarms()
        }

        val now = System.currentTimeMillis()

        for (dayOffset in 0..1) {
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }
            
            val times = PrayerCalculator.calculatePrayerTimes(cal.time, lat, lon, tz, methodKey)

            val prayerList = listOf(
                "fajr" to ("الفجر" to times.fajr),
                "dhuhr" to ("الظهر" to times.dhuhr),
                "asr" to ("العصر" to times.asr),
                "maghrib" to ("المغرب" to times.maghrib),
                "isha" to ("العشاء" to times.isha)
            )

            for ((key, pair) in prayerList) {
                val name = pair.first
                val timeStr = pair.second
                val timeMs = getPrayerTimeMs(cal, timeStr, tz) ?: continue

                if (timeMs > now) {
                    val requestCode = (key + "_" + dayOffset).hashCode()
                    val intent = Intent(context, AdhanAlarmReceiver::class.java).apply {
                        action = AdhanAlarmReceiver.ACTION_ALARM_FIRE
                        putExtra(AdhanAlarmReceiver.EXTRA_PRAYER_NAME, name)
                        putExtra(AdhanAlarmReceiver.EXTRA_MUEZZIN_ID, muezzinId)
                        putExtra("extra_prayer_key", key)
                    }

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    try {
                        val showIntent = Intent(context, com.example.MainActivity::class.java)
                        val showPendingIntent = PendingIntent.getActivity(context, requestCode, showIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                        
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (exactAlarmsAllowed) {
                                val alarmClockInfo = AlarmManager.AlarmClockInfo(timeMs, showPendingIntent)
                                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                            } else {
                                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent)
                            }
                        } else {
                            val alarmClockInfo = AlarmManager.AlarmClockInfo(timeMs, showPendingIntent)
                            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        exactAlarmsAllowed = false
                    }
                }
            }
        }
        
        return exactAlarmsAllowed
    }

    private fun getPrayerTimeMs(dayCal: Calendar, timeStr: String, cityTz: Double): Long? {
        val parts = timeStr.trim().split(" ")
        if (parts.size < 2) return null
        val timeParts = parts[0].split(":")
        if (timeParts.size < 2) return null
        var h = timeParts[0].toIntOrNull() ?: return null
        val m = timeParts[1].toIntOrNull() ?: return null
        val period = parts[1]
        if (period == "م" && h != 12) h += 12
        if (period == "ص" && h == 12) h = 0

        val tzSign = if (cityTz >= 0) "+" else "-"
        val tzHours = Math.abs(cityTz).toInt()
        val tzMinutes = ((Math.abs(cityTz) - tzHours) * 60).toInt()
        val tzId = String.format("GMT%s%d:%02d", tzSign, tzHours, tzMinutes)
        val tz = java.util.TimeZone.getTimeZone(tzId)

        val cal = Calendar.getInstance(tz).apply {
            set(Calendar.YEAR, dayCal.get(Calendar.YEAR))
            set(Calendar.MONTH, dayCal.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, dayCal.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun cancelAllAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val keys = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
        for (dayOffset in 0..1) {
            for (key in keys) {
                val requestCode = (key + "_" + dayOffset).hashCode()
                val intent = Intent(context, AdhanAlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
            }
        }
    }
}
