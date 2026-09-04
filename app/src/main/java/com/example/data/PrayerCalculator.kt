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

data class PrayerDateTimes(
    val fajr: Date?,
    val dhuhr: Date?,
    val asr: Date?,
    val maghrib: Date?,
    val isha: Date?
)

object PrayerCalculator {

    fun getPrayerDates(
        date: Date = Date(),
        lat: Double,
        lon: Double,
        methodKey: String = "auto",
        countryCode: String? = null,
        madhab: String = "shafi"
    ): PrayerDateTimes {
        val coordinates = com.batoulapps.adhan.Coordinates(lat, lon)
        val cal = Calendar.getInstance().apply { time = date }
        val dateComponents = com.batoulapps.adhan.data.DateComponents(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        val method = when (methodKey) {
            "auto" -> if (countryCode != null) detectCalculationMethodByCountry(countryCode, lat, lon) else detectCalculationMethod(lat, lon)
            "egypt" -> com.batoulapps.adhan.CalculationMethod.EGYPTIAN
            "mwl", "turkey" -> com.batoulapps.adhan.CalculationMethod.MUSLIM_WORLD_LEAGUE
            "isna" -> com.batoulapps.adhan.CalculationMethod.NORTH_AMERICA
            "karachi" -> com.batoulapps.adhan.CalculationMethod.KARACHI
            "singapore" -> com.batoulapps.adhan.CalculationMethod.SINGAPORE
            "dubai" -> com.batoulapps.adhan.CalculationMethod.DUBAI
            "qatar" -> com.batoulapps.adhan.CalculationMethod.QATAR
            "kuwait" -> com.batoulapps.adhan.CalculationMethod.KUWAIT
            "moon_sighting" -> com.batoulapps.adhan.CalculationMethod.MOON_SIGHTING_COMMITTEE
            else -> com.batoulapps.adhan.CalculationMethod.UMM_AL_QURA
        }
        val params = method.parameters
        params.madhab = if (madhab == "hanafi") com.batoulapps.adhan.Madhab.HANAFI else com.batoulapps.adhan.Madhab.SHAFI
        val adhanTimes = com.batoulapps.adhan.PrayerTimes(coordinates, dateComponents, params)
        return PrayerDateTimes(
            fajr = adhanTimes.fajr,
            dhuhr = adhanTimes.dhuhr,
            asr = adhanTimes.asr,
            maghrib = adhanTimes.maghrib,
            isha = adhanTimes.isha
        )
    }

    fun detectCalculationMethod(lat: Double, lon: Double): com.batoulapps.adhan.CalculationMethod {
        return when {
            // North America (ISNA)
            lat in 15.0..85.0 && lon in -170.0..-50.0 -> com.batoulapps.adhan.CalculationMethod.NORTH_AMERICA
            
            // Egypt, Sudan, Libya (Egyptian General Authority)
            lat in 15.0..38.0 && lon in 20.0..36.0 -> com.batoulapps.adhan.CalculationMethod.EGYPTIAN
            
            // Saudi Arabia, Gulf, Levant, Yemen, Jordan, Palestine (Umm Al-Qura)
            lat in 12.0..34.0 && lon in 34.0..60.0 -> com.batoulapps.adhan.CalculationMethod.UMM_AL_QURA
            
            // Pakistan, India, Bangladesh, Afghanistan (Karachi)
            lat in 5.0..37.0 && lon in 60.0..95.0 -> com.batoulapps.adhan.CalculationMethod.KARACHI
            
            // South East Asia: Singapore / Malaysia / Indonesia (MUIS / Singapore)
            lat in -10.0..10.0 && lon in 95.0..120.0 -> com.batoulapps.adhan.CalculationMethod.SINGAPORE
            
            // Europe, Turkey, Balkans & Rest of World (Muslim World League)
            else -> com.batoulapps.adhan.CalculationMethod.MUSLIM_WORLD_LEAGUE
        }
    }

    fun detectCalculationMethodByCountry(countryCode: String, lat: Double, lon: Double): com.batoulapps.adhan.CalculationMethod {
        return when (countryCode.uppercase()) {
            "EG", "SY", "PS", "SD" -> com.batoulapps.adhan.CalculationMethod.EGYPTIAN
            "JO" -> com.batoulapps.adhan.CalculationMethod.KARACHI
            "SA", "BH", "YE", "OM", "IQ" -> com.batoulapps.adhan.CalculationMethod.UMM_AL_QURA
            "KW" -> com.batoulapps.adhan.CalculationMethod.KUWAIT
            "QA" -> com.batoulapps.adhan.CalculationMethod.QATAR
            "AE" -> com.batoulapps.adhan.CalculationMethod.DUBAI
            "PK", "IN", "BD", "AF" -> com.batoulapps.adhan.CalculationMethod.KARACHI
            "SG", "MY", "ID", "BN" -> com.batoulapps.adhan.CalculationMethod.SINGAPORE
            "US", "CA" -> com.batoulapps.adhan.CalculationMethod.NORTH_AMERICA
            "MA", "DZ", "TN", "LY", "MR", "SO", "TR" -> com.batoulapps.adhan.CalculationMethod.MUSLIM_WORLD_LEAGUE
            else -> detectCalculationMethod(lat, lon)
        }
    }

    fun calculatePrayerTimes(
        date: Date = Date(),
        lat: Double,
        lon: Double,
        timezone: Double,
        methodKey: String = "auto",
        countryCode: String? = null,
        madhab: String = "shafi",
        prayerMinuteOffsets: Map<String, Int> = emptyMap()
    ): PrayerTimes {
        val coordinates = com.batoulapps.adhan.Coordinates(lat, lon)
        
        val cal = Calendar.getInstance().apply { time = date }
        val dateComponents = com.batoulapps.adhan.data.DateComponents(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        
        var finalMethodKey = methodKey
        if (methodKey == "auto" && countryCode == null) {
            if (lat in 29.0..33.5 && lon in 34.5..39.5) {
                finalMethodKey = "karachi" // Default for Jordan Awqaf
            }
        }
        
        val method = when (finalMethodKey) {
            "auto" -> if (countryCode != null) detectCalculationMethodByCountry(countryCode, lat, lon) else detectCalculationMethod(lat, lon)
            "egypt" -> com.batoulapps.adhan.CalculationMethod.EGYPTIAN
            "mwl", "turkey" -> com.batoulapps.adhan.CalculationMethod.MUSLIM_WORLD_LEAGUE
            "isna" -> com.batoulapps.adhan.CalculationMethod.NORTH_AMERICA
            "karachi" -> com.batoulapps.adhan.CalculationMethod.KARACHI
            "singapore" -> com.batoulapps.adhan.CalculationMethod.SINGAPORE
            "dubai" -> com.batoulapps.adhan.CalculationMethod.DUBAI
            "qatar" -> com.batoulapps.adhan.CalculationMethod.QATAR
            "kuwait" -> com.batoulapps.adhan.CalculationMethod.KUWAIT
            "moon_sighting" -> com.batoulapps.adhan.CalculationMethod.MOON_SIGHTING_COMMITTEE
            else -> com.batoulapps.adhan.CalculationMethod.UMM_AL_QURA
        }
        
        var effectiveOffsets = prayerMinuteOffsets
        // Auto-apply Jordan Awqaf safety offsets if in Jordan's bounding box and no specific offsets were requested
        if (effectiveOffsets.isEmpty() && lat in 29.0..33.5 && lon in 34.5..39.5) {
            // Irbid/Amman specific corrections to match Awqaf exactly:
            // Dhuhr: raw is 1 min late, Asr is 1 min late, Maghrib is 6 mins early
            effectiveOffsets = mapOf("dhuhr" to -1, "asr" to -1, "maghrib" to 6)
        }
        
        val params = method.parameters
        params.madhab = if (madhab == "hanafi") com.batoulapps.adhan.Madhab.HANAFI else com.batoulapps.adhan.Madhab.SHAFI
        
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
        
        fun applyOffset(d: Date?, key: String): Date? {
            if (d == null) return null
            val minutes = effectiveOffsets[key] ?: 0
            if (minutes == 0) return d
            val c = Calendar.getInstance()
            c.time = d
            c.add(Calendar.MINUTE, minutes)
            return c.time
        }
        
        // Ramadan check for Isha (Umm Al-Qura extends Isha from 90 to 120 minutes)
        val hMonth = getHijriMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), 0)
        var ishaTime = adhanTimes.isha
        if (method == com.batoulapps.adhan.CalculationMethod.UMM_AL_QURA && hMonth == 9 && ishaTime != null) {
            val cIsha = Calendar.getInstance()
            cIsha.time = ishaTime
            cIsha.add(Calendar.MINUTE, 30) // Adds up to 120 minutes total since base is 90
            ishaTime = cIsha.time
        }
        
        return PrayerTimes(
            fajr = formatDateToLocalString(applyOffset(adhanTimes.fajr, "fajr")),
            sunrise = formatDateToLocalString(adhanTimes.sunrise),
            dhuhr = formatDateToLocalString(applyOffset(adhanTimes.dhuhr, "dhuhr")),
            asr = formatDateToLocalString(applyOffset(adhanTimes.asr, "asr")),
            maghrib = formatDateToLocalString(applyOffset(adhanTimes.maghrib, "maghrib")),
            isha = formatDateToLocalString(applyOffset(ishaTime, "isha"))
        )
    }

