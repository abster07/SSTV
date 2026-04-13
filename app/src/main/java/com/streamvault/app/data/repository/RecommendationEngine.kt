package com.streamvault.app.data.repository

import com.streamvault.app.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecommendationEngine @Inject constructor() {

    fun recommend(
        allChannels: List<Channel>,
        watchHistory: List<WatchHistoryEntity>,
        favoriteIds: Set<String>,
        settings: RecommendationSettings
    ): List<RecommendedChannel> {
        if (!settings.enabled || allChannels.isEmpty()) return emptyList()

        // Build preference profile from behavior
        val watchedIds = watchHistory.map { it.channelId }.toSet()
        val watchedChannels = allChannels.filter { watchedIds.contains(it.id) }
        val favoriteChannels = allChannels.filter { favoriteIds.contains(it.id) }

        // Derive implicit preferences
        val implicitCategories = buildFrequencyMap(
            (watchedChannels + favoriteChannels).flatMap { it.categories }
        )
        val implicitCountries = buildFrequencyMap(
            (watchedChannels + favoriteChannels).map { it.country }
        )
        val implicitNetworks = buildFrequencyMap(
            (watchedChannels + favoriteChannels).mapNotNull { it.network }
        )
        val implicitContinents = buildFrequencyMap(
            (watchedChannels + favoriteChannels)
                .mapNotNull { ContinentMap.getContinentForCountry(it.country) }
        )

        // Candidate channels — exclude already watched/favorited
        val candidates = allChannels.filter { ch ->
            ch.streams.isNotEmpty() &&
            !watchedIds.contains(ch.id) &&
            !favoriteIds.contains(ch.id) &&
            !(settings.excludeNsfw && ch.isNsfw)
        }

        return candidates
            .map { ch -> score(ch, settings, implicitCategories,
                implicitCountries, implicitNetworks, implicitContinents) }
            .filter { it.score > 0f }
            .sortedByDescending { it.score }
            .take(settings.maxResults)
    }

    private fun score(
        channel: Channel,
        settings: RecommendationSettings,
        implicitCategories: Map<String, Int>,
        implicitCountries: Map<String, Int>,
        implicitNetworks: Map<String, String>,
        implicitContinents: Map<String, Int>
    ): RecommendedChannel {
        var score = 0f
        val reasons = mutableListOf<RecommendationReason>()

        // Explicit tag preferences (highest weight)
        settings.preferredTags.forEach { tag ->
            if (channel.categories.contains(tag)) {
                score += 3f
                reasons.add(RecommendationReason.MatchesCategory(tag))
            }
        }

        // Explicit country preferences
        if (settings.preferredRegions.contains(channel.country)) {
            score += 2.5f
            reasons.add(RecommendationReason.MatchesCountry(channel.country))
        }

        // Explicit continent preferences
        val channelContinent = ContinentMap.getContinentForCountry(channel.country)
        if (channelContinent != null && settings.preferredContinents.contains(channelContinent)) {
            score += 2f
            reasons.add(RecommendationReason.MatchesContinent(
                ContinentMap.continents[channelContinent] ?: channelContinent
            ))
        }

        if (settings.basedOnHistory || settings.basedOnFavorites) {
            // Implicit category match (from watch/favorites behavior)
            channel.categories.forEach { cat ->
                val freq = implicitCategories[cat] ?: 0
                if (freq > 0) {
                    score += minOf(freq * 0.8f, 2.4f)
                    if (reasons.none { it is RecommendationReason.MatchesCategory }) {
                        reasons.add(RecommendationReason.MatchesCategory(cat))
                    }
                }
            }

            // Implicit country match
            val countryFreq = implicitCountries[channel.country] ?: 0
            if (countryFreq > 0) {
                score += minOf(countryFreq * 0.6f, 1.8f)
                if (reasons.none { it is RecommendationReason.MatchesCountry }) {
                    reasons.add(RecommendationReason.MatchesCountry(channel.country))
                }
            }

            // Implicit continent match
            if (channelContinent != null) {
                val continentFreq = implicitContinents[channelContinent] ?: 0
                if (continentFreq > 0) {
                    score += minOf(continentFreq * 0.4f, 1.2f)
                    if (reasons.none { it is RecommendationReason.MatchesContinent }) {
                        reasons.add(RecommendationReason.MatchesContinent(
                            ContinentMap.continents[channelContinent] ?: channelContinent
                        ))
                    }
                }
            }

            // Network match (e.g. user watches BBC One → suggest BBC Two)
            if (channel.network != null && implicitNetworks.containsKey(channel.network)) {
                score += 1.5f
                reasons.add(RecommendationReason.InFavoriteNetwork(channel.network))
            }
        }

        // Boost channels with more streams (signal of quality/reliability)
        score += minOf(channel.streams.size * 0.1f, 0.5f)

        return RecommendedChannel(channel, score, reasons.distinctBy { it::class })
    }

    private fun buildFrequencyMap(items: List<String>): Map<String, Int> =
        items.groupingBy { it }.eachCount()

    // Network map stores channel.id → network for fast lookup
    private fun buildFrequencyMap(items: Map<String, String>): Map<String, String> = items
}