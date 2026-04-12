package com.streamvault.app.ui.screens.player

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.streamvault.app.data.model.*
import com.streamvault.app.player.PlayerState
import com.streamvault.app.ui.components.*
import com.streamvault.app.ui.screens.theme.*

@UnstableApi
@Composable
fun PlayerScreen(
    channelId: String,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val accentColor = Color(settings.accentColor.hex)
    val (bgColor) = StreamVaultColors.backgrounds(settings.theme)

    LaunchedEffect(channelId) {
        viewModel.initPlayer(channelId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { viewModel.showControls() }
    ) {
        // ── Video Surface ──────────────────────────────────────────────
        viewModel.player?.let { player ->
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        this.player = player
                        useController = false
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // ── Loading Overlay ────────────────────────────────────────────
        if (uiState.playerInfo.state == PlayerState.LOADING ||
            uiState.playerInfo.state == PlayerState.BUFFERING) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    StreamVaultLoader(accentColor, "Connecting to stream...")
                }
            }
        }

        // ── Error Overlay ──────────────────────────────────────────────
        if (uiState.errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.7f)),
                contentAlignment = Alignment.Center
            ) {
                ErrorState(
                    message = uiState.errorMessage!!,
                    onRetry = { viewModel.initPlayer(channelId) },
                    accentColor = accentColor
                )
            }
        }

        // ── Animated Controls Overlay ──────────────────────────────────
        AnimatedVisibility(
            visible = uiState.controlsVisible,
            enter = fadeIn(tween(300)) + slideInVertically(tween(300)),
            exit = fadeOut(tween(400)) + slideOutVertically(tween(400))
        ) {
            PlayerControls(
                uiState = uiState,
                settings = settings,
                accentColor = accentColor,
                onBack = onBack,
                onPlayPause = viewModel::togglePlayPause,
                onFavoriteToggle = viewModel::toggleFavorite,
                onToggleQualityMenu = viewModel::toggleQualityMenu,
                onToggleStreamMenu = viewModel::toggleStreamMenu,
                onStreamSelect = viewModel::selectStream,
                onQualitySelect = viewModel::setQuality
            )
        }

        // ── Sub-menus ──────────────────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.showStreamMenu,
            enter = fadeIn() + slideInHorizontally { it },
            exit = fadeOut() + slideOutHorizontally { it },
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            StreamMenuPanel(
                streams = uiState.availableStreams,
                currentStream = uiState.currentStream,
                accentColor = accentColor,
                onSelect = viewModel::selectStream,
                onDismiss = viewModel::toggleStreamMenu
            )
        }

        AnimatedVisibility(
            visible = uiState.showQualityMenu,
            enter = fadeIn() + slideInHorizontally { it },
            exit = fadeOut() + slideOutHorizontally { it },
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            QualityMenuPanel(
                currentQuality = settings.defaultQuality,
                accentColor = accentColor,
                onSelect = {
                    viewModel.setQuality(it)
                    viewModel.toggleQualityMenu()
                },
                onDismiss = viewModel::toggleQualityMenu
            )
        }
    }
}

@Composable
private fun PlayerControls(
    uiState: PlayerUiState,
    settings: AppSettings,
    accentColor: Color,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onToggleQualityMenu: () -> Unit,
    onToggleStreamMenu: () -> Unit,
    onStreamSelect: (Stream) -> Unit,
    onQualitySelect: (StreamQuality) -> Unit
) {
    val channel = uiState.channel
    Box(modifier = Modifier.fillMaxSize()) {
        // Top gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(0.8f), Color.Transparent)
                    )
                )
        )
        // Bottom gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(0.9f))
                    )
                )
        )

        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 48.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerIconButton(
                icon = Icons.Filled.ArrowBack,
                onClick = onBack,
                accentColor = accentColor
            )

            if (channel != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = channel.logoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(StreamVaultColors.SurfaceVariant)
                    )
                    Column {
                        Text(
                            channel.name,
                            style = TextStyles.TitleLarge,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(channel.countryFlag, style = TextStyles.Caption)
                            LiveBadge()
                            uiState.currentStream?.quality?.let { QualityBadge(it) }
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlayerIconButton(
                    icon = if (uiState.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    onClick = onFavoriteToggle,
                    accentColor = if (uiState.isFavorite) Color(0xFFEF4444) else accentColor
                )
            }
        }

        // Center Controls
        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (uiState.playerInfo.state) {
                PlayerState.PLAYING -> {
                    PlayerIconButton(
                        icon = Icons.Filled.Pause,
                        onClick = onPlayPause,
                        accentColor = accentColor,
                        size = 72.dp
                    )
                }
                PlayerState.PAUSED -> {
                    PlayerIconButton(
                        icon = Icons.Filled.PlayArrow,
                        onClick = onPlayPause,
                        accentColor = accentColor,
                        size = 72.dp
                    )
                }
                else -> {}
            }
        }

        // Bottom Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 48.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stream info
            Column {
                Text(
                    uiState.currentStream?.title ?: "",
                    style = TextStyles.TitleMedium,
                    color = Color.White
                )
                Text(
                    "${uiState.availableStreams.size} streams available",
                    style = TextStyles.Caption,
                    color = Color.White.copy(0.6f)
                )
            }

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PlayerActionButton(
                    icon = Icons.Filled.Hd,
                    label = "Quality",
                    onClick = onToggleQualityMenu,
                    accentColor = accentColor
                )
                PlayerActionButton(
                    icon = Icons.Filled.Stream,
                    label = "Streams",
                    onClick = onToggleStreamMenu,
                    accentColor = accentColor
                )
            }
        }
    }
}

