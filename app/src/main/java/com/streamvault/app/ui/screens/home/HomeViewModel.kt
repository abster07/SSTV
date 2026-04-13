package com.streamvault.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamvault.app.data.model.*
import com.streamvault.app.data.repository.ChannelRepository
import com.streamvault.app.data.repository.Result
import com.streamvault.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val isFilterLoading: Boolean = false,
    val error: String? = null,
    val allChannels: List<Channel> = emptyList(),
    val filteredChannels: List<Channel> = emptyList(),
    val categories: List<Category> = emptyList(),
    val countries: List<Country> = emptyList(),
    val selectedCategory: String? = null,
    val selectedCountry: String? = null,
    val searchQuery: String = "",
    val favoriteIds: List<String> = emptyList(),
    val recentChannels: List<Channel> = emptyList(),
    val featuredChannels: List<Channel> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val channelRepo: ChannelRepository,
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val settings: StateFlow<AppSettings> = settingsRepo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    init {
        observeFavorites()
        observeWatchHistory()
        loadData()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            channelRepo.getFavoriteIds().collect { ids ->
                _uiState.update { it.copy(favoriteIds = ids) }
                refreshFiltered()
            }
        }
    }

    private fun observeWatchHistory() {
        viewModelScope.launch {
            channelRepo.getWatchHistory().collect { history ->
                val allChannels = _uiState.value.allChannels
                if (allChannels.isEmpty()) return@collect
                val recent = history
                    .mapNotNull { entry -> allChannels.find { it.id == entry.channelId } }
                    .distinctBy { it.id }
                    .take(12)
                _uiState.update { it.copy(recentChannels = recent) }
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = channelRepo.loadAll()) {
                is Result.Success -> {
                    val all = channelRepo.channels.value
                    val featured = all
                        .filter { it.streams.isNotEmpty() }
                        .sortedByDescending { it.streams.size }
                        .take(10)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            allChannels = all,
                            categories = channelRepo.categories.value,
                            countries = channelRepo.countries.value,
                            featuredChannels = featured
                        )
                    }
                    refreshFiltered()
                    // Now that channels are loaded, rebuild recent from existing history
                    rebuildRecentFromHistory()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> {}
            }
        }
    }

    private fun rebuildRecentFromHistory() {
        viewModelScope.launch {
            channelRepo.getWatchHistory().first().let { history ->
                val allChannels = _uiState.value.allChannels
                val recent = history
                    .mapNotNull { entry -> allChannels.find { it.id == entry.channelId } }
                    .distinctBy { it.id }
                    .take(12)
                _uiState.update { it.copy(recentChannels = recent) }
            }
        }
    }

    fun selectCategory(categoryId: String?) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedCategory = categoryId,
                    selectedCountry = null,
                    isFilterLoading = true
                )
            }
            // Small delay gives the skeleton a chance to render before heavy work
            delay(80)
            refreshFiltered()
            _uiState.update { it.copy(isFilterLoading = false) }
        }
    }

    fun selectCountry(countryCode: String?) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedCountry = countryCode,
                    selectedCategory = null,
                    isFilterLoading = true
                )
            }
            delay(80)
            refreshFiltered()
            _uiState.update { it.copy(isFilterLoading = false) }
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(searchQuery = query, isFilterLoading = query.isNotBlank()) }
            if (query.isNotBlank()) delay(80)
            refreshFiltered()
            _uiState.update { it.copy(isFilterLoading = false) }
        }
    }

    private fun refreshFiltered() {
        val state = _uiState.value
        val showNsfw = settings.value.showNsfw
        val channels = when {
            state.searchQuery.isNotBlank() ->
                channelRepo.searchChannels(state.searchQuery)
            state.selectedCategory != null ->
                channelRepo.getChannelsByCategory(state.selectedCategory)
            state.selectedCountry != null ->
                channelRepo.getChannelsByCountry(state.selectedCountry)
            else ->
                state.allChannels.filter { it.streams.isNotEmpty() }
        }
        val filtered = if (!showNsfw) channels.filter { !it.isNsfw } else channels
        _uiState.update { it.copy(filteredChannels = filtered) }
    }

    fun toggleFavorite(channelId: String) {
        viewModelScope.launch {
            val isFav = _uiState.value.favoriteIds.contains(channelId)
            if (isFav) channelRepo.removeFavorite(channelId)
            else channelRepo.addFavorite(channelId)
        }
    }

    fun addToWatchHistory(channel: Channel, streamUrl: String) {
        viewModelScope.launch {
            channelRepo.addToHistory(
                WatchHistoryEntity(
                    channelId = channel.id,
                    channelName = channel.name,
                    logoUrl = channel.logoUrl,
                    streamUrl = streamUrl,
                    watchedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun getFavoriteChannels(): List<Channel> {
        val favIds = _uiState.value.favoriteIds.toSet()
        return _uiState.value.allChannels.filter {
            favIds.contains(it.id) && it.streams.isNotEmpty()
        }
    }
}