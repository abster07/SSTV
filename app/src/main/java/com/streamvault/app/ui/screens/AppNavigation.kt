package com.streamvault.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.streamvault.app.data.model.AppSettings
import com.streamvault.app.ui.components.NavItem
import com.streamvault.app.ui.screens.home.HomeScreen
import com.streamvault.app.ui.screens.home.HomeViewModel
import com.streamvault.app.ui.screens.player.PlayerScreen
import com.streamvault.app.ui.screens.settings.SettingsScreen
import com.streamvault.app.ui.screens.settings.SettingsViewModel
import com.streamvault.app.ui.screens.theme.*
import androidx.media3.common.util.UnstableApi

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Player : Screen("player/{channelId}") {
        fun withId(id: String) = "player/${java.net.URLEncoder.encode(id, "UTF-8")}"
    }
    object Favorites : Screen("favorites")
    object Settings : Screen("settings")
    object Search : Screen("search")
}

data class NavEntry(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

val navEntries = listOf(
    NavEntry(Screen.Home, "Home", Icons.Filled.Home),
    NavEntry(Screen.Search, "Search", Icons.Filled.Search),
    NavEntry(Screen.Favorites, "Favorites", Icons.Filled.Favorite),
    NavEntry(Screen.Settings, "Settings", Icons.Filled.Settings)
)

@UnstableApi
@Composable
fun AppNavigation(settings: AppSettings) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val accentColor = Color(settings.accentColor.hex)
    val (bgColor, surfaceColor) = StreamVaultColors.backgrounds(settings.theme)

    // Determine if side nav should be shown
    val showSideNav = currentRoute != null &&
            !currentRoute.startsWith("player")

    Row(modifier = Modifier.fillMaxSize()) {
        // ── Side Navigation ────────────────────────────────────────────
        AnimatedVisibility(
            visible = showSideNav,
            enter = slideInHorizontally { -it },
            exit = slideOutHorizontally { -it }
        ) {
            SideNavigation(
                currentRoute = currentRoute,
                accentColor = accentColor,
                surfaceColor = surfaceColor,
                onNavigate = { screen ->
                    navController.navigate(screen.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        // ── Main Content ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                enterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 10 } },
                exitTransition = { fadeOut(tween(200)) },
                popEnterTransition = { fadeIn(tween(300)) },
                popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(300)) { it / 10 } }
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        onChannelClick = { channel ->
                            navController.navigate(Screen.Player.withId(channel.id))
                        }
                    )
                }

                composable(Screen.Search.route) {
                    // Reuse HomeScreen in search mode
                    HomeScreen(
                        onChannelClick = { channel ->
                            navController.navigate(Screen.Player.withId(channel.id))
                        }
                    )
                }

                composable(Screen.Favorites.route) {
                    FavoritesScreen(
                        settings = settings,
                        onChannelClick = { channel ->
                            navController.navigate(Screen.Player.withId(channel.id))
                        }
                    )
                }

                composable(
                    route = Screen.Player.route,
                    arguments = listOf(navArgument("channelId") { type = NavType.StringType })
                ) { backStack ->
                    val channelId = backStack.arguments?.getString("channelId")
                        ?.let { java.net.URLDecoder.decode(it, "UTF-8") } ?: return@composable
                    PlayerScreen(
                        channelId = channelId,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}

@Composable
private fun SideNavigation(
    currentRoute: String?,
    accentColor: Color,
    surfaceColor: Color,
    onNavigate: (Screen) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val navWidth by animateDpAsState(
        if (expanded) Dimens.SideNavWidth else Dimens.SideNavCollapsedWidth,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "nav_width"
    )

    Column(
        modifier = Modifier
            .width(navWidth)
            .fillMaxHeight()
            .background(surfaceColor)
            .onFocusChanged { expanded = it.hasFocus }
            .padding(vertical = 24.dp, horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Logo mark
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(0.15f))
                        .border(1.dp, accentColor.copy(0.3f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Tv, null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
                if (expanded) {
                    Text(
                        "SV",
                        style = TextStyles.HeadlineLarge,
                        color = accentColor
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = StreamVaultColors.CardBorder)
        Spacer(Modifier.height(8.dp))

        navEntries.forEach { entry ->
            NavItem(
                icon = entry.icon,
                label = entry.label,
                selected = currentRoute == entry.screen.route,
                expanded = expanded,
                onClick = { onNavigate(entry.screen) },
                accentColor = accentColor
            )
        }
    }
}

// ─── Favorites Screen ──────────────────────────────────────────────────────

@Composable
fun FavoritesScreen(
    settings: AppSettings,
    onChannelClick: (com.streamvault.app.data.model.Channel) -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val accentColor = Color(settings.accentColor.hex)
    val (bgColor) = StreamVaultColors.backgrounds(settings.theme)
    val favoriteChannels = homeViewModel.getFavoriteChannels()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(48.dp)
    ) {
        Column {
            com.streamvault.app.ui.components.SectionHeader(
                title = "My Favorites",
                subtitle = "${favoriteChannels.size} saved channels",
                accentColor = accentColor
            )
            Spacer(Modifier.height(24.dp))

            if (favoriteChannels.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💙", style = TextStyles.DisplayLarge)
                        Spacer(Modifier.height(12.dp))
                        Text("No favorites yet", style = TextStyles.HeadlineLarge, color = StreamVaultColors.TextPrimary)
                        Text(
                            "Browse channels and press ♥ to save your favorites",
                            style = TextStyles.BodyMedium,
                            color = StreamVaultColors.TextMuted
                        )
                    }
                }
            } else {
                val chunks = favoriteChannels.chunked(6)
                androidx.compose.foundation.lazy.LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(chunks.size) { idx ->
                        val chunk = chunks[idx]
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            chunk.forEach { channel ->
                                com.streamvault.app.ui.components.ChannelCard(
                                    channel = channel,
                                    isFavorite = true,
                                    onClick = { onChannelClick(channel) },
                                    onFavoriteToggle = { homeViewModel.toggleFavorite(channel.id) },
                                    modifier = Modifier.weight(1f),
                                    accentColor = accentColor
                                )
                            }
                            repeat(6 - chunk.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
    }
}
