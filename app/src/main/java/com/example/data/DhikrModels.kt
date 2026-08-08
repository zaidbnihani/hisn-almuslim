package com.example.data

data class DhikrCategory(
    val id: String,
    val name: String,
    val desc: String
)

data class DhikrItem(
    val id: String,
    val title: String,
    val text: String,
    val count: Int,
    val source: String,
    val note: String = ""
)

data class FortressItem(
    val id: String,
    val category: String,
    val title: String,
    val text: String,
    val count: Int = 1,
    val source: String,
    val note: String = ""
)

data class TasbihPhrase(
    val text: String,
    val target: Int,
    val next: String
)

data class PrayerTimes(
    val fajr: String = "--:--",
    val sunrise: String = "--:--",
    val dhuhr: String = "--:--",
    val asr: String = "--:--",
    val maghrib: String = "--:--",
    val isha: String = "--:--"
)

data class CityPreset(
    val key: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val tz: Double,
    val method: String
)

data class CustomLocation(
    val lat: Double,
    val lon: Double,
    val tz: Double,
    val name: String
)

data class MuezzinItem(
    val id: String,
    val name: String,
    val description: String,
    val audioUrl: String = ""
)
