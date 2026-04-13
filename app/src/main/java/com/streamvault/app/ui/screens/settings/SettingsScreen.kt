package com.streamvault.app.ui.screens.settings

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamvault.app.data.model.*
import com.streamvault.app.ui.components.SectionHeader
import com.streamvault.app.ui.screens.theme.*

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val accentColor = Color(settings.accentColor.hex)
    val (bgColor, surfaceColor) = StreamVaultColors.backgrounds(settings.theme)

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // ── Left: Settings Nav ─────────────────────────────────────────
        var selectedSection by remember { mutableStateOf(0) }
        val sections = listOf(
            "Appearance" to Icons.Filled.Palette,
            "Playback" to Icons.Filled.PlayCircle,
            "Audio & Video" to Icons.Filled.Tune,
            "Language" to Icons.Filled.Language,
            "Parental" to Icons.Filled.Lock,
            "Recommendations" to Icons.Filled.Recommend,
            "About" to Icons.Filled.Info
        )

        Column(
            modifier = Modifier
                .width(260.dp)
                .fillMaxHeight()
                .background(surfaceColor)
                .padding(vertical = 32.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onBack)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Filled.ArrowBack, null, tint = accentColor, modifier = Modifier.size(20.dp))
                Text("Back", style = TextStyles.TitleMedium, color = accentColor)
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Settings",
                style = TextStyles.DisplayMedium,
                color = StreamVaultColors.TextPrimary,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Spacer(Modifier.height(24.dp))

            sections.forEachIndexed { index, (label, icon) ->
                SettingsNavItem(
                    icon = icon,
                    label = label,
                    selected = selectedSection == index,
                    onClick = { selectedSection = index },
                    accentColor = accentColor
                )
            }
        }

        // ── Right: Settings Content ────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(48.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                item {
                    when (selectedSection) {
                        0 -> AppearanceSection(settings, accentColor, viewModel)
                        1 -> PlaybackSection(settings, accentColor, viewModel)
                        2 -> AudioVideoSection(settings, accentColor, viewModel)
                        3 -> LanguageSection(settings, accentColor, viewModel)
                        4 -> ParentalSection(settings, accentColor, viewModel)
                        5 -> RecommendationsSection(settings, accentColor, viewModel)
                        6 -> AboutSection(accentColor)  
                    }
                }
            }
        }
    }
}

// ─── Appearance Section ────────────────────────────────────────────────────

@Composable
private fun AppearanceSection(
    settings: AppSettings,
    accentColor: Color,
    viewModel: SettingsViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
        SectionHeader("Appearance", accentColor)

        // Theme selector
        SettingsGroup(title = "Theme", accentColor = accentColor) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppTheme.values().forEach { theme ->
                    ThemeCard(
                        theme = theme,
                        selected = settings.theme == theme,
                        accentColor = accentColor,
                        onClick = { viewModel.setTheme(theme) }
                    )
                }
            }
        }

        // Accent color selector
        SettingsGroup(title = "Accent Color", accentColor = accentColor) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AccentColor.values().forEach { color ->
                    AccentColorDot(
                        color = Color(color.hex),
                        name = color.name.lowercase().replaceFirstChar { it.uppercase() },
                        selected = settings.accentColor == color,
                        onClick = { viewModel.setAccentColor(color) }
                    )
                }
            }
        }

        // UI toggles
        SettingsGroup(title = "UI Options", accentColor = accentColor) {
            SettingsToggle(
                label = "Show Clock",
                description = "Display current time in top bar",
                icon = Icons.Filled.AccessTime,
                checked = settings.clockVisible,
                onCheckedChange = viewModel::setClockVisible,
                accentColor = accentColor
            )
            SettingsDivider()
            SettingsToggle(
                label = "EPG Guide",
                description = "Show program guide information",
                icon = Icons.Filled.DateRange,
                checked = settings.epgEnabled,
                onCheckedChange = viewModel::setEpg,
                accentColor = accentColor
            )
        }
    }
}

// ─── Playback Section ──────────────────────────────────────────────────────

