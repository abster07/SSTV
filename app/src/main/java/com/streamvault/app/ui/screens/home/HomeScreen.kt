package com.streamvault.app.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.streamvault.app.data.model.*
import com.streamvault.app.ui.components.*
import com.streamvault.app.ui.screens.theme.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onChannelClick: (Channel) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val accentColor = Color(settings.accentColor.hex)
    val (bgColor, surfaceColor) = StreamVaultColors.backgrounds(settings.theme)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        when {
            uiState.isLoading -> StreamVaultLoader(accentColor)
            uiState.error != null -> ErrorState(uiState.error!!, viewModel::loadData, accentColor)
            else -> HomeContent(
                uiState = uiState,
                settings = settings,
                accentColor = accentColor,
                onChannelClick = onChannelClick,
                onCategorySelect = viewModel::selectCategory,
                onCountrySelect = viewModel::selectCountry,
                onSearch = viewModel::search,
                onFavoriteToggle = viewModel::toggleFavorite,
                getFavoriteChannels = viewModel::getFavoriteChannels
            )
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    settings: AppSettings,
    accentColor: Color,
    onChannelClick: (Channel) -> Unit,
    onCategorySelect: (String?) -> Unit,
    onCountrySelect: (String?) -> Unit,
    onSearch: (String) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    getFavoriteChannels: () -> List<Channel>
) {
    val scrollState = rememberLazyListState()
    var searchQuery by remember { mutableStateOf("") }

    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 32.dp)
    ) {
        item {
            TopBar(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it; onSearch(it) },
                accentColor = accentColor,
                channelCount = uiState.allChannels.size
            )
            Spacer(Modifier.height(24.dp))
        }

        if (uiState.featuredChannels.isNotEmpty() && searchQuery.isBlank() &&
            uiState.selectedCategory == null && uiState.selectedCountry == null) {
            item {
                SectionHeader(
                    title = "Featured",
                    subtitle = "Top channels right now",
                    accentColor = accentColor,
                    modifier = Modifier.padding(horizontal = 48.dp)
                )
                Spacer(Modifier.height(16.dp))
                FeaturedCarousel(
                    channels = uiState.featuredChannels,
                    favoriteIds = uiState.favoriteIds,
                    accentColor = accentColor,
                    onChannelClick = onChannelClick,
                    onFavoriteToggle = onFavoriteToggle
                )
                Spacer(Modifier.height(32.dp))
            }
        }

        item {
            SectionHeader(
                title = "Categories",
                accentColor = accentColor,
                modifier = Modifier.padding(horizontal = 48.dp)
            )
            Spacer(Modifier.height(12.dp))
            CategoryRow(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                accentColor = accentColor,
                onSelect = onCategorySelect
            )
            Spacer(Modifier.height(32.dp))
        }

        val favorites = getFavoriteChannels()
        if (favorites.isNotEmpty() && searchQuery.isBlank()) {
            item {
                SectionHeader(
                    title = "My Favorites",
                    subtitle = "${favorites.size} channels",
                    accentColor = accentColor,
                    modifier = Modifier.padding(horizontal = 48.dp)
                )
                Spacer(Modifier.height(16.dp))
                ChannelRow(
                    channels = favorites,
                    favoriteIds = uiState.favoriteIds,
                    accentColor = accentColor,
                    onChannelClick = onChannelClick,
                    onFavoriteToggle = onFavoriteToggle
                )
                Spacer(Modifier.height(32.dp))
            }
        }

        if (searchQuery.isBlank() && uiState.selectedCategory == null) {
            item {
                SectionHeader(
                    title = "By Country",
                    accentColor = accentColor,
                    modifier = Modifier.padding(horizontal = 48.dp)
                )
                Spacer(Modifier.height(12.dp))
                CountryRow(
                    countries = uiState.countries.take(30),
                    selectedCountry = uiState.selectedCountry,
                    accentColor = accentColor,
                    onSelect = onCountrySelect
                )
                Spacer(Modifier.height(32.dp))
            }
        }

        item {
            val title = when {
                searchQuery.isNotBlank() -> "Search: \"$searchQuery\""
                uiState.selectedCategory != null -> uiState.categories.find {
                    it.id == uiState.selectedCategory
                }?.name ?: "Channels"
                uiState.selectedCountry != null -> "Channels in ${uiState.selectedCountry}"
                else -> "All Channels"
            }
            SectionHeader(
                title = title,
                subtitle = "${uiState.filteredChannels.size} channels",
                accentColor = accentColor,
                modifier = Modifier.padding(horizontal = 48.dp)
            )
            Spacer(Modifier.height(16.dp))
        }

        val chunks = uiState.filteredChannels.chunked(6)
        items(chunks) { chunk ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                chunk.forEach { channel ->
                    ChannelCard(
                        channel = channel,
                        isFavorite = uiState.favoriteIds.contains(channel.id),
                        onClick = { onChannelClick(channel) },
                        onFavoriteToggle = { onFavoriteToggle(channel.id) },
                        modifier = Modifier.weight(1f),
                        accentColor = accentColor
                    )
                }
                repeat(6 - chunk.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        item { Spacer(Modifier.height(48.dp)) }
    }
}

