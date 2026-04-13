package com.streamvault.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.streamvault.app.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "streamvault_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val DEFAULT_QUALITY = stringPreferencesKey("default_quality")
        val AUDIO_LANGUAGE = stringPreferencesKey("audio_language")
        val UI_LANGUAGE = stringPreferencesKey("ui_language")
        val PARENTAL_ENABLED = booleanPreferencesKey("parental_enabled")
        val PARENTAL_PIN = stringPreferencesKey("parental_pin")
        val SHOW_NSFW = booleanPreferencesKey("show_nsfw")
        val AUTO_PLAY_NEXT = booleanPreferencesKey("auto_play_next")
        val BUFFER_SIZE_MS = intPreferencesKey("buffer_size_ms")
        val USE_HW_DECODING = booleanPreferencesKey("use_hw_decoding")
        val USE_FFMPEG = booleanPreferencesKey("use_ffmpeg")
        val SUBTITLE_ENABLED = booleanPreferencesKey("subtitle_enabled")
        val CLOCK_VISIBLE = booleanPreferencesKey("clock_visible")
        val EPG_ENABLED = booleanPreferencesKey("epg_enabled")
        val REC_ENABLED           = booleanPreferencesKey("rec_enabled")
        val REC_HISTORY           = booleanPreferencesKey("rec_history")
        val REC_FAVORITES         = booleanPreferencesKey("rec_favorites") 
        val REC_REGIONS           = stringPreferencesKey("rec_regions")      // comma-separated
        val REC_CONTINENTS        = stringPreferencesKey("rec_continents")   // comma-separated
        val REC_TAGS              = stringPreferencesKey("rec_tags")         // comma-separated
        val REC_EXCLUDE_NSFW      = booleanPreferencesKey("rec_exclude_nsfw")
        val REC_MAX_RESULTS       = intPreferencesKey("rec_max_results")
    }
    val recommendationSettings: RecommendationSettings = RecommendationSettings()
    val settings: Flow<AppSettings> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            AppSettings(
                theme = AppTheme.valueOf(prefs[Keys.THEME] ?: AppTheme.DARK.name),
                accentColor = AccentColor.valueOf(prefs[Keys.ACCENT_COLOR] ?: AccentColor.CYAN.name),
                defaultQuality = StreamQuality.valueOf(prefs[Keys.DEFAULT_QUALITY] ?: StreamQuality.AUTO.name),
                audioLanguage = prefs[Keys.AUDIO_LANGUAGE] ?: "eng",
                uiLanguage = prefs[Keys.UI_LANGUAGE] ?: "en",
                parentalControlEnabled = prefs[Keys.PARENTAL_ENABLED] ?: false,
                parentalControlPin = prefs[Keys.PARENTAL_PIN] ?: "",
                showNsfw = prefs[Keys.SHOW_NSFW] ?: false,
                autoPlayNext = prefs[Keys.AUTO_PLAY_NEXT] ?: true,
                bufferSizeMs = prefs[Keys.BUFFER_SIZE_MS] ?: 5000,
                useHardwareDecoding = prefs[Keys.USE_HW_DECODING] ?: true,
                useFfmpegDecoder = prefs[Keys.USE_FFMPEG] ?: true,
                subtitleEnabled = prefs[Keys.SUBTITLE_ENABLED] ?: false,
                clockVisible = prefs[Keys.CLOCK_VISIBLE] ?: true,
                epgEnabled = prefs[Keys.EPG_ENABLED] ?: true,
                recommendationSettings = RecommendationSettings(
                  enabled         = prefs[Keys.REC_ENABLED]       ?: true,
                  basedOnHistory  = prefs[Keys.REC_HISTORY]       ?: true,
                  basedOnFavorites= prefs[Keys.REC_FAVORITES]     ?: true,
                  preferredRegions    = prefs[Keys.REC_REGIONS]
                  ?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
                  preferredContinents = prefs[Keys.REC_CONTINENTS]
                  ?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
                  preferredTags       = prefs[Keys.REC_TAGS]
                  ?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
                  excludeNsfw     = prefs[Keys.REC_EXCLUDE_NSFW]  ?: true,
                  maxResults      = prefs[Keys.REC_MAX_RESULTS]   ?: 20
                )
            )
        }

    suspend fun updateTheme(theme: AppTheme) = context.dataStore.edit {
        it[Keys.THEME] = theme.name
    }

    suspend fun updateAccentColor(color: AccentColor) = context.dataStore.edit {
        it[Keys.ACCENT_COLOR] = color.name
    }

    suspend fun updateQuality(quality: StreamQuality) = context.dataStore.edit {
        it[Keys.DEFAULT_QUALITY] = quality.name
    }

    suspend fun updateAudioLanguage(lang: String) = context.dataStore.edit {
        it[Keys.AUDIO_LANGUAGE] = lang
    }

    suspend fun updateShowNsfw(show: Boolean) = context.dataStore.edit {
        it[Keys.SHOW_NSFW] = show
    }

    suspend fun updateParentalControl(enabled: Boolean, pin: String) = context.dataStore.edit {
        it[Keys.PARENTAL_ENABLED] = enabled
        it[Keys.PARENTAL_PIN] = pin
    }

    suspend fun updateAutoPlayNext(autoPlay: Boolean) = context.dataStore.edit {
        it[Keys.AUTO_PLAY_NEXT] = autoPlay
    }

    suspend fun updateBufferSize(ms: Int) = context.dataStore.edit {
        it[Keys.BUFFER_SIZE_MS] = ms
    }

    suspend fun updateHardwareDecoding(enabled: Boolean) = context.dataStore.edit {
        it[Keys.USE_HW_DECODING] = enabled
    }

    suspend fun updateFfmpeg(enabled: Boolean) = context.dataStore.edit {
        it[Keys.USE_FFMPEG] = enabled
    }

    suspend fun updateSubtitle(enabled: Boolean) = context.dataStore.edit {
        it[Keys.SUBTITLE_ENABLED] = enabled
    }

    suspend fun updateClockVisible(visible: Boolean) = context.dataStore.edit {
        it[Keys.CLOCK_VISIBLE] = visible
    }

    suspend fun updateEpg(enabled: Boolean) = context.dataStore.edit {
        it[Keys.EPG_ENABLED] = enabled
    }

    suspend fun updateUiLanguage(lang: String) = context.dataStore.edit {
        it[Keys.UI_LANGUAGE] = lang
    }
    
    suspend fun updateRecommendationEnabled(v: Boolean) = context.dataStore.edit {
        it[Keys.REC_ENABLED] = v
      }
  suspend fun updateRecommendationHistory(v: Boolean) = context.dataStore.edit {
      it[Keys.REC_HISTORY] = v
    }
      
  suspend fun updateRecommendationFavorites(v: Boolean) = context.dataStore.edit {
       it[Keys.REC_FAVORITES] = v
    }
        
  suspend fun updateRecommendationRegions(codes: List<String>) = context.dataStore.edit {
      it[Keys.REC_REGIONS] = codes.joinToString(",")
    }
          
  suspend fun updateRecommendationContinents(codes: List<String>) = context.dataStore.edit {
      it[Keys.REC_CONTINENTS] = codes.joinToString(",")
    }
            
  suspend fun updateRecommendationTags(tags: List<String>) = context.dataStore.edit {
      it[Keys.REC_TAGS] = tags.joinToString(",")
  }
  suspend fun updateRecommendationExcludeNsfw(v: Boolean) = context.dataStore.edit {
      it[Keys.REC_EXCLUDE_NSFW] = v
    }
                
  suspend fun updateRecommendationMaxResults(n: Int) = context.dataStore.edit {
       it[Keys.REC_MAX_RESULTS] = n
     }
}