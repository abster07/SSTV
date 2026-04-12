package com.streamvault.app.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import com.streamvault.app.data.model.AppSettings
import com.streamvault.app.data.model.Stream
import com.streamvault.app.data.model.StreamQuality
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class PlayerState { IDLE, LOADING, PLAYING, PAUSED, BUFFERING, ERROR }

data class PlayerInfo(
    val state: PlayerState = PlayerState.IDLE,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0,
    val duration: Long = 0,
    val bufferedPercent: Int = 0,
    val videoFormat: Format? = null,
    val audioFormat: Format? = null,
    val playbackSpeed: Float = 1f,
    val volume: Float = 1f,
    val errorMessage: String? = null
)

@Singleton
@UnstableApi
class StreamPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var exoPlayer: ExoPlayer? = null
    private var trackSelector: DefaultTrackSelector? = null

    private val _playerInfo = MutableStateFlow(PlayerInfo())
    val playerInfo: StateFlow<PlayerInfo> = _playerInfo

    private val _availableQualities = MutableStateFlow<List<String>>(emptyList())
    val availableQualities: StateFlow<List<String>> = _availableQualities

    fun buildPlayer(settings: AppSettings): ExoPlayer {
        release()

        trackSelector = DefaultTrackSelector(context).apply {
            val paramsBuilder = buildUponParameters()

            when (settings.defaultQuality) {
                StreamQuality.P1080 -> paramsBuilder.setMaxVideoSize(1920, 1080)
                StreamQuality.P720  -> paramsBuilder.setMaxVideoSize(1280, 720)
                StreamQuality.P480  -> paramsBuilder.setMaxVideoSize(854, 480)
                StreamQuality.P360  -> paramsBuilder.setMaxVideoSize(640, 360)
                StreamQuality.AUTO  -> paramsBuilder.clearVideoSizeConstraints()
            }

            if (settings.audioLanguage.isNotEmpty()) {
                paramsBuilder.setPreferredAudioLanguage(settings.audioLanguage)
            }

            parameters = paramsBuilder.build()
        }

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                settings.bufferSizeMs,
                settings.bufferSizeMs * 4,
                1000,
                2000
            )
            .build()

        val renderersFactory = MediaPlayer.buildCustomRenderersFactory(
            context, settings.useHardwareDecoding, settings.useFfmpegDecoder
        )

        val player = ExoPlayer.Builder(context, renderersFactory)
            .setTrackSelector(trackSelector!!)
            .setLoadControl(loadControl)
            .setBandwidthMeter(DefaultBandwidthMeter.getSingletonInstance(context))
            .build()
            .also {
                it.addListener(playerListener)
                exoPlayer = it
            }

        return player
    }

    fun loadStream(stream: Stream, player: ExoPlayer) {
        val httpFactory = buildOkHttpDataSourceFactory(stream)
        val dataSourceFactory = DefaultDataSource.Factory(context, httpFactory)

        val mediaItem = MediaItem.Builder()
            .setUri(stream.url)
            .build()

        val mediaSource = when {
            stream.url.contains(".m3u8", ignoreCase = true) ||
            stream.url.contains("hls",  ignoreCase = true) -> {
                // Use the DataSource.Factory overload to avoid ambiguity
                HlsMediaSource.Factory(dataSourceFactory as androidx.media3.datasource.DataSource.Factory)
                    .createMediaSource(mediaItem)
            }
            stream.url.contains(".mpd", ignoreCase = true) -> {
                androidx.media3.exoplayer.dash.DashMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem)
            }
            else -> {
                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            }
        }

        player.apply {
            setMediaSource(mediaSource)
            prepare()
            playWhenReady = true
        }

        _playerInfo.value = _playerInfo.value.copy(state = PlayerState.LOADING)
    }

    private fun buildOkHttpDataSourceFactory(stream: Stream): OkHttpDataSource.Factory {
        val okClient = okhttp3.OkHttpClient.Builder()
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .apply {
                if (stream.referrer != null || stream.userAgent != null) {
                    addInterceptor { chain ->
                        val reqBuilder = chain.request().newBuilder()
                        stream.referrer?.let  { reqBuilder.addHeader("Referer",    it) }
                        stream.userAgent?.let { reqBuilder.addHeader("User-Agent", it) }
                        chain.proceed(reqBuilder.build())
                    }
                }
            }
            .build()

        return OkHttpDataSource.Factory(okClient).apply {
            stream.referrer?.let  { setDefaultRequestProperties(mapOf("Referer" to it)) }
            stream.userAgent?.let { setUserAgent(it) }
        }
    }

    fun setQuality(quality: StreamQuality) {
        trackSelector?.let { ts ->
            val p = ts.buildUponParameters()
            when (quality) {
                StreamQuality.P1080 -> p.setMaxVideoSize(1920, 1080)
                StreamQuality.P720  -> p.setMaxVideoSize(1280, 720)
                StreamQuality.P480  -> p.setMaxVideoSize(854, 480)
                StreamQuality.P360  -> p.setMaxVideoSize(640, 360)
                StreamQuality.AUTO  -> p.clearVideoSizeConstraints()
            }
            ts.parameters = p.build()
        }
    }

    fun setAudioLanguage(langCode: String) {
        trackSelector?.let { ts ->
            ts.parameters = ts.buildUponParameters()
                .setPreferredAudioLanguage(langCode)
                .build()
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            val playerState = when (state) {
                Player.STATE_IDLE      -> PlayerState.IDLE
                Player.STATE_BUFFERING -> PlayerState.BUFFERING
                Player.STATE_READY     -> if (exoPlayer?.playWhenReady == true) PlayerState.PLAYING else PlayerState.PAUSED
                Player.STATE_ENDED     -> PlayerState.IDLE
                else                   -> PlayerState.IDLE
            }
            _playerInfo.value = _playerInfo.value.copy(state = playerState)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playerInfo.value = _playerInfo.value.copy(isPlaying = isPlaying)
        }

        override fun onVideoSizeChanged(videoSize: VideoSize) { /* track quality updates */ }

        override fun onPlayerError(error: PlaybackException) {
            _playerInfo.value = _playerInfo.value.copy(
                state = PlayerState.ERROR,
                errorMessage = error.localizedMessage
            )
        }
    }

    fun release() {
        exoPlayer?.apply {
            removeListener(playerListener)
            release()
        }
        exoPlayer = null
        trackSelector = null
    }
}

// ─── Renderer factory helpers ──────────────────────────────────────────────

object MediaPlayer {
    @UnstableApi
    fun buildCustomRenderersFactory(
        context: Context,
        enableHardware: Boolean,
        useFfmpeg: Boolean = true
    ): androidx.media3.exoplayer.DefaultRenderersFactory {
        return androidx.media3.exoplayer.DefaultRenderersFactory(context).apply {
            setExtensionRendererMode(
                if (useFfmpeg)
                    androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                else if (enableHardware)
                    androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON
                else
                    androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF
            )
        }
    }

    @UnstableApi
    fun buildDefaultRenderersFactory(
        context: Context,
        enableHardware: Boolean
    ): androidx.media3.exoplayer.DefaultRenderersFactory {
        return androidx.media3.exoplayer.DefaultRenderersFactory(context).apply {
            setExtensionRendererMode(
                if (enableHardware)
                    androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON
                else
                    androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
            )
        }
    }
}
