package com.example.update

object SemVer {
    /**
     * Compares two semantic version strings (e.g. "2.4.0" or "v2.4.0" vs "2.0.0").
     * Returns true if [latestVersion] is strictly newer than [currentVersion].
     */
    fun isNewer(currentVersion: String, latestVersion: String): Boolean {
        val currentNums = parseVersion(currentVersion)
        val latestNums = parseVersion(latestVersion)

        val maxSize = maxOf(currentNums.size, latestNums.size)
        for (i in 0 until maxSize) {
            val curr = currentNums.getOrElse(i) { 0 }
            val latest = latestNums.getOrElse(i) { 0 }
            if (latest > curr) return true
            if (latest < curr) return false
        }
        return false
    }

    private fun parseVersion(versionStr: String): List<Int> {
        val cleaned = versionStr.trim().removePrefix("v").removePrefix("V").substringBefore("-")
        return cleaned.split(".")
            .mapNotNull { it.toIntOrNull() }
    }
}
