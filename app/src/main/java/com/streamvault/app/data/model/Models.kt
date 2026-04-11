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
