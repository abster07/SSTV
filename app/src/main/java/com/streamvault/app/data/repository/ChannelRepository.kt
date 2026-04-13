package com.streamvault.app.data.repository

import com.streamvault.app.data.api.FavoriteDao
import com.streamvault.app.data.api.IptvApiService
import com.streamvault.app.data.api.WatchHistoryDao
import com.streamvault.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

@Singleton
class ChannelRepository @Inject constructor(
    private val api: IptvApiService,
    private val favoriteDao: FavoriteDao,
    private val watchHistoryDao: WatchHistoryDao
) {
    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _countries = MutableStateFlow<List<Country>>(emptyList())
    val countries: StateFlow<List<Country>> = _countries

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var blockedChannelIds: Set<String> = emptySet()

    suspend fun loadAll(): Result<Unit> = withContext(Dispatchers.IO) {
        _isLoading.value = true
        return@withContext try {
            coroutineScope {
                val channelsDeferred = async { api.getChannels() }
                val streamsDeferred = async { api.getStreams() }
                val logosDeferred = async { api.getLogos() }
                val categoriesDeferred = async { api.getCategories() }
                val countriesDeferred = async { api.getCountries() }
                val blocklistDeferred = async { api.getBlocklist() }

                val channelDtos = channelsDeferred.await()
                val streamDtos = streamsDeferred.await()
                val logoDtos = logosDeferred.await()
                val categoryDtos = categoriesDeferred.await()
                val countryDtos = countriesDeferred.await()
                val blocklist = blocklistDeferred.await()

                blockedChannelIds = blocklist
                    .filter { it.reason == "dmca" }
                    .map { it.channel }
                    .toSet()

                val streamsByChannel = streamDtos.groupBy { it.channel }
                val logosByChannel = logoDtos.groupBy { it.channel }
                val countriesMap = countryDtos.associateBy { it.code }

                val mappedChannels = channelDtos
                    .filter { it.closed == null }
                    .filter { !blockedChannelIds.contains(it.id) }
                    .map { dto ->
                        val channelStreams = streamsByChannel[dto.id]?.map { s ->
                            Stream(s.channel, s.feed, s.title, s.url,
                                s.referrer, s.userAgent, s.quality, s.label)
                        } ?: emptyList()

                        val logoUrl = logosByChannel[dto.id]
                            ?.maxByOrNull { it.width }?.url

                        Channel(
                            id = dto.id,
                            name = dto.name,
                            altNames = dto.altNames,
                            network = dto.network,
                            country = dto.country,
                            countryFlag = countriesMap[dto.country]?.flag ?: "",
                            categories = dto.categories,
                            isNsfw = dto.isNsfw,
                            website = dto.website,
                            logoUrl = logoUrl,
                            streams = channelStreams,
                            isBlocked = blockedChannelIds.contains(dto.id)
                        )
                    }

                val mappedCategories = categoryDtos.map {
                    Category(it.id, it.name, it.description, getCategoryIcon(it.id))
                }

                val mappedCountries = countryDtos.map {
                    Country(it.name, it.code, it.languages, it.flag)
                }

                _channels.value = mappedChannels
                _categories.value = mappedCategories
                _countries.value = mappedCountries
                Result.Success(Unit)
            }
        } catch (e: Exception) {
            Result.Error("Failed to load channels: ${e.localizedMessage}", e)
        } finally {
            _isLoading.value = false
        }
    }

    fun getChannelsByCategory(categoryId: String): List<Channel> =
        _channels.value.filter { channel ->
            channel.categories.contains(categoryId) && channel.streams.isNotEmpty()
        }

    fun getChannelsByCountry(countryCode: String): List<Channel> =
        _channels.value.filter { it.country == countryCode && it.streams.isNotEmpty() }

    fun searchChannels(query: String): List<Channel> {
        if (query.isBlank()) return _channels.value.filter { it.streams.isNotEmpty() }
        val q = query.lowercase()
        return _channels.value.filter { ch ->
            (ch.name.lowercase().contains(q) ||
                    ch.altNames.any { it.lowercase().contains(q) } ||
                    ch.network?.lowercase()?.contains(q) == true ||
                    ch.country.lowercase().contains(q)) &&
                    ch.streams.isNotEmpty()
        }
    }

    fun getFavorites(): Flow<List<FavoriteEntity>> = favoriteDao.getAllFavorites()
    fun getFavoriteIds(): Flow<List<String>> = favoriteDao.getFavoriteIds()

    suspend fun toggleFavorite(channelId: String) {
      val isFav = favoriteDao.isFavorite(channelId).first()
      if (isFav) favoriteDao.removeFavorite(channelId)
      else favoriteDao.addFavorite(FavoriteEntity(channelId))
      
    }
    
    suspend fun addFavorite(channelId: String) = favoriteDao.addFavorite(FavoriteEntity(channelId))
    suspend fun removeFavorite(channelId: String) = favoriteDao.removeFavorite(channelId)
    fun isFavorite(channelId: String): Flow<Boolean> = favoriteDao.isFavorite(channelId)

    fun getWatchHistory(): Flow<List<WatchHistoryEntity>> = watchHistoryDao.getRecentHistory()
    suspend fun addToHistory(entry: WatchHistoryEntity) = watchHistoryDao.insertHistory(entry)
    suspend fun clearHistory() = watchHistoryDao.clearAll()

    private fun getCategoryIcon(categoryId: String): Int = when (categoryId) {
        "news" -> 1
        "sports" -> 2
        "movies" -> 3
        "entertainment" -> 4
        "music" -> 5
        "kids" -> 6
        "documentary" -> 7
        "cooking" -> 8
        "travel" -> 9
        "science" -> 10
        "business" -> 11
        "religion" -> 12
        "weather" -> 13
        "general" -> 14
        else -> 0
    }
}
