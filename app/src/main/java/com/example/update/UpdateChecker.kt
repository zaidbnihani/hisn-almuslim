package com.example.update

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class UpdateInfo(
    val newVersion: String,
    val releaseNotes: String,
    val downloadUrl: String
)

object UpdateChecker {
    // Default GitHub repository owner and repo name
    var GITHUB_OWNER = "zaidbnihani"
    var GITHUB_REPO = "hisn-almuslim"

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(GitHubApiService::class.java)

    /**
     * Checks GitHub for a newer release than [currentVersion].
     * Returns [UpdateInfo] if a newer release is found, or null otherwise or on error.
     */
    suspend fun checkForUpdate(
        currentVersion: String,
        owner: String = GITHUB_OWNER,
        repo: String = GITHUB_REPO
    ): UpdateInfo? {
        if (owner == "OWNER" || repo == "REPO") {
            // Placeholder repo details, fail gracefully without errors
            return null
        }
        return try {
            val response = apiService.getLatestRelease(owner, repo)
            if (response.isSuccessful) {
                val release = response.body() ?: return null
                val latestTag = release.tagName

                if (SemVer.isNewer(currentVersion, latestTag)) {
                    val apkAsset = release.assets?.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
                    val downloadUrl = apkAsset?.downloadUrl ?: release.htmlUrl

                    UpdateInfo(
                        newVersion = latestTag.removePrefix("v").removePrefix("V"),
                        releaseNotes = release.body.orEmpty().ifBlank { "لا توجد ملاحظات تفصيلية لهذا الإصدار." },
                        downloadUrl = downloadUrl
                    )
                } else null
            } else null
        } catch (e: Exception) {
            // Silently ignore network or API failures
            null
        }
    }
}
