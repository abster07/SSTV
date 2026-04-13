package com.streamvault.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.streamvault.app.data.model.Channel
import com.streamvault.app.data.model.Category
import com.streamvault.app.ui.screens.theme.*

// ─── Focusable Card ────────────────────────────────────────────────────────

@Composable
fun FocusableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = StreamVaultColors.CyanPrimary,
    content: @Composable BoxScope.() -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.06f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )
    val borderAlpha by animateFloatAsState(
        targetValue = if (isFocused) 1f else 0f,
        animationSpec = tween(200),
        label = "border_alpha"
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.4f else 0f,
        animationSpec = tween(300),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .drawBehind {
                if (glowAlpha > 0f) {
                    drawCircle(
                        color = accentColor.copy(alpha = glowAlpha * 0.3f),
                        radius = size.maxDimension * 0.6f,
                        center = center
                    )
                }
            }
            .clip(RoundedCornerShape(Dimens.CardRadius))
            .border(
                width = Dimens.FocusBorderWidth,
                color = accentColor.copy(alpha = borderAlpha),
                shape = RoundedCornerShape(Dimens.CardRadius)
            )
            .background(StreamVaultColors.SurfaceCard)
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(onClick = onClick),
        content = content
    )
}

// ─── Channel Card ──────────────────────────────────────────────────────────

@Composable
fun ChannelCard(
    channel: Channel,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = StreamVaultColors.CyanPrimary
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        if (isFocused) 1.05f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "ch_scale"
    )

    Box(
        modifier = modifier
            .width(Dimens.ChannelCardWidth)
            .height(Dimens.ChannelCardHeight)
            .scale(scale)
            .clip(RoundedCornerShape(Dimens.CardRadius))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) accentColor else StreamVaultColors.CardBorder,
                shape = RoundedCornerShape(Dimens.CardRadius)
            )
            .background(
                Brush.verticalGradient(
                    listOf(StreamVaultColors.SurfaceCard, StreamVaultColors.Surface)
                )
            )
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(onClick = onClick)
    ) {
        // Background logo blur
        if (channel.logoUrl != null) {
            AsyncImage(
                model = channel.logoUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(20.dp)
                    .graphicsLayer { alpha = 0.15f },
                contentScale = ContentScale.Crop
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(StreamVaultColors.SurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (channel.logoUrl != null) {
                    AsyncImage(
                        model = channel.logoUrl,
                        contentDescription = channel.name,
                        modifier = Modifier.fillMaxSize().padding(4.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = channel.name.take(2).uppercase(),
                        style = TextStyles.HeadlineMedium,
                        color = accentColor
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = channel.name,
                    style = TextStyles.LabelLarge,
                    color = StreamVaultColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = channel.countryFlag, style = TextStyles.Caption)
                    Text(
                        text = channel.country,
                        style = TextStyles.Caption,
                        color = StreamVaultColors.TextMuted
                    )
                    if (channel.streams.isNotEmpty()) {
                        LiveBadge()
                    }
                }
            }
        }

        if (isFocused) {
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(28.dp)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color(0xFFEF4444) else StreamVaultColors.TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ─── Live Badge ────────────────────────────────────────────────────────────

@Composable
fun LiveBadge() {
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_alpha"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(StreamVaultColors.Live.copy(alpha = alpha))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(text = "LIVE", style = TextStyles.LabelSmall, color = Color.White)
    }
}

// ─── Category Chip ─────────────────────────────────────────────────────────

@Composable
fun CategoryChip(
    category: Category,
    selected: Boolean,
    onClick: () -> Unit,
    accentColor: Color = StreamVaultColors.CyanPrimary
) {
    var isFocused by remember { mutableStateOf(false) }
    val active = selected || isFocused

    Box(
        modifier = Modifier
            .height(Dimens.CategoryChipHeight)
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (active)
                    Brush.horizontalGradient(listOf(accentColor, accentColor.copy(0.7f)))
                else
                    Brush.horizontalGradient(
                        listOf(StreamVaultColors.SurfaceVariant, StreamVaultColors.SurfaceVariant)
                    )
            )
            .border(
                1.dp,
                if (active) accentColor else StreamVaultColors.CardBorder,
                RoundedCornerShape(20.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = CategoryIcons.getEmoji(category.id), style = TextStyles.BodyMedium)
            Text(
                text = category.name,
                style = TextStyles.LabelLarge,
                color = if (active) StreamVaultColors.TextOnAccent else StreamVaultColors.TextSecondary
            )
        }
    }
}

// ─── Section Header ────────────────────────────────────────────────────────

@Composable
fun SectionHeader(
    title: String,
    accentColor: Color = StreamVaultColors.CyanPrimary,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(accentColor)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = title,
                    style = TextStyles.HeadlineMedium,
                    color = StreamVaultColors.TextPrimary
                )
            }
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = TextStyles.BodyMedium,
                    color = StreamVaultColors.TextMuted,
                    modifier = Modifier.padding(start = 13.dp)
                )
            }
        }
        action?.invoke()
    }
}

// ─── Loading Spinner ───────────────────────────────────────────────────────

@Composable
fun StreamVaultLoader(
    accentColor: Color = StreamVaultColors.CyanPrimary,
    message: String = "Loading streams..."
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "loader")
        val rotation by infiniteTransition.animateFloat(
            0f, 360f,
            infiniteRepeatable(tween(1200, easing = LinearEasing)),
            label = "rotation"
        )

        Box(
            modifier = Modifier
                .size(64.dp)
                .drawBehind {
                    drawArc(
                        color = accentColor.copy(alpha = 0.2f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(4.dp.toPx())
                    )
                    drawArc(
                        brush = Brush.sweepGradient(listOf(Color.Transparent, accentColor)),
                        startAngle = rotation,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = message,
            style = TextStyles.BodyMedium,
            color = StreamVaultColors.TextSecondary
        )
    }
}

// ─── Error State ───────────────────────────────────────────────────────────

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    accentColor: Color = StreamVaultColors.CyanPrimary
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("⚠️", style = TextStyles.DisplayLarge)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Something went wrong",
            style = TextStyles.HeadlineLarge,
            color = StreamVaultColors.TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = TextStyles.BodyMedium,
            color = StreamVaultColors.TextSecondary
        )
        Spacer(Modifier.height(24.dp))
        FocusableCard(
            onClick = onRetry,
            accentColor = accentColor,
            modifier = Modifier.padding(horizontal = 48.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, tint = accentColor)
                Text("Retry", style = TextStyles.LabelLarge, color = StreamVaultColors.TextPrimary)
            }
        }
    }
}

// ─── Quality Badge ─────────────────────────────────────────────────────────

@Composable
fun QualityBadge(quality: String?) {
    if (quality == null) return
    val color = when {
        quality.contains("1080", true) -> Color(0xFFFFD600)
        quality.contains("720",  true) -> Color(0xFF00E5FF)
        quality.contains("480",  true) -> Color(0xFF22C55E)
        else                           -> Color(0xFF64748B)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, color.copy(0.6f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = quality.uppercase(), style = TextStyles.LabelSmall, color = color)
    }
}

// ─── Side Navigation Item ──────────────────────────────────────────────────

@Composable
fun NavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    expanded: Boolean,
    onClick: () -> Unit,
    accentColor: Color = StreamVaultColors.CyanPrimary
) {
    var isFocused by remember { mutableStateOf(false) }
    val active = selected || isFocused

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) accentColor.copy(0.15f)
                else if (isFocused) StreamVaultColors.SurfaceVariant
                else Color.Transparent
            )
            .border(
                width = if (active) 1.dp else 0.dp,
                color = if (active) accentColor.copy(0.4f) else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) accentColor else StreamVaultColors.TextMuted,
                modifier = Modifier.size(22.dp)
            )
            if (expanded) {
                Text(
                    text = label,
                    style = TextStyles.TitleMedium,
                    color = if (active) StreamVaultColors.TextPrimary else StreamVaultColors.TextSecondary,
                    maxLines = 1
                )
            }
        }

        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(3.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(0.dp, 2.dp, 2.dp, 0.dp))
                    .background(accentColor)
            )
        }
    }
}

// ─── Search Bar ────────────────────────────────────────────────────────────

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = StreamVaultColors.CyanPrimary
) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(StreamVaultColors.SurfaceVariant)
            .border(
                1.dp,
                if (isFocused) accentColor else StreamVaultColors.CardBorder,
                RoundedCornerShape(24.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Filled.Search,
                contentDescription = "Search",
                tint = if (isFocused) accentColor else StreamVaultColors.TextMuted,
                modifier = Modifier.size(20.dp)
            )
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { isFocused = it.isFocused },
                textStyle = TextStyles.BodyLarge.copy(color = StreamVaultColors.TextPrimary),
                cursorBrush = SolidColor(accentColor),
                decorationBox = { innerTextField: @Composable () -> Unit ->
                    if (query.isEmpty()) {
                        Text(
                            "Search channels, countries...",
                            style = TextStyles.BodyLarge,
                            color = StreamVaultColors.TextMuted
                        )
                    }
                    innerTextField()
                }
            )
            if (query.isNotEmpty()) {
                Icon(
                    Icons.Filled.Clear,
                    contentDescription = "Clear",
                    tint = StreamVaultColors.TextMuted,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onQueryChange("") }
                )
            }
        }
    }
}