@Composable
private fun PlaybackSection(
    settings: AppSettings,
    accentColor: Color,
    viewModel: SettingsViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
        SectionHeader("Playback", accentColor)

        SettingsGroup(title = "Stream Quality", accentColor = accentColor) {
            StreamQuality.values().forEach { quality ->
                SettingsRadioItem(
                    label = quality.displayLabel(),
                    description = quality.description(),
                    selected = settings.defaultQuality == quality,
                    onClick = { viewModel.setQuality(quality) },
                    accentColor = accentColor
                )
                if (quality != StreamQuality.values().last()) SettingsDivider()
            }
        }

        SettingsGroup(title = "Buffer Size", accentColor = accentColor) {
            val bufferOptions = listOf(2000, 5000, 10000, 15000, 30000)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                bufferOptions.forEach { ms ->
                    val selected = settings.bufferSizeMs == ms
                    var isFocused by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selected) accentColor.copy(0.15f)
                                else StreamVaultColors.SurfaceVariant
                            )
                            .border(
                                1.dp,
                                if (selected || isFocused) accentColor else StreamVaultColors.CardBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .onFocusChanged { isFocused = it.isFocused }
                            .clickable { viewModel.setBufferSize(ms) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${ms / 1000}s",
                            style = TextStyles.LabelLarge,
                            color = if (selected) accentColor else StreamVaultColors.TextSecondary
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Current: ${settings.bufferSizeMs / 1000}s — Higher values reduce buffering on slow connections",
                style = TextStyles.Caption,
                color = StreamVaultColors.TextMuted
            )
        }

        SettingsGroup(title = "Behaviour", accentColor = accentColor) {
            SettingsToggle(
                label = "Auto-play Next",
                description = "Automatically play the next available stream on error",
                icon = Icons.Filled.SkipNext,
                checked = settings.autoPlayNext,
                onCheckedChange = viewModel::setAutoPlayNext,
                accentColor = accentColor
            )
        }
    }
}

// ─── Audio & Video Section ─────────────────────────────────────────────────

@Composable
private fun AudioVideoSection(
    settings: AppSettings,
    accentColor: Color,
    viewModel: SettingsViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
        SectionHeader("Audio & Video", accentColor)

        SettingsGroup(title = "Decoder", accentColor = accentColor) {
            SettingsToggle(
                label = "Hardware Decoding",
                description = "Use GPU for video decoding — better performance on supported devices",
                icon = Icons.Filled.Memory,
                checked = settings.useHardwareDecoding,
                onCheckedChange = viewModel::setHardwareDecoding,
                accentColor = accentColor
            )
            SettingsDivider()
            SettingsToggle(
                label = "FFmpeg Audio Decoder",
                description = "Enables MP2, AC3, EAC3, Opus, Vorbis codec support via FFmpeg extension",
                icon = Icons.Filled.GraphicEq,
                checked = settings.useFfmpegDecoder,
                onCheckedChange = viewModel::setFfmpeg,
                accentColor = accentColor
            )
        }

        SettingsGroup(title = "Subtitles", accentColor = accentColor) {
            SettingsToggle(
                label = "Enable Subtitles",
                description = "Show subtitles when available in the stream",
                icon = Icons.Filled.Subtitles,
                checked = settings.subtitleEnabled,
                onCheckedChange = viewModel::setSubtitle,
                accentColor = accentColor
            )
        }

        SettingsGroup(title = "Content Filter", accentColor = accentColor) {
            SettingsToggle(
                label = "Show Adult Content",
                description = "Include NSFW channels in search and browse results",
                icon = Icons.Filled.VisibilityOff,
                checked = settings.showNsfw,
                onCheckedChange = viewModel::setShowNsfw,
                accentColor = accentColor
            )
        }
    }
}

// ─── Language Section ──────────────────────────────────────────────────────

