package com.streamvault.app.data.api

import com.streamvault.app.data.model.*
import retrofit2.http.GET

interface IptvApiService {

    @GET("channels.json")
    suspend fun getChannels(): List<ChannelDto>

    @GET("feeds.json")
    suspend fun getFeeds(): List<FeedDto>

    @GET("logos.json")
    suspend fun getLogos(): List<LogoDto>

    @GET("streams.json")
    suspend fun getStreams(): List<StreamDto>

    @GET("categories.json")
    suspend fun getCategories(): List<CategoryDto>

    @GET("languages.json")
    suspend fun getLanguages(): List<LanguageDto>

    @GET("countries.json")
    suspend fun getCountries(): List<CountryDto>

    @GET("guides.json")
    suspend fun getGuides(): List<GuideDto>

    @GET("blocklist.json")
    suspend fun getBlocklist(): List<BlocklistDto>
}
