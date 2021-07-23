package com.raywenderlich.podplay.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.raywenderlich.podplay.repository.ItunesRepo
import com.raywenderlich.podplay.service.PodcastResponse.ItunesPodcast

class SearchViewModel(application: Application): AndroidViewModel(application) {
    var itunesRepo: ItunesRepo? = null

    data class PodcastSummaryViewData(
        var name: String? = "",
        var lastUpdated: String? = "",
        var imageUrl: String? = "",
        var feedUrl: String? = ""
    )

    private fun itunesPodcastToPodcastSummaryView(itunesPodcast: ItunesPodcast) =
        PodcastSummaryViewData(
            name = itunesPodcast.collectionCensoredName,
            lastUpdated = DateUtils.jsonDateToShortDate(itunesPodcast.releaseDate),
            imageUrl = itunesPodcast.artworkUrl30,
            feedUrl = itunesPodcast.feedUrl
        )

    suspend fun searchPodcasts(term: String): List<PodcastSummaryViewData> {
        val response = itunesRepo?.searchByTerm(term)
        if (response == null || !response.isSuccessful) {
            return emptyList()
        }

        val podcasts = response.body()?.results
        if (podcasts.isNullOrEmpty()) {
            return emptyList()
        }

        return podcasts.map { podcast -> itunesPodcastToPodcastSummaryView(podcast) }
    }
}
