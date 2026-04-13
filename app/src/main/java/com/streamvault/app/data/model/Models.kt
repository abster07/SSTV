package com.streamvault.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

// ─── API Response Models ───────────────────────────────────────────────────

data class ChannelDto(
    val id: String,
    val name: String,
    @SerializedName("alt_names") val altNames: List<String> = emptyList(),
    val network: String? = null,
    val owners: List<String> = emptyList(),
    val country: String,
    val categories: List<String> = emptyList(),
    @SerializedName("is_nsfw") val isNsfw: Boolean = false,
    val launched: String? = null,
    val closed: String? = null,
    @SerializedName("replaced_by") val replacedBy: String? = null,
    val website: String? = null
)

data class FeedDto(
    val channel: String,
    val id: String,
    val name: String,
    @SerializedName("alt_names") val altNames: List<String> = emptyList(),
    @SerializedName("is_main") val isMain: Boolean = false,
    @SerializedName("broadcast_area") val broadcastArea: List<String> = emptyList(),
    val timezones: List<String> = emptyList(),
    val languages: List<String> = emptyList(),
    val format: String? = null
)

data class LogoDto(
    val channel: String,
    val feed: String? = null,
    val tags: List<String> = emptyList(),
    val width: Int = 0,
    val height: Int = 0,
    val format: String? = null,
    val url: String
)

data class StreamDto(
    val channel: String? = null,
    val feed: String? = null,
    val title: String,
    val url: String,
    val referrer: String? = null,
    @SerializedName("user_agent") val userAgent: String? = null,
    val quality: String? = null,
    val label: String? = null
)

data class CategoryDto(
    val id: String,
    val name: String,
    val description: String
)

data class LanguageDto(
    val name: String,
    val code: String
)

data class CountryDto(
    val name: String,
    val code: String,
    val languages: List<String> = emptyList(),
    val flag: String = ""
)

data class GuideDto(
    val channel: String? = null,
    val feed: String? = null,
    val site: String,
    @SerializedName("site_id") val siteId: String,
    @SerializedName("site_name") val siteName: String,
    val lang: String
)

data class BlocklistDto(
    val channel: String,
    val reason: String,
    val ref: String
)

// ─── Domain / UI Models ────────────────────────────────────────────────────

data class Channel(
    val id: String,
    val name: String,
    val altNames: List<String>,
    val network: String?,
    val country: String,
    val countryFlag: String,
    val categories: List<String>,
    val isNsfw: Boolean,
    val website: String?,
    val logoUrl: String?,
    val streams: List<Stream>,
    val isFavorite: Boolean = false,
    val isBlocked: Boolean = false
)

data class Stream(
    val channelId: String?,
    val feedId: String?,
    val title: String,
    val url: String,
    val referrer: String?,
    val userAgent: String?,
    val quality: String?,
    val label: String?
)

data class Category(
    val id: String,
    val name: String,
    val description: String,
    val iconRes: Int = 0
)

data class Country(
    val name: String,
    val code: String,
    val languages: List<String>,
    val flag: String
)

// ─── Room Entities ─────────────────────────────────────────────────────────

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val channelId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val channelId: String,
    val channelName: String,
    val logoUrl: String?,
    val streamUrl: String,
    val watchedAt: Long = System.currentTimeMillis(),
    val durationMs: Long = 0
)

// ─── Settings / Preferences ────────────────────────────────────────────────

data class AppSettings(
    val theme: AppTheme = AppTheme.DARK,
    val accentColor: AccentColor = AccentColor.CYAN,
    val defaultQuality: StreamQuality = StreamQuality.AUTO,
    val audioLanguage: String = "eng",
    val uiLanguage: String = "en",
    val parentalControlEnabled: Boolean = false,
    val parentalControlPin: String = "",
    val showNsfw: Boolean = false,
    val autoPlayNext: Boolean = true,
    val bufferSizeMs: Int = 5000,
    val useHardwareDecoding: Boolean = true,
    val useFfmpegDecoder: Boolean = true,
    val subtitleEnabled: Boolean = false,
    val clockVisible: Boolean = true,
    val epgEnabled: Boolean = true
)

enum class AppTheme { DARK, AMOLED, MIDNIGHT_BLUE, FOREST }
enum class AccentColor(val hex: Long) {
    CYAN(0xFF00E5FF),
    ORANGE(0xFFFF6D00),
    PINK(0xFFE91E8C),
    GREEN(0xFF00E676),
    GOLD(0xFFFFD600)
}
enum class StreamQuality { AUTO, P1080, P720, P480, P360 }