@Composable
private fun LanguageSection(
    settings: AppSettings,
    accentColor: Color,
    viewModel: SettingsViewModel
) {
    val audioLanguages = listOf(
        "eng" to "English", "fra" to "French", "deu" to "German",
        "spa" to "Spanish", "ita" to "Italian", "por" to "Portuguese",
        "rus" to "Russian", "ara" to "Arabic", "zho" to "Chinese",
        "jpn" to "Japanese", "kor" to "Korean", "hin" to "Hindi",
        "tur" to "Turkish", "nld" to "Dutch", "pol" to "Polish"
    )

    val uiLanguages = listOf(
        "en" to "English", "fr" to "Français", "de" to "Deutsch",
        "es" to "Español", "it" to "Italiano", "pt" to "Português",
        "ru" to "Русский", "ar" to "العربية", "zh" to "中文",
        "ja" to "日本語", "ko" to "한국어"
    )

    Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
        SectionHeader("Language", accentColor)

        SettingsGroup(title = "Preferred Audio Language", accentColor = accentColor) {
            Text(
                "Automatically select audio track in this language when available",
                style = TextStyles.BodyMedium,
                color = StreamVaultColors.TextMuted
            )
            Spacer(Modifier.height(12.dp))
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(audioLanguages.size) { i ->
                    val (code, name) = audioLanguages[i]
                    val selected = settings.audioLanguage == code
                    var isFocused by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selected) accentColor.copy(0.15f) else StreamVaultColors.SurfaceVariant
                            )
                            .border(
                                1.dp,
                                if (selected || isFocused) accentColor else StreamVaultColors.CardBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .onFocusChanged { isFocused = it.isFocused }
                            .clickable { viewModel.setAudioLanguage(code) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            name,
                            style = TextStyles.LabelLarge,
                            color = if (selected) accentColor else StreamVaultColors.TextSecondary
                        )
                    }
                }
            }
        }

        SettingsGroup(title = "Interface Language", accentColor = accentColor) {
            Text(
                "Language for menus and UI elements",
                style = TextStyles.BodyMedium,
                color = StreamVaultColors.TextMuted
            )
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                uiLanguages.chunked(4).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { (code, name) ->
                            val selected = settings.uiLanguage == code
                            var isFocused by remember { mutableStateOf(false) }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (selected) accentColor.copy(0.15f) else StreamVaultColors.SurfaceVariant
                                    )
                                    .border(
                                        1.dp,
                                        if (selected || isFocused) accentColor else StreamVaultColors.CardBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .onFocusChanged { isFocused = it.isFocused }
                                    .clickable { viewModel.setUiLanguage(code) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    name,
                                    style = TextStyles.LabelLarge,
                                    color = if (selected) accentColor else StreamVaultColors.TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Parental Section ──────────────────────────────────────────────────────

@Composable
private fun ParentalSection(
    settings: AppSettings,
    accentColor: Color,
    viewModel: SettingsViewModel
) {
    var pinInput by remember { mutableStateOf("") }
    var showPinInput by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
        SectionHeader("Parental Controls", accentColor)

        SettingsGroup(title = "Access Control", accentColor = accentColor) {
            SettingsToggle(
                label = "Enable Parental Controls",
                description = "Require PIN to access adult content",
                icon = Icons.Filled.FamilyRestroom,
                checked = settings.parentalControlEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) showPinInput = true
                    else viewModel.setParentalControl(false, "")
                },
                accentColor = accentColor
            )

            if (showPinInput || settings.parentalControlEnabled) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Set 4-digit PIN",
                    style = TextStyles.TitleMedium,
                    color = StreamVaultColors.TextPrimary
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(4) { i ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(StreamVaultColors.SurfaceVariant)
                                .border(1.dp, accentColor.copy(0.4f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (i < pinInput.length) "●" else "",
                                style = TextStyles.HeadlineLarge,
                                color = accentColor
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                // Virtual PIN pad
                val digits = listOf("1","2","3","4","5","6","7","8","9","←","0","✓")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    digits.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { digit ->
                                var isFocused by remember { mutableStateOf(false) }
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isFocused) accentColor.copy(0.2f)
                                            else StreamVaultColors.SurfaceVariant
                                        )
                                        .border(
                                            1.dp,
                                            if (isFocused) accentColor else StreamVaultColors.CardBorder,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .onFocusChanged { isFocused = it.isFocused }
                                        .clickable {
                                            when (digit) {
                                                "←" -> if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1)
                                                "✓" -> if (pinInput.length == 4) {
                                                    viewModel.setParentalControl(true, pinInput)
                                                    showPinInput = false
                                                }
                                                else -> if (pinInput.length < 4) pinInput += digit
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        digit,
                                        style = TextStyles.HeadlineMedium,
                                        color = when (digit) {
                                            "✓" -> accentColor
                                            "←" -> StreamVaultColors.Error
                                            else -> StreamVaultColors.TextPrimary
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── About Section ─────────────────────────────────────────────────────────

@Composable
private fun AboutSection(accentColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
        SectionHeader("About StreamVault", accentColor)

        SettingsGroup(title = "App Info", accentColor = accentColor) {
            AboutRow("Version", "1.0.0", Icons.Filled.NewReleases)
            SettingsDivider()
            AboutRow("Data Source", "iptv-org.github.io/api", Icons.Filled.Storage)
            SettingsDivider()
            AboutRow("Video Engine", "Media3 ExoPlayer + FFmpeg", Icons.Filled.VideoSettings)
            SettingsDivider()
            AboutRow("Total Channels", "10,000+", Icons.Filled.Tv)
            SettingsDivider()
            AboutRow("Countries", "200+", Icons.Filled.Public)
        }

        SettingsGroup(title = "Technology Stack", accentColor = accentColor) {
            val stack = listOf(
                "Jetpack Compose" to "UI Framework",
                "Media3 ExoPlayer" to "HLS / DASH / Progressive",
                "FFmpeg Extension" to "MP2, AC3, Opus, Vorbis",
                "Hilt" to "Dependency Injection",
                "Room" to "Local Database",
                "Retrofit + OkHttp" to "Network Layer",
                "Coil" to "Image Loading",
                "DataStore" to "Preferences"
            )
            stack.forEach { (tech, desc) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(tech, style = TextStyles.TitleMedium, color = accentColor)
                    Text(desc, style = TextStyles.BodyMedium, color = StreamVaultColors.TextSecondary)
                }
                if (tech != stack.last().first) SettingsDivider()
            }
        }
    }
}

// ─── Shared Components ─────────────────────────────────────────────────────

@Composable
private fun SettingsGroup(
    title: String,
    accentColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            title,
            style = TextStyles.LabelLarge,
            color = accentColor,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(StreamVaultColors.SurfaceCard)
                .border(1.dp, StreamVaultColors.CardBorder, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsToggle(
    label: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(icon, null, tint = if (checked) accentColor else StreamVaultColors.TextMuted, modifier = Modifier.size(22.dp))
            Column {
                Text(label, style = TextStyles.TitleMedium, color = StreamVaultColors.TextPrimary)
                Text(description, style = TextStyles.Caption, color = StreamVaultColors.TextMuted)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = StreamVaultColors.TextOnAccent,
                checkedTrackColor = accentColor,
                uncheckedTrackColor = StreamVaultColors.SurfaceVariant
            )
        )
    }
}

@Composable
private fun SettingsRadioItem(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    accentColor: Color
) {
    var isFocused by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isFocused) StreamVaultColors.SurfaceVariant else Color.Transparent)
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = TextStyles.TitleMedium, color = if (selected) accentColor else StreamVaultColors.TextPrimary)
            Text(description, style = TextStyles.Caption, color = StreamVaultColors.TextMuted)
        }
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = accentColor)
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        color = StreamVaultColors.CardBorder,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun SettingsNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    accentColor: Color
) {
    var isFocused by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    selected -> accentColor.copy(0.15f)
                    isFocused -> StreamVaultColors.SurfaceVariant
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) accentColor.copy(0.4f) else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon, null,
            tint = if (selected || isFocused) accentColor else StreamVaultColors.TextMuted,
            modifier = Modifier.size(20.dp)
        )
        Text(
            label,
            style = TextStyles.TitleMedium,
            color = if (selected) StreamVaultColors.TextPrimary else StreamVaultColors.TextSecondary
        )
    }
}

