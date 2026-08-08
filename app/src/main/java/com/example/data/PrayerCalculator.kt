package com.example.data

import java.util.Calendar
import java.util.Date
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

object PrayerCalculator {

    fun calculatePrayerTimes(
        date: Date = Date(),
        lat: Double,
        lon: Double,
        timezone: Double,
        methodKey: String = "umm_alqura"
    ): PrayerTimes {
        val coordinates = com.batoulapps.adhan.Coordinates(lat, lon)
        
        val cal = Calendar.getInstance().apply { time = date }
        val dateComponents = com.batoulapps.adhan.data.DateComponents(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        
        val method = when (methodKey) {
            "egypt" -> com.batoulapps.adhan.CalculationMethod.EGYPTIAN
            "mwl" -> com.batoulapps.adhan.CalculationMethod.MUSLIM_WORLD_LEAGUE
            "isna" -> com.batoulapps.adhan.CalculationMethod.NORTH_AMERICA
            "karachi" -> com.batoulapps.adhan.CalculationMethod.KARACHI
            else -> com.batoulapps.adhan.CalculationMethod.UMM_AL_QURA
        }
        
        val params = method.parameters
        
        val adhanTimes = com.batoulapps.adhan.PrayerTimes(coordinates, dateComponents, params)
        
        val tzSign = if (timezone >= 0) "+" else "-"
        val tzHours = Math.abs(timezone).toInt()
        val tzMinutes = ((Math.abs(timezone) - tzHours) * 60).toInt()
        val tzId = String.format("GMT%s%d:%02d", tzSign, tzHours, tzMinutes)
        val tz = java.util.TimeZone.getTimeZone(tzId)
        
        fun formatDateToLocalString(d: Date?): String {
            if (d == null) return "--:--"
            val c = Calendar.getInstance(tz)
            c.time = d
            val hours24 = c.get(Calendar.HOUR_OF_DAY)
            val mins = c.get(Calendar.MINUTE)
            val period = if (hours24 >= 12) "م" else "ص"
            val displayHours = if (hours24 % 12 == 0) 12 else hours24 % 12
            return String.format(java.util.Locale.US, "%02d:%02d %s", displayHours, mins, period)
        }
        
        return PrayerTimes(
            fajr = formatDateToLocalString(adhanTimes.fajr),
            sunrise = formatDateToLocalString(adhanTimes.sunrise),
            dhuhr = formatDateToLocalString(adhanTimes.dhuhr),
            asr = formatDateToLocalString(adhanTimes.asr),
            maghrib = formatDateToLocalString(adhanTimes.maghrib),
            isha = formatDateToLocalString(adhanTimes.isha)
        )
    }

    fun getHijriDateString(date: Date = Date()): String {
        val cal = Calendar.getInstance().apply { time = date }
        return getAstronomicalHijri(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.DAY_OF_WEEK))
    }

    fun convertGregorianToHijri(year: Int, month: Int, day: Int): String {
        val cal = Calendar.getInstance().apply {
            set(year, month - 1, day)
        }
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        return getAstronomicalHijri(year, month, day, dayOfWeek)
    }

    private fun getAstronomicalHijri(year: Int, month: Int, day: Int, dayOfWeek: Int): String {
        var m = month
        var y = year
        if (m < 3) {
            y -= 1
            m += 12
        }

        val a = floor(y / 100.0).toInt()
        var b = 2 - a + floor(a / 4.0).toInt()
        if (y < 1583) b = 0
        val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5

        var b2 = 0
        if (jd > 2299160) {
            val a2 = floor((jd - 1867216.25) / 36524.25).toInt()
            b2 = 1 + a2 - floor(a2 / 4.0).toInt()
        }
        val bb = jd + b2 + 1524
        val cc = floor((bb - 122.1) / 365.25).toInt()
        val dd = floor(365.25 * cc).toInt()
        val ee = floor((bb - dd) / 30.6001).toInt()

        var l = (jd - 1948440 + 10632).toInt()
        val n = floor((l - 1) / 10631.0).toInt()
        l = (l - 10631 * n + 354)
        val j = (floor((10985 - l) / 5316.0) * floor((50 * l) / 17719.0) + floor(l / 5670.0) * floor((43 * l) / 15238.0)).toInt()
        l = (l - floor((30 - j) / 15.0) * floor((17719 * j) / 50.0) - floor(j / 16.0) * floor((15238 * j) / 43.0) + 29).toInt()
        val hMonth = floor((24 * l) / 709.0).toInt()
        val hDay = l - floor((709 * hMonth) / 24.0).toInt()
        val hYear = 30 * n + j - 30

        val monthNames = arrayOf(
            "محرم", "صفر", "ربيع الأول", "ربيع الثاني",
            "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
            "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
        )

        val daysOfWeek = arrayOf(
            "", "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت"
        )

        val dayName = if (dayOfWeek in 1..7) daysOfWeek[dayOfWeek] else ""
        val monthName = if (hMonth in 1..12) monthNames[hMonth - 1] else ""

        return "$dayName، $hDay $monthName $hYear هـ"
    }

    fun getGregorianDateFormatted(date: Date = Date()): String {
        val cal = Calendar.getInstance().apply { time = date }
        val daysOfWeek = arrayOf("", "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت")
        val months = arrayOf(
            "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
            "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
        )

        val dayOfWeek = daysOfWeek[cal.get(Calendar.DAY_OF_WEEK)]
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val monthName = months[cal.get(Calendar.MONTH)]
        val year = cal.get(Calendar.YEAR)

        return "$dayOfWeek، $dayOfMonth $monthName $year م"
    }
}
