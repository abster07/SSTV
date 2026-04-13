package com.streamvault.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamvault.app.data.model.*
import com.streamvault.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun setTheme(theme: AppTheme) = viewModelScope.launch { settingsRepo.updateTheme(theme) }
    fun setAccentColor(color: AccentColor) = viewModelScope.launch { settingsRepo.updateAccentColor(color) }
    fun setQuality(q: StreamQuality) = viewModelScope.launch { settingsRepo.updateQuality(q) }
    fun setAudioLanguage(lang: String) = viewModelScope.launch { settingsRepo.updateAudioLanguage(lang) }
    fun setShowNsfw(show: Boolean) = viewModelScope.launch { settingsRepo.updateShowNsfw(show) }
    fun setParentalControl(enabled: Boolean, pin: String) = viewModelScope.launch { settingsRepo.updateParentalControl(enabled, pin) }
    fun setAutoPlayNext(v: Boolean) = viewModelScope.launch { settingsRepo.updateAutoPlayNext(v) }
    fun setBufferSize(ms: Int) = viewModelScope.launch { settingsRepo.updateBufferSize(ms) }
    fun setHardwareDecoding(v: Boolean) = viewModelScope.launch { settingsRepo.updateHardwareDecoding(v) }
    fun setFfmpeg(v: Boolean) = viewModelScope.launch { settingsRepo.updateFfmpeg(v) }
    fun setSubtitle(v: Boolean) = viewModelScope.launch { settingsRepo.updateSubtitle(v) }
    fun setClockVisible(v: Boolean) = viewModelScope.launch { settingsRepo.updateClockVisible(v) }
    fun setEpg(v: Boolean) = viewModelScope.launch { settingsRepo.updateEpg(v) }
    fun setUiLanguage(lang: String) = viewModelScope.launch { settingsRepo.updateUiLanguage(lang) }
    fun setRecEnabled(v: Boolean)           = viewModelScope.launch { settingsRepo.updateRecommendationEnabled(v) }
fun setRecHistory(v: Boolean)           = viewModelScope.launch { settingsRepo.updateRecommendationHistory(v) }
fun setRecFavorites(v: Boolean)         = viewModelScope.launch { settingsRepo.updateRecommendationFavorites(v) }
fun setRecTags(tags: List<String>)      = viewModelScope.launch { settingsRepo.updateRecommendationTags(tags) }
fun setRecContinents(codes: List<String>) = viewModelScope.launch { settingsRepo.updateRecommendationContinents(codes) }
fun setRecRegions(codes: List<String>)  = viewModelScope.launch { settingsRepo.updateRecommendationRegions(codes) }
fun setRecExcludeNsfw(v: Boolean)       = viewModelScope.launch { settingsRepo.updateRecommendationExcludeNsfw(v) }
fun setRecMaxResults(n: Int)            = viewModelScope.launch { settingsRepo.updateRecommendationMaxResults(n) }
}
