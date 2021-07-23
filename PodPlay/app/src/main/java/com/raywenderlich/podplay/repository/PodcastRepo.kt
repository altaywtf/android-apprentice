package com.raywenderlich.podplay.repository

import com.raywenderlich.podplay.model.Podcast

class PodcastRepo {
    fun getPodcast(feedUrl: String): Podcast? {
        return Podcast(
            feedUrl,
            "No name",
            "No description",
            "No Image"
        )
    }
}