@Composable
private fun ThemeCard(
    theme: AppTheme,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val (bg, surface) = StreamVaultColors.backgrounds(theme)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp, 50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(bg)
                .border(
                    width = if (selected || isFocused) 2.dp else 1.dp,
                    color = if (selected) accentColor else if (isFocused) accentColor.copy(0.5f) else StreamVaultColors.CardBorder,
                    shape = RoundedCornerShape(8.dp)
                )
                .onFocusChanged { isFocused = it.isFocused }
                .clickable(onClick = onClick)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .fillMaxHeight()
                    .background(surface)
            )
            if (selected) {
                Icon(
                    Icons.Filled.Check,
                    null,
                    tint = accentColor,
                    modifier = Modifier.align(Alignment.Center).size(20.dp)
                )
            }
        }
        Text(
            theme.name.lowercase().replaceFirstChar { it.uppercase() },
            style = TextStyles.Caption,
            color = if (selected) accentColor else StreamVaultColors.TextMuted
        )
    }
}

@Composable
private fun AccentColorDot(
    color: Color,
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color)
                .border(
                    width = if (selected || isFocused) 3.dp else 0.dp,
                    color = if (selected) Color.White else if (isFocused) Color.White.copy(0.5f) else Color.Transparent,
                    shape = CircleShape
                )
                .onFocusChanged { isFocused = it.isFocused }
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(Icons.Filled.Check, null, tint = Color.Black, modifier = Modifier.size(20.dp))
            }
        }
        Text(name, style = TextStyles.Caption, color = StreamVaultColors.TextMuted)
    }
}

