package com.streamvault.app.ui.screens.player

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.streamvault.app.data.model.*
import com.streamvault.app.data.repository.ChannelRepository
import com.streamvault.app.data.repository.SettingsRepository
import com.streamvault.app.player.PlayerInfo
import com.streamvault.app.player.PlayerState
import com.streamvault.app.player.StreamPlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val channel: Channel? = null,
    val currentStream: Stream? = null,
    val availableStreams: List<Stream> = emptyList(),
    val playerInfo: PlayerInfo = PlayerInfo(),
    val showControls: Boolean = true,
    val isFavorite: Boolean = false,
    val showQualityMenu: Boolean = false,
    val showLanguageMenu: Boolean = false,
    val showStreamMenu: Boolean = false,
    val controlsVisible: Boolean = true,
    val errorMessage: String? = null
)

@UnstableApi
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val channelRepo: ChannelRepository,
    private val settingsRepo: SettingsRepository,
    private val playerManager: StreamPlayerManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    val settings: StateFlow<AppSettings> = settingsRepo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    private var exoPlayer: ExoPlayer? = null
    val player get() = exoPlayer

    private var controlsAutoHideJob: Job? = null
    // FIX 1: Track the favorite observer job so we can cancel it before re-launching
    private var favoriteObserverJob: Job? = null

    init {
        viewModelScope.launch {
            playerManager.playerInfo.collect { info ->
                _uiState.update { it.copy(playerInfo = info) }
            }
        }
    }

    fun initPlayer(channelId: String) {
        val channel = channelRepo.channels.value.find { it.id == channelId } ?: return
        val streams = channel.streams.filter { it.url.isNotBlank() }
        val bestStream = streams.firstOrNull { it.quality?.contains("1080", true) == true }
            ?: streams.firstOrNull { it.quality?.contains("720", true) == true }
            ?: streams.firstOrNull()

        _uiState.update {
            it.copy(
                channel = channel,
                currentStream = bestStream,
                availableStreams = streams,
                errorMessage = null
            )
        }

        // FIX 1: Cancel the previous favorite observer before starting a new one
        favoriteObserverJob?.cancel()
        favoriteObserverJob = viewModelScope.launch {
            channelRepo.isFavorite(channelId).collect { isFav ->
                _uiState.update { it.copy(isFavorite = isFav) }
            }
        }

        buildAndPlay(bestStream)
        scheduleHideControls()
    }

    private fun buildAndPlay(stream: Stream?) {
        if (stream == null) {
            _uiState.update { it.copy(errorMessage = "No stream available for this channel") }
            return
        }

        // FIX 2: Release the old player cleanly before building a new one.
        // Do NOT call playerManager.release() here — that nulls the manager's
        // internal state and breaks the listener. Only release the ExoPlayer instance.
        exoPlayer?.release()
        exoPlayer = null

        exoPlayer = playerManager.buildPlayer(settings.value)
        exoPlayer?.let { playerManager.loadStream(stream, it) }
        _uiState.update { it.copy(currentStream = stream, errorMessage = null) }
    }

    fun selectStream(stream: Stream) {
        buildAndPlay(stream)
        _uiState.update { it.copy(showStreamMenu = false) }
    }

    fun togglePlayPause() {
        exoPlayer?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun toggleFavorite() {
        val channelId = _uiState.value.channel?.id ?: return
        viewModelScope.launch {
            channelRepo.toggleFavorite(channelId)
        }
    }

    fun setQuality(quality: StreamQuality) {
        playerManager.setQuality(quality)
        viewModelScope.launch { settingsRepo.updateQuality(quality) }
    }

    fun setAudioLanguage(langCode: String) {
        playerManager.setAudioLanguage(langCode)
    }

    fun showControls() {
        _uiState.update { it.copy(controlsVisible = true) }
        scheduleHideControls()
    }

    private fun scheduleHideControls() {
        controlsAutoHideJob?.cancel()
        controlsAutoHideJob = viewModelScope.launch {
            delay(5000)
            _uiState.update { it.copy(controlsVisible = false) }
        }
    }

    fun toggleQualityMenu() = _uiState.update { it.copy(showQualityMenu = !it.showQualityMenu) }
    fun toggleLanguageMenu() = _uiState.update { it.copy(showLanguageMenu = !it.showLanguageMenu) }
    fun toggleStreamMenu() = _uiState.update { it.copy(showStreamMenu = !it.showStreamMenu) }

    override fun onCleared() {
        super.onCleared()
        favoriteObserverJob?.cancel()
        controlsAutoHideJob?.cancel()

        exoPlayer?.release()
        exoPlayer = null
        playerManager.release()
    }
}