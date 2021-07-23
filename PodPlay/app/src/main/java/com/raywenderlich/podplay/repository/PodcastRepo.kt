package com.raywenderlich.podplay.repository

import com.raywenderlich.podplay.model.Episode
import com.raywenderlich.podplay.model.Podcast
import com.raywenderlich.podplay.service.RssFeedResponse
import com.raywenderlich.podplay.service.RssFeedService

class PodcastRepo(private var rssFeedService: RssFeedService) {
    private fun rssItemsToEpisodes(
        episodeResponses: List<RssFeedResponse.EpisodeResponse>
    ) = episodeResponses.map {
        Episode(
            guid = it.guid ?: "",
            title = it.title ?: "",
            description = it.description ?: "",
            mediaUrl = it.url ?: "",
            mimeType = it.type ?: "",
            releaseDate = DateUtils.xmlDateToDate(it.pubDate),
            duration = it.duration ?: ""
        )
    }

    private fun rssResponseToPodcast(
        feedUrl: String,
        imageUrl: String,
        rssResponse: RssFeedResponse
    ) = Podcast(
        feedUrl = feedUrl,
        feedTitle = rssResponse.title,
        feedDesc = if (rssResponse.description == "") rssResponse.summary else rssResponse.description,
        imageUrl = imageUrl,
        lastUpdated = rssResponse.lastUpdated,
        episodes = rssItemsToEpisodes(rssResponse.episodes ?: mutableListOf())
    )

    suspend fun getPodcast(feedUrl: String): Podcast? {
        val rssFeedResponse = rssFeedService.getFeed(feedUrl) ?: return null
        return rssResponseToPodcast(feedUrl, "", rssFeedResponse)
    }
}