@Composable
private fun PlayerIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    accentColor: Color,
    size: androidx.compose.ui.unit.Dp = 48.dp
) {
    var isFocused by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size / 2))
            .background(
                if (isFocused) accentColor.copy(0.3f) else Color.White.copy(0.1f)
            )
            .border(
                1.dp,
                if (isFocused) accentColor else Color.White.copy(0.2f),
                RoundedCornerShape(size / 2)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isFocused) accentColor else Color.White,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

@Composable
private fun PlayerActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    accentColor: Color
) {
    var isFocused by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isFocused) accentColor.copy(0.2f) else Color.White.copy(0.1f)
            )
            .border(
                1.dp,
                if (isFocused) accentColor else Color.White.copy(0.15f),
                RoundedCornerShape(8.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isFocused) accentColor else Color.White,
            modifier = Modifier.size(18.dp)
        )
        Text(
            label,
            style = TextStyles.LabelLarge,
            color = if (isFocused) accentColor else Color.White
        )
    }
}

@Composable
private fun StreamMenuPanel(
    streams: List<Stream>,
    currentStream: Stream?,
    accentColor: Color,
    onSelect: (Stream) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(360.dp)
            .background(StreamVaultColors.Background.copy(0.95f))
            .padding(24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Available Streams", style = TextStyles.HeadlineMedium, color = StreamVaultColors.TextPrimary)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, null, tint = StreamVaultColors.TextSecondary)
                }
            }
            Spacer(Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(streams) { stream ->
                    val isSelected = stream.url == currentStream?.url
                    StreamItem(stream, isSelected, accentColor, onClick = { onSelect(stream) })
                }
            }
        }
    }
}

@Composable
private fun StreamItem(
    stream: Stream,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) accentColor.copy(0.15f)
                else if (isFocused) StreamVaultColors.SurfaceVariant
                else StreamVaultColors.SurfaceCard
            )
            .border(
                1.dp,
                if (isSelected) accentColor else StreamVaultColors.CardBorder,
                RoundedCornerShape(10.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stream.title,
                    style = TextStyles.TitleMedium,
                    color = if (isSelected) accentColor else StreamVaultColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                stream.quality?.let { QualityBadge(it) }
            }
            if (stream.label != null) {
                Text(stream.label, style = TextStyles.Caption, color = StreamVaultColors.Warning)
            }
        }
    }
}

@Composable
private fun QualityMenuPanel(
    currentQuality: StreamQuality,
    accentColor: Color,
    onSelect: (StreamQuality) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(StreamVaultColors.Background.copy(0.95f))
            .padding(24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Quality", style = TextStyles.HeadlineMedium, color = StreamVaultColors.TextPrimary)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, null, tint = StreamVaultColors.TextSecondary)
                }
            }
            Spacer(Modifier.height(16.dp))
            StreamQuality.values().forEach { quality ->
                val isSelected = quality == currentQuality
                var isFocused by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) accentColor.copy(0.15f)
                            else if (isFocused) StreamVaultColors.SurfaceVariant
                            else Color.Transparent
                        )
                        .onFocusChanged { isFocused = it.isFocused }
                        .clickable { onSelect(quality) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        quality.displayName(),
                        style = TextStyles.TitleMedium,
                        color = if (isSelected) accentColor else StreamVaultColors.TextPrimary
                    )
                    if (isSelected) {
                        Icon(Icons.Filled.Check, null, tint = accentColor, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

fun StreamQuality.displayName() = when (this) {
    StreamQuality.AUTO -> "Auto (Best)"
    StreamQuality.P1080 -> "1080p Full HD"
    StreamQuality.P720 -> "720p HD"
    StreamQuality.P480 -> "480p SD"
    StreamQuality.P360 -> "360p Low"
}