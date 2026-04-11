package com.streamvault.app.ui.screens.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamvault.app.data.model.AccentColor
import com.streamvault.app.data.model.AppTheme

// ─── Color System ──────────────────────────────────────────────────────────

object StreamVaultColors {

    // Dark theme
    val Background = Color(0xFF080C12)
    val Surface = Color(0xFF0E1420)
    val SurfaceVariant = Color(0xFF141C2C)
    val SurfaceCard = Color(0xFF111827)
    val CardBorder = Color(0xFF1E293B)
    val Overlay = Color(0xB3000000)

    // Text
    val TextPrimary = Color(0xFFF1F5F9)
    val TextSecondary = Color(0xFF94A3B8)
    val TextMuted = Color(0xFF475569)
    val TextOnAccent = Color(0xFF0A0E17)

    // Accents
    val CyanPrimary = Color(0xFF00E5FF)
    val CyanSecondary = Color(0xFF006675)
    val CyanGlow = Color(0x3300E5FF)

    val OrangePrimary = Color(0xFFFF6D00)
    val PinkPrimary = Color(0xFFE91E8C)
    val GreenPrimary = Color(0xFF00E676)
    val GoldPrimary = Color(0xFFFFD600)

    // Status
    val Success = Color(0xFF22C55E)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFEF4444)
    val Live = Color(0xFFFF3B30)

    // Special
    val Gradient1 = Color(0xFF00E5FF)
    val Gradient2 = Color(0xFF0080FF)
    val FocusBorder = Color(0xFF00E5FF)
    val FocusGlow = Color(0x5500E5FF)

    // AMOLED
    val AmoledBackground = Color(0xFF000000)
    val AmoledSurface = Color(0xFF0A0A0A)

    // Midnight Blue
    val MidnightBackground = Color(0xFF030B1A)
    val MidnightSurface = Color(0xFF071428)

    // Forest
    val ForestBackground = Color(0xFF040D08)
    val ForestSurface = Color(0xFF08180C)
    val ForestAccent = Color(0xFF00E676)

    fun accentPrimary(color: AccentColor) = when (color) {
        AccentColor.CYAN -> CyanPrimary
        AccentColor.ORANGE -> OrangePrimary
        AccentColor.PINK -> PinkPrimary
        AccentColor.GREEN -> GreenPrimary
        AccentColor.GOLD -> GoldPrimary
    }

    fun backgrounds(theme: AppTheme) = when (theme) {
        AppTheme.DARK -> Background to Surface
        AppTheme.AMOLED -> AmoledBackground to AmoledSurface
        AppTheme.MIDNIGHT_BLUE -> MidnightBackground to MidnightSurface
        AppTheme.FOREST -> ForestBackground to ForestSurface
    }
}

// ─── Dimensions ────────────────────────────────────────────────────────────

object Dimens {
    val ScreenPadding = 48.dp
    val CardRadius = 12.dp
    val CardElevation = 8.dp
    val ItemSpacing = 16.dp
    val RowSpacing = 24.dp
    val FocusBorderWidth = 2.dp
    val ChannelCardWidth = 180.dp
    val ChannelCardHeight = 120.dp
    val CategoryChipHeight = 40.dp
    val PlayerControlsHeight = 80.dp
    val SideNavWidth = 240.dp
    val SideNavCollapsedWidth = 72.dp
    val LogoSize = 48.dp
    val CountryFlagSize = 28.dp
}

// ─── Typography Scale ──────────────────────────────────────────────────────

object TextStyles {
    val DisplayLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 42.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = (-1).sp,
        lineHeight = 48.sp
    )
    val DisplayMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp
    )
    val HeadlineLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp
    )
    val HeadlineMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.1.sp
    )
    val TitleLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp
    )
    val TitleMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.15.sp
    )
    val BodyLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.5.sp
    )
    val BodyMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.25.sp
    )
    val LabelLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp
    )
    val LabelSmall = androidx.compose.ui.text.TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp
    )
    val Caption = androidx.compose.ui.text.TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.4.sp
    )
}

// ─── Category Icon Mapping ─────────────────────────────────────────────────

object CategoryIcons {
    fun getEmoji(categoryId: String): String = when (categoryId) {
        "news" -> "📰"
        "sports" -> "⚽"
        "movies" -> "🎬"
        "entertainment" -> "🎭"
        "music" -> "🎵"
        "kids" -> "🧸"
        "documentary" -> "🎥"
        "cooking" -> "🍳"
        "travel" -> "✈️"
        "science" -> "🔬"
        "business" -> "💼"
        "religion" -> "🙏"
        "weather" -> "🌤️"
        "general" -> "📺"
        "animated" -> "🎨"
        "classic" -> "📽️"
        "comedy" -> "😄"
        "education" -> "📚"
        "family" -> "👨‍👩‍👧"
        "legislative" -> "⚖️"
        "outdoor" -> "🌿"
        "series" -> "📀"
        "shop" -> "🛒"
        "auto" -> "🚗"
        "fashion" -> "👗"
        "fitness" -> "💪"
        "health" -> "❤️"
        "gaming" -> "🎮"
        "horror" -> "👻"
        "quiz" -> "❓"
        "relax" -> "🧘"
        "surveillance" -> "📹"
        "teleshopping" -> "🛍️"
        "adult" -> "🔞"
        else -> "📺"
    }

    fun getColor(categoryId: String): Color = when (categoryId) {
        "news" -> Color(0xFF3B82F6)
        "sports" -> Color(0xFF22C55E)
        "movies" -> Color(0xFFEF4444)
        "entertainment" -> Color(0xFFA855F7)
        "music" -> Color(0xFFEC4899)
        "kids" -> Color(0xFFF59E0B)
        "documentary" -> Color(0xFF0EA5E9)
        "cooking" -> Color(0xFFFF6D00)
        "travel" -> Color(0xFF14B8A6)
        else -> Color(0xFF64748B)
    }
}