@Composable
private fun AboutRow(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = StreamVaultColors.TextMuted, modifier = Modifier.size(18.dp))
        Text(label, style = TextStyles.TitleMedium, color = StreamVaultColors.TextSecondary, modifier = Modifier.weight(1f))
        Text(value, style = TextStyles.TitleMedium, color = StreamVaultColors.TextPrimary)
    }
}

private fun StreamQuality.displayLabel() = when (this) {
    StreamQuality.AUTO -> "🔄 Auto"
    StreamQuality.P1080 -> "🥇 1080p Full HD"
    StreamQuality.P720 -> "🥈 720p HD"
    StreamQuality.P480 -> "🥉 480p SD"
    StreamQuality.P360 -> "📱 360p Low"
}

private fun StreamQuality.description() = when (this) {
    StreamQuality.AUTO -> "Adapts to your connection speed (recommended)"
    StreamQuality.P1080 -> "Best quality — requires fast connection (10+ Mbps)"
    StreamQuality.P720 -> "Great quality — suitable for most connections (5+ Mbps)"
    StreamQuality.P480 -> "Standard quality — works on slower connections (2+ Mbps)"
    StreamQuality.P360 -> "Lowest quality — minimal bandwidth usage"
}


@Composable
private fun RecommendationsSection(
    settings: AppSettings,
    accentColor: Color,
    viewModel: SettingsViewModel
) {
    val rec = settings.recommendationSettings
    val allCategories = listOf(
        "news","sports","movies","entertainment","music","kids",
        "documentary","cooking","travel","science","business","general"
    )

    Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
        SectionHeader("Recommendations", accentColor)

        // Master toggle
        SettingsGroup(title = "General", accentColor = accentColor) {
            SettingsToggle(
                label = "Enable Recommendations",
                description = "Show personalised channel suggestions on the home screen",
                icon = Icons.Filled.Recommend,
                checked = rec.enabled,
                onCheckedChange = { viewModel.setRecEnabled(it) },
                accentColor = accentColor
            )
        }

        AnimatedVisibility(visible = rec.enabled) {
            Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {

                // Signals
                SettingsGroup(title = "Based On", accentColor = accentColor) {
                    SettingsToggle(
                        label = "Watch History",
                        description = "Recommend channels similar to what you have watched",
                        icon = Icons.Filled.History,
                        checked = rec.basedOnHistory,
                        onCheckedChange = { viewModel.setRecHistory(it) },
                        accentColor = accentColor
                    )
                    SettingsDivider()
                    SettingsToggle(
                        label = "Favourites",
                        description = "Recommend channels similar to your saved favourites",
                        icon = Icons.Filled.Favorite,
                        checked = rec.basedOnFavorites,
                        onCheckedChange = { viewModel.setRecFavorites(it) },
                        accentColor = accentColor
                    )
                    SettingsDivider()
                    SettingsToggle(
                        label = "Exclude Adult Content",
                        description = "Never recommend NSFW channels regardless of history",
                        icon = Icons.Filled.VisibilityOff,
                        checked = rec.excludeNsfw,
                        onCheckedChange = { viewModel.setRecExcludeNsfw(it) },
                        accentColor = accentColor
                    )
                }

                // Preferred categories / tags
                SettingsGroup(title = "Preferred Categories", accentColor = accentColor) {
                    Text(
                        "Boost channels in these categories regardless of watch history",
                        style = TextStyles.BodyMedium,
                        color = StreamVaultColors.TextMuted
                    )
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        allCategories.chunked(4).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { catId ->
                                    val selected = rec.preferredTags.contains(catId)
                                    var isFocused by remember { mutableStateOf(false) }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (selected) accentColor.copy(0.15f)
                                                else StreamVaultColors.SurfaceVariant
                                            )
                                            .border(
                                                1.dp,
                                                if (selected || isFocused) accentColor
                                                else StreamVaultColors.CardBorder,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .onFocusChanged { isFocused = it.isFocused }
                                            .clickable {
                                                val updated = if (selected)
                                                    rec.preferredTags - catId
                                                else
                                                    rec.preferredTags + catId
                                                viewModel.setRecTags(updated)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                CategoryIcons.getEmoji(catId),
                                                style = TextStyles.BodyMedium
                                            )
                                            Text(
                                                catId.replaceFirstChar { it.uppercase() },
                                                style = TextStyles.LabelLarge,
                                                color = if (selected) accentColor
                                                        else StreamVaultColors.TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Preferred continents
                SettingsGroup(title = "Preferred Continents", accentColor = accentColor) {
                    Text(
                        "Boost channels from these regions",
                        style = TextStyles.BodyMedium,
                        color = StreamVaultColors.TextMuted
                    )
                    Spacer(Modifier.height(12.dp))
                    val continentEmojis = mapOf(
                        "EU" to "🇪🇺", "AS" to "🌏", "NA" to "🌎",
                        "SA" to "🌎", "AF" to "🌍", "ME" to "🕌", "OC" to "🌊"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ContinentMap.continents.forEach { (code, name) ->
                            val selected = rec.preferredContinents.contains(code)
                            var isFocused by remember { mutableStateOf(false) }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (selected) accentColor.copy(0.15f)
                                        else StreamVaultColors.SurfaceVariant
                                    )
                                    .border(
                                        1.dp,
                                        if (selected || isFocused) accentColor
                                        else StreamVaultColors.CardBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .onFocusChanged { isFocused = it.isFocused }
                                    .clickable {
                                        val updated = if (selected)
                                            rec.preferredContinents - code
                                        else
                                            rec.preferredContinents + code
                                        viewModel.setRecContinents(updated)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        continentEmojis[code] ?: "🌐",
                                        style = TextStyles.BodyMedium
                                    )
                                    Text(
                                        name,
                                        style = TextStyles.LabelSmall,
                                        color = if (selected) accentColor
                                                else StreamVaultColors.TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Max results slider
                SettingsGroup(title = "Result Count", accentColor = accentColor) {
                    Text(
                        "Maximum number of recommended channels to show",
                        style = TextStyles.BodyMedium,
                        color = StreamVaultColors.TextMuted
                    )
                    Spacer(Modifier.height(12.dp))
                    val options = listOf(10, 15, 20, 30, 50)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        options.forEach { n ->
                            val selected = rec.maxResults == n
                            var isFocused by remember { mutableStateOf(false) }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (selected) accentColor.copy(0.15f)
                                        else StreamVaultColors.SurfaceVariant
                                    )
                                    .border(
                                        1.dp,
                                        if (selected || isFocused) accentColor
                                        else StreamVaultColors.CardBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .onFocusChanged { isFocused = it.isFocused }
                                    .clickable { viewModel.setRecMaxResults(n) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$n",
                                    style = TextStyles.LabelLarge,
                                    color = if (selected) accentColor
                                            else StreamVaultColors.TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}