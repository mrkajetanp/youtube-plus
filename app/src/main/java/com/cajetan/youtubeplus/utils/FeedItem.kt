package com.cajetan.youtubeplus.utils

import com.google.api.services.youtube.model.Channel
import com.google.api.services.youtube.model.Playlist
import com.google.api.services.youtube.model.Video

class FeedItem(val id: String, val video: Video? = null,
               val playlist: Playlist? = null, val channel: Channel? = null) {

    val itemType = when {
        video != null -> ItemType.Video
        playlist != null -> ItemType.Playlist
        channel != null -> ItemType.Channel
        else -> throw IllegalStateException("No data object passed to the FeedItem")
    }
}

enum class ItemType {
    Video,
    Playlist,
    Channel
}