    fun getHijriDateString(date: Date = Date(), hijriOffset: Int = 0): String {
        val cal = Calendar.getInstance().apply { time = date }
        return getAstronomicalHijri(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.DAY_OF_WEEK), hijriOffset.coerceIn(-2, 2))
    }

    fun convertGregorianToHijri(year: Int, month: Int, day: Int, hijriOffset: Int = 0): String {
        val cal = Calendar.getInstance().apply {
            set(year, month - 1, day)
        }
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        return getAstronomicalHijri(year, month, day, dayOfWeek, hijriOffset.coerceIn(-2, 2))
    }

    private fun getHijriMonth(year: Int, month: Int, day: Int, hijriOffset: Int): Int {
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
        val jdAdjusted = jd + hijriOffset
        
        var l = (jdAdjusted - 1948440 + 10632).toInt()
        val n = floor((l - 1) / 10631.0).toInt()
        l = (l - 10631 * n + 354)
        val j = (floor((10985 - l) / 5316.0) * floor((50 * l) / 17719.0) + floor(l / 5670.0) * floor((43 * l) / 15238.0)).toInt()
        l = (l - floor((30 - j) / 15.0) * floor((17719 * j) / 50.0) - floor(j / 16.0) * floor((15238 * j) / 43.0) + 29).toInt()
        return floor((24 * l) / 709.0).toInt()
    }

    private fun getAstronomicalHijri(year: Int, month: Int, day: Int, dayOfWeek: Int, hijriOffset: Int = 0): String {
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
        val jdAdjusted = jd + hijriOffset

        var b2 = 0
        if (jdAdjusted > 2299160) {
            val a2 = floor((jdAdjusted - 1867216.25) / 36524.25).toInt()
            b2 = 1 + a2 - floor(a2 / 4.0).toInt()
        }
        val bb = jdAdjusted + b2 + 1524
        val cc = floor((bb - 122.1) / 365.25).toInt()
        val dd = floor(365.25 * cc).toInt()
        val ee = floor((bb - dd) / 30.6001).toInt()

        var l = (jdAdjusted - 1948440 + 10632).toInt()
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
