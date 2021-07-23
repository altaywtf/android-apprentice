package com.raywenderlich.podplay.viewmodel

import com.raywenderlich.podplay.model.Episode
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.raywenderlich.podplay.model.Podcast
import com.raywenderlich.podplay.repository.PodcastRepo
import java.util.*

class PodcastViewModel(application: Application): AndroidViewModel(application) {
    var podcastRepo: PodcastRepo? = null
    var activePodcastViewData: PodcastViewData? = null

    data class PodcastViewData(
        var subscribed: Boolean = false,
        var feedTitle: String? = "",
        var feedUrl: String? = "",
        var feedDesc: String? = "",
        var imageUrl: String? = "",
        var episodes: List<EpisodeViewData>
    )

    data class EpisodeViewData(
        var guid: String? = "",
        var title: String? = "",
        var description: String? = "",
        var mediaUrl: String? = "",
        var releaseDate: Date? = null,
        var duration: String? = ""
    )

    private fun episodeToEpisodesView(episodes: List<Episode>) = episodes.map { episode ->
        EpisodeViewData(
            guid = episode.guid,
            title = episode.title,
            description = episode.description,
            mediaUrl = episode.mediaUrl,
            releaseDate = episode.releaseDate,
            duration = episode.duration,
        )
    }

    private fun podcastToPodcastView(podcast: Podcast) = PodcastViewData(
        subscribed = false,
        feedTitle =  podcast.feedTitle,
        feedUrl = podcast.feedUrl,
        feedDesc = podcast.feedDesc,
        imageUrl = podcast.imageUrl,
        episodes = episodeToEpisodesView(podcast.episodes),
    )

    fun getPodcast(podcastSummaryViewData: SearchViewModel.PodcastSummaryViewData): PodcastViewData? {
        val repo = podcastRepo ?: return null
        val feedUrl = podcastSummaryViewData.feedUrl ?: return null
        val podcast = repo.getPodcast(feedUrl) ?: return null
        activePodcastViewData = podcastToPodcastView(podcast)
        return activePodcastViewData
    }
}