// ─── Recommendation Engine ─────────────────────────────────────────────────

data class RecommendationSettings(
    val enabled: Boolean = true,
    val basedOnHistory: Boolean = true,
    val basedOnFavorites: Boolean = true,
    val preferredRegions: List<String> = emptyList(),   // country codes
    val preferredContinents: List<String> = emptyList(), // e.g. "EU", "AS", "NA"
    val preferredTags: List<String> = emptyList(),       // category ids
    val excludeNsfw: Boolean = true,
    val maxResults: Int = 20
)

data class RecommendedChannel(
    val channel: Channel,
    val score: Float,
    val reasons: List<RecommendationReason>
)

sealed class RecommendationReason {
    data class SimilarToWatched(val channelName: String) : RecommendationReason()
    data class MatchesCategory(val categoryName: String) : RecommendationReason()
    data class MatchesCountry(val countryName: String) : RecommendationReason()
    data class MatchesContinent(val continentName: String) : RecommendationReason()
    data class InFavoriteNetwork(val network: String) : RecommendationReason()
    object PopularInRegion : RecommendationReason()
}

// Continent → country code mapping
object ContinentMap {
    val continents = mapOf(
        "AF" to "Africa",
        "AS" to "Asia",
        "EU" to "Europe",
        "NA" to "North America",
        "SA" to "South America",
        "OC" to "Oceania",
        "ME" to "Middle East"
    )

    // Abbreviated — covers the most common iptv-org country codes
    val countryToContinent = mapOf(
        // Europe
        "GB" to "EU", "DE" to "EU", "FR" to "EU", "IT" to "EU", "ES" to "EU",
        "NL" to "EU", "PL" to "EU", "RU" to "EU", "SE" to "EU", "NO" to "EU",
        "DK" to "EU", "FI" to "EU", "CH" to "EU", "AT" to "EU", "BE" to "EU",
        "PT" to "EU", "GR" to "EU", "CZ" to "EU", "HU" to "EU", "RO" to "EU",
        "UA" to "EU", "HR" to "EU", "RS" to "EU", "SK" to "EU", "BG" to "EU",
        "SI" to "EU", "LT" to "EU", "LV" to "EU", "EE" to "EU", "IE" to "EU",
        "TR" to "EU", "AL" to "EU", "MK" to "EU", "BA" to "EU", "ME" to "EU",
        // Asia
        "CN" to "AS", "JP" to "AS", "KR" to "AS", "IN" to "AS", "ID" to "AS",
        "TH" to "AS", "VN" to "AS", "PH" to "AS", "MY" to "AS", "SG" to "AS",
        "PK" to "AS", "BD" to "AS", "NP" to "AS", "LK" to "AS", "MM" to "AS",
        "KH" to "AS", "LA" to "AS", "HK" to "AS", "TW" to "AS", "MN" to "AS",
        // Middle East
        "SA" to "ME", "AE" to "ME", "IR" to "ME", "IQ" to "ME", "SY" to "ME",
        "JO" to "ME", "LB" to "ME", "IL" to "ME", "KW" to "ME", "QA" to "ME",
        "BH" to "ME", "OM" to "ME", "YE" to "ME", "PS" to "ME",
        // Africa
        "NG" to "AF", "EG" to "AF", "ZA" to "AF", "KE" to "AF", "ET" to "AF",
        "GH" to "AF", "TZ" to "AF", "MA" to "AF", "DZ" to "AF", "TN" to "AF",
        "CM" to "AF", "CI" to "AF", "SN" to "AF", "ZW" to "AF", "UG" to "AF",
        // North America
        "US" to "NA", "CA" to "NA", "MX" to "NA", "CU" to "NA", "HT" to "NA",
        "DO" to "NA", "GT" to "NA", "HN" to "NA", "SV" to "NA", "NI" to "NA",
        "CR" to "NA", "PA" to "NA", "JM" to "NA", "TT" to "NA", "BB" to "NA",
        // South America
        "BR" to "SA", "AR" to "SA", "CO" to "SA", "PE" to "SA", "VE" to "SA",
        "CL" to "SA", "EC" to "SA", "BO" to "SA", "PY" to "SA", "UY" to "SA",
        // Oceania
        "AU" to "OC", "NZ" to "OC", "PG" to "OC", "FJ" to "OC"
    )

    fun getContinentForCountry(countryCode: String) =
        countryToContinent[countryCode.uppercase()]

    fun getCountriesForContinent(continentCode: String) =
        countryToContinent.filter { it.value == continentCode }.keys.toSet()
}