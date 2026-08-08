package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AthkarData
import com.example.data.CityPreset
import com.example.data.CustomLocation
import com.example.data.DhikrItem
import com.example.data.FortressItem
import com.example.data.PrayerCalculator
import com.example.data.PrayerTimes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class AthkarUiState(
    val activeCategory: String = "morning",
    val remainingCounts: Map<String, Map<String, Int>> = emptyMap(),
    val completedDates: Map<String, String> = emptyMap(),
    
    // Privacy Gate State
    val showPrivacyGate: Boolean = false,
    val privacyChecking: Boolean = true,
    val privacyVersion: String = "2026-08-08",
    val privacyPolicyUrl: String = "https://zaidmtsmbanihani.blogspot.com/2026/08/blog-post.html",
    val acceptedPrivacyVersion: String? = null,
    
    // Tasbih
    val activeTasbihIndex: Int = 0,
    val currentTasbihCount: Int = 0,
    val tasbihSession: Int = 0,
    val tasbihTotal: Int = 0,
    
    // Prayer
    val selectedCityKey: String = "mecca",
    val selectedMethodKey: String = "umm_alqura",
    val customLocation: CustomLocation? = null,
    val prayerTimes: PrayerTimes = PrayerTimes(),
    val nextPrayerName: String = "--",
    val nextPrayerKey: String = "",
    val countdownText: String = "متبقي --:--:--",
    val countdownHours: Int = 0,
    val countdownMinutes: Int = 0,
    val countdownSeconds: Int = 0,
    val countdownProgress: Float = 0f,
    val hijriDateText: String = "--",
    val gregorianDateText: String = "--",
    
    // Fortress
    val fortressQuery: String = "",
    
    // Date Converter
    val convertYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val convertMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val convertDay: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
    val convertedHijriDate: String = "",
    
    // Settings
    val soundEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val selectedMuezzinId: String = "mishaari",
    val activeAdhanPrayerName: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("athkar_user_data", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(AthkarUiState())
    val uiState: StateFlow<AthkarUiState> = _uiState.asStateFlow()

    private var lastTriggeredAdhanKey = ""

    init {
        loadSavedState()
        checkPrivacyPolicyVersion()
        recalculatePrayerTimes()
        updateConvertDate(
            _uiState.value.convertYear,
            _uiState.value.convertMonth,
            _uiState.value.convertDay
        )
        startPrayerCountdownTimer()
    }

    private fun loadSavedState() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        val initialCounts = mutableMapOf<String, MutableMap<String, Int>>()
        val completedDatesMap = mutableMapOf<String, String>()

        AthkarData.categories.forEach { cat ->
            val catMap = mutableMapOf<String, Int>()
            val dhikrs = AthkarData.getDhikrsForCategory(cat.id)
            var allDone = true

            dhikrs.forEach { d ->
                val prefKey = "count_${cat.id}_${d.id}"
                val savedCount = if (prefs.contains(prefKey)) prefs.getInt(prefKey, d.count) else d.count
                catMap[d.id] = savedCount
                if (savedCount > 0) allDone = false
            }
            initialCounts[cat.id] = catMap

            val compDateKey = "completed_date_${cat.id}"
            val savedCompDate = prefs.getString(compDateKey, null)
            if (savedCompDate != null) {
                completedDatesMap[cat.id] = savedCompDate
            } else if (allDone) {
                completedDatesMap[cat.id] = todayStr
            }
        }

        // Settings & Tasbih
        val savedCityKey = prefs.getString("selected_city", "mecca") ?: "mecca"
        val savedMethodKey = prefs.getString("selected_method", "umm_alqura") ?: "umm_alqura"
        val savedMuezzinId = prefs.getString("selected_muezzin", "mishaari") ?: "mishaari"
        val savedSound = prefs.getBoolean("sound_enabled", true)
        val savedNotifs = prefs.getBoolean("notifs_enabled", true)
        val savedTasbihIdx = prefs.getInt("tasbih_index", 0)
        val savedTasbihCount = prefs.getInt("tasbih_count", 0)
        val savedTasbihSession = prefs.getInt("tasbih_session", 0)
        val savedTasbihTotal = prefs.getInt("tasbih_total", 0)

        val savedAcceptedPrivacy = prefs.getString("accepted_privacy_version", null)

        _uiState.value = _uiState.value.copy(
            remainingCounts = initialCounts,
            completedDates = completedDatesMap,
            acceptedPrivacyVersion = savedAcceptedPrivacy,
            showPrivacyGate = (savedAcceptedPrivacy == null),
            selectedCityKey = savedCityKey,
            selectedMethodKey = savedMethodKey,
            selectedMuezzinId = savedMuezzinId,
            soundEnabled = savedSound,
            notificationsEnabled = savedNotifs,
            activeTasbihIndex = savedTasbihIdx,
            currentTasbihCount = savedTasbihCount,
            tasbihSession = savedTasbihSession,
            tasbihTotal = savedTasbihTotal
        )
    }

    private fun saveAthkarProgress(categoryId: String) {
        val editor = prefs.edit()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        editor.putString("last_app_date", todayStr)

        val catMap = _uiState.value.remainingCounts[categoryId] ?: emptyMap()
        catMap.forEach { (dhikrId, count) ->
            editor.putInt("count_${categoryId}_$dhikrId", count)
        }

        val compDate = _uiState.value.completedDates[categoryId]
        if (compDate != null) {
            editor.putString("completed_date_$categoryId", compDate)
        } else {
            editor.remove("completed_date_$categoryId")
        }
        editor.apply()
    }

    private fun saveTasbihState() {
        prefs.edit()
            .putInt("tasbih_index", _uiState.value.activeTasbihIndex)
            .putInt("tasbih_count", _uiState.value.currentTasbihCount)
            .putInt("tasbih_session", _uiState.value.tasbihSession)
            .putInt("tasbih_total", _uiState.value.tasbihTotal)
            .apply()
    }

    private fun saveSettingsState() {
        prefs.edit()
            .putString("selected_city", _uiState.value.selectedCityKey)
            .putString("selected_method", _uiState.value.selectedMethodKey)
            .putString("selected_muezzin", _uiState.value.selectedMuezzinId)
            .putBoolean("sound_enabled", _uiState.value.soundEnabled)
            .putBoolean("notifs_enabled", _uiState.value.notificationsEnabled)
            .apply()
    }

    fun selectCategory(catId: String) {
        _uiState.value = _uiState.value.copy(activeCategory = catId)
    }

    fun decrementDhikr(categoryId: String, dhikrId: String, maxCount: Int) {
        val currentCounts = _uiState.value.remainingCounts.toMutableMap()
        val catMap = (currentCounts[categoryId] ?: emptyMap()).toMutableMap()
        val currentVal = catMap[dhikrId] ?: maxCount

        if (currentVal <= 0) {
            catMap[dhikrId] = maxCount
        } else {
            val newVal = currentVal - 1
            catMap[dhikrId] = newVal
        }
        currentCounts[categoryId] = catMap

        // Check if all done
        val dhikrs = AthkarData.getDhikrsForCategory(categoryId)
        val allCompleted = dhikrs.all { (catMap[it.id] ?: 0) == 0 }

        val completedDatesMap = _uiState.value.completedDates.toMutableMap()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        if (allCompleted) {
            completedDatesMap[categoryId] = todayStr
        } else {
            completedDatesMap.remove(categoryId)
        }

        _uiState.value = _uiState.value.copy(
            remainingCounts = currentCounts,
            completedDates = completedDatesMap
        )

        saveAthkarProgress(categoryId)
    }

    fun resetCategoryProgress(categoryId: String) {
        val currentCounts = _uiState.value.remainingCounts.toMutableMap()
        val catMap = mutableMapOf<String, Int>()
        val dhikrs = AthkarData.getDhikrsForCategory(categoryId)
        dhikrs.forEach { d ->
            catMap[d.id] = d.count
        }
        currentCounts[categoryId] = catMap

        val completedDatesMap = _uiState.value.completedDates.toMutableMap()
        completedDatesMap.remove(categoryId)

        _uiState.value = _uiState.value.copy(
            remainingCounts = currentCounts,
            completedDates = completedDatesMap
        )

        saveAthkarProgress(categoryId)
    }

    // Tasbih
    fun incrementTasbih() {
        val activePhrase = AthkarData.tasbihPhrases.getOrNull(_uiState.value.activeTasbihIndex)
            ?: AthkarData.tasbihPhrases[0]

        val newCount = _uiState.value.currentTasbihCount + 1
        val newSession = _uiState.value.tasbihSession + 1
        val newTotal = _uiState.value.tasbihTotal + 1

        if (newCount >= activePhrase.target) {
            val nextIdx = (_uiState.value.activeTasbihIndex + 1) % AthkarData.tasbihPhrases.size
            _uiState.value = _uiState.value.copy(
                activeTasbihIndex = nextIdx,
                currentTasbihCount = 0,
                tasbihSession = newSession,
                tasbihTotal = newTotal
            )
        } else {
            _uiState.value = _uiState.value.copy(
                currentTasbihCount = newCount,
                tasbihSession = newSession,
                tasbihTotal = newTotal
            )
        }
        saveTasbihState()
    }

    fun selectTasbihPhrase(index: Int) {
        if (index in AthkarData.tasbihPhrases.indices) {
            _uiState.value = _uiState.value.copy(
                activeTasbihIndex = index,
                currentTasbihCount = 0
            )
            saveTasbihState()
        }
    }

    fun resetCurrentTasbih() {
        _uiState.value = _uiState.value.copy(
            currentTasbihCount = 0,
            tasbihSession = 0
        )
        saveTasbihState()
    }

    // Prayer
    fun selectCity(cityKey: String) {
        _uiState.value = _uiState.value.copy(
            selectedCityKey = cityKey,
            customLocation = null
        )
        recalculatePrayerTimes()
        saveSettingsState()
    }

    fun selectMethod(methodKey: String) {
        _uiState.value = _uiState.value.copy(selectedMethodKey = methodKey)
        recalculatePrayerTimes()
        saveSettingsState()
    }

    fun setCustomLocation(lat: Double, lon: Double, name: String) {
        val offsetMillis = java.util.TimeZone.getDefault().getOffset(System.currentTimeMillis())
        val tzHours = offsetMillis / (1000.0 * 60 * 60)
        val customLoc = CustomLocation(
            lat = lat,
            lon = lon,
            tz = tzHours,
            name = name
        )
        _uiState.value = _uiState.value.copy(customLocation = customLoc)
        recalculatePrayerTimes()
    }

    fun recalculatePrayerTimes() {
        val today = Date()
        val lat: Double
        val lon: Double
        val tz: Double
        val methodKey = _uiState.value.selectedMethodKey
        val gregorianText: String

        val custom = _uiState.value.customLocation
        if (custom != null) {
            lat = custom.lat
            lon = custom.lon
            tz = custom.tz
            gregorianText = "موقعي الحالي (${custom.name})"
        } else {
            val city = AthkarData.presetCities.find { it.key == _uiState.value.selectedCityKey }
                ?: AthkarData.presetCities[0]
            lat = city.lat
            lon = city.lon
            tz = city.tz
            gregorianText = PrayerCalculator.getGregorianDateFormatted(today)
        }

        val times = PrayerCalculator.calculatePrayerTimes(today, lat, lon, tz, methodKey)
        val hijriText = PrayerCalculator.getHijriDateString(today)

        _uiState.value = _uiState.value.copy(
            prayerTimes = times,
            hijriDateText = hijriText,
            gregorianDateText = gregorianText
        )

        updateNextPrayerAndCountdown()
    }

    private fun startPrayerCountdownTimer() {
        viewModelScope.launch {
            while (true) {
                updateNextPrayerAndCountdown()
                delay(1000)
            }
        }
    }

    private fun updateNextPrayerAndCountdown() {
        val times = _uiState.value.prayerTimes
        val now = Calendar.getInstance()
        val currentMins = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        fun parseTimeToMinutes(timeStr: String): Int {
            val parts = timeStr.trim().split(" ")
            if (parts.size < 2) return 0
            val timeParts = parts[0].split(":")
            if (timeParts.size < 2) return 0
            var h = timeParts[0].toIntOrNull() ?: 0
            val m = timeParts[1].toIntOrNull() ?: 0
            val period = parts[1]
            if (period == "م" && h != 12) h += 12
            if (period == "ص" && h == 12) h = 0
            return h * 60 + m
        }

        data class PrayerItem(val key: String, val name: String, val mins: Int)

        val prayers = listOf(
            PrayerItem("fajr", "الفجر", parseTimeToMinutes(times.fajr)),
            PrayerItem("sunrise", "الشروق", parseTimeToMinutes(times.sunrise)),
            PrayerItem("dhuhr", "الظهر", parseTimeToMinutes(times.dhuhr)),
            PrayerItem("asr", "العصر", parseTimeToMinutes(times.asr)),
            PrayerItem("maghrib", "المغرب", parseTimeToMinutes(times.maghrib)),
            PrayerItem("isha", "العشاء", parseTimeToMinutes(times.isha))
        )

        var nextPrayer = prayers[0]
        var isNextDay = false

        for (p in prayers) {
            if (currentMins < p.mins) {
                nextPrayer = p
                break
            }
        }

        if (currentMins >= prayers.last().mins) {
            nextPrayer = prayers[0]
            isNextDay = true
        }

        val currentSecs = now.get(Calendar.HOUR_OF_DAY) * 3600 + now.get(Calendar.MINUTE) * 60 + now.get(Calendar.SECOND)
        var targetSecs = nextPrayer.mins * 60
        if (isNextDay) targetSecs += 24 * 3600

        var diffSecs = targetSecs - currentSecs
        if (diffSecs < 0) diffSecs = 0

        val hrs = diffSecs / 3600
        val mns = (diffSecs % 3600) / 60
        val scs = diffSecs % 60

        val nextIdx = prayers.indexOf(nextPrayer)
        val prevPrayerMins = if (nextIdx > 0) {
            prayers[nextIdx - 1].mins
        } else {
            prayers.last().mins - 24 * 60
        }
        val intervalSecs = (nextPrayer.mins - prevPrayerMins) * 60f
        val maxIntervalSecs = if (intervalSecs > 0f) intervalSecs else 6 * 3600f
        val progress = (1.0f - (diffSecs.toFloat() / maxIntervalSecs)).coerceIn(0f, 1f)

        val countdownStr = String.format("متبقي %02d:%02d", hrs, mns)

        var triggerAdhanName: String? = _uiState.value.activeAdhanPrayerName
        if (diffSecs == 0) {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val triggerKey = "${nextPrayer.key}-$todayStr"
            if (lastTriggeredAdhanKey != triggerKey) {
                lastTriggeredAdhanKey = triggerKey
                if (_uiState.value.notificationsEnabled) {
                    triggerAdhanName = nextPrayer.name
                }
            }
        }

        _uiState.value = _uiState.value.copy(
            nextPrayerName = nextPrayer.name,
            nextPrayerKey = nextPrayer.key,
            countdownText = countdownStr,
            countdownHours = hrs,
            countdownMinutes = mns,
            countdownSeconds = scs,
            countdownProgress = progress,
            activeAdhanPrayerName = triggerAdhanName
        )
    }

    // Fortress
    fun updateFortressQuery(query: String) {
        _uiState.value = _uiState.value.copy(fortressQuery = query)
    }

    // Date Converter
    fun updateConvertDate(year: Int, month: Int, day: Int) {
        val converted = PrayerCalculator.convertGregorianToHijri(year, month, day)
        _uiState.value = _uiState.value.copy(
            convertYear = year,
            convertMonth = month,
            convertDay = day,
            convertedHijriDate = converted
        )
    }

    // Settings
    fun toggleSound() {
        _uiState.value = _uiState.value.copy(soundEnabled = !_uiState.value.soundEnabled)
        saveSettingsState()
    }

    fun toggleNotifications() {
        _uiState.value = _uiState.value.copy(notificationsEnabled = !_uiState.value.notificationsEnabled)
        saveSettingsState()
    }

    fun selectMuezzin(muezzinId: String) {
        _uiState.value = _uiState.value.copy(selectedMuezzinId = muezzinId)
        saveSettingsState()
    }

    fun stopAdhan() {
        _uiState.value = _uiState.value.copy(activeAdhanPrayerName = null)
    }

    fun resetAllData() {
        prefs.edit().clear().apply()
        _uiState.value = AthkarUiState()
        loadSavedState()
        checkPrivacyPolicyVersion()
        recalculatePrayerTimes()
    }

    // Privacy Policy Consent Gate
    fun checkPrivacyPolicyVersion() {
        val acceptedVer = prefs.getString("accepted_privacy_version", null)
        _uiState.value = _uiState.value.copy(acceptedPrivacyVersion = acceptedVer)

        viewModelScope.launch(Dispatchers.IO) {
            var remoteVersion = "2026-08-08"
            var remoteUrl = "https://zaidmtsmbanihani.blogspot.com/2026/08/blog-post.html"

            try {
                val url = java.net.URL("https://zaidmtsmbanihani.blogspot.com/2026/08/privacy_version.json")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                conn.requestMethod = "GET"

                if (conn.responseCode == 200) {
                    val text = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = org.json.JSONObject(text)
                    if (json.has("version")) {
                        remoteVersion = json.getString("version")
                    }
                    if (json.has("url")) {
                        remoteUrl = json.getString("url")
                    }
                }
                conn.disconnect()
            } catch (e: Exception) {
                // If network fetch fails and user has already accepted a local version, keep offline user unblocked
                if (acceptedVer != null) {
                    remoteVersion = acceptedVer
                }
            }

            val needsConsent = acceptedVer == null || acceptedVer != remoteVersion

            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    privacyChecking = false,
                    privacyVersion = remoteVersion,
                    privacyPolicyUrl = remoteUrl,
                    showPrivacyGate = needsConsent
                )
            }
        }
    }

    fun acceptPrivacyPolicy() {
        val ver = _uiState.value.privacyVersion
        prefs.edit()
            .putString("accepted_privacy_version", ver)
            .putLong("accepted_privacy_timestamp", System.currentTimeMillis())
            .apply()

        _uiState.value = _uiState.value.copy(
            showPrivacyGate = false,
            acceptedPrivacyVersion = ver
        )
    }
}