@Composable
private fun TopBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    accentColor: Color,
    channelCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(listOf(accentColor, accentColor.copy(0.5f)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Tv, null, tint = Color.Black, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text("StreamVault", style = TextStyles.DisplayMedium, color = StreamVaultColors.TextPrimary)
            }
            Text("$channelCount channels available", style = TextStyles.Caption, color = StreamVaultColors.TextMuted)
        }

        SearchBar(
            query = searchQuery,
            onQueryChange = onSearchChange,
            modifier = Modifier.width(400.dp),
            accentColor = accentColor
        )

        val time = remember { mutableStateOf("") }
        LaunchedEffect(Unit) {
            while (true) {
                time.value = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                    .format(java.util.Date())
                kotlinx.coroutines.delay(30_000)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(time.value, style = TextStyles.HeadlineLarge, color = accentColor)
            Text(
                java.text.SimpleDateFormat("EEE, MMM d", java.util.Locale.getDefault())
                    .format(java.util.Date()),
                style = TextStyles.Caption,
                color = StreamVaultColors.TextMuted
            )
        }
    }
}

@Composable
private fun FeaturedCarousel(
    channels: List<Channel>,
    favoriteIds: List<String>,
    accentColor: Color,
    onChannelClick: (Channel) -> Unit,
    onFavoriteToggle: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(channels, key = { it.id }) { channel ->
            FeaturedCard(
                channel = channel,
                isFavorite = favoriteIds.contains(channel.id),
                accentColor = accentColor,
                onClick = { onChannelClick(channel) },
                onFavoriteToggle = { onFavoriteToggle(channel.id) }
            )
        }
    }
}

@Composable
private fun FeaturedCard(
    channel: Channel,
    isFavorite: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        if (isFocused) 1.04f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "feat_scale"
    )

    Box(
        modifier = Modifier
            .width(280.dp)
            .height(160.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(16.dp))
            .border(
                2.dp,
                if (isFocused) accentColor else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .background(StreamVaultColors.SurfaceCard)
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(onClick = onClick)
    ) {
        if (channel.logoUrl != null) {
            AsyncImage(
                model = channel.logoUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.2f },
                contentScale = ContentScale.Crop
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, StreamVaultColors.Background.copy(0.9f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                AsyncImage(
                    model = channel.logoUrl,
                    contentDescription = channel.name,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(StreamVaultColors.SurfaceVariant),
                    contentScale = ContentScale.Fit
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LiveBadge()
                    channel.streams.firstOrNull()?.quality?.let { QualityBadge(it) }
                }
            }

            Column {
                Text(
                    channel.name,
                    style = TextStyles.TitleLarge,
                    color = StreamVaultColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(channel.countryFlag, style = TextStyles.BodyMedium)
                    Text(
                        channel.network ?: channel.country,
                        style = TextStyles.Caption,
                        color = StreamVaultColors.TextMuted
                    )
                    Text(
                        "• ${channel.streams.size} feeds",
                        style = TextStyles.Caption,
                        color = StreamVaultColors.TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(
    categories: List<Category>,
    selectedCategory: String?,
    accentColor: Color,
    onSelect: (String?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            CategoryChip(
                category = Category("all", "All", "All channels"),
                selected = selectedCategory == null,
                onClick = { onSelect(null) },
                accentColor = accentColor
            )
        }
        items(categories, key = { it.id }) { cat ->
            CategoryChip(
                category = cat,
                selected = selectedCategory == cat.id,
                onClick = { onSelect(if (selectedCategory == cat.id) null else cat.id) },
                accentColor = accentColor
            )
        }
    }
}

@Composable
private fun CountryRow(
    countries: List<Country>,
    selectedCountry: String?,
    accentColor: Color,
    onSelect: (String?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(countries, key = { it.code }) { country ->
            var isFocused by remember { mutableStateOf(false) }
            val active = selectedCountry == country.code || isFocused

            Box(
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (active) accentColor.copy(0.15f) else StreamVaultColors.SurfaceVariant
                    )
                    .border(
                        1.dp,
                        if (active) accentColor else StreamVaultColors.CardBorder,
                        RoundedCornerShape(20.dp)
                    )
                    .onFocusChanged { isFocused = it.isFocused }
                    .clickable { onSelect(if (selectedCountry == country.code) null else country.code) }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(country.flag, style = TextStyles.BodyMedium)
                    Text(
                        country.name,
                        style = TextStyles.LabelLarge,
                        color = if (active) accentColor else StreamVaultColors.TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ChannelRow(
    channels: List<Channel>,
    favoriteIds: List<String>,
    accentColor: Color,
    onChannelClick: (Channel) -> Unit,
    onFavoriteToggle: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(channels, key = { it.id }) { channel ->
            ChannelCard(
                channel = channel,
                isFavorite = favoriteIds.contains(channel.id),
                onClick = { onChannelClick(channel) },
                onFavoriteToggle = { onFavoriteToggle(channel.id) },
                accentColor = accentColor
            )
        }
    }
}
