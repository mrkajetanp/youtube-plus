package com.cajetan.youtubeplus.viewmodels

import androidx.lifecycle.ViewModel
import com.cajetan.youtubeplus.utils.FeedItem

class StartViewModel : ViewModel() {
    var searchQuery: String = ""
    var nextPageToken: String = ""
    var searching = false

    private var adapterItems: ArrayList<FeedItem> = ArrayList(emptyList())

    fun getAdapterItems(): ArrayList<FeedItem> {
        return adapterItems
    }

    fun addAdapterItems(items: List<FeedItem>) {
        adapterItems.addAll(items)
    }

    fun clearAdapterItems() {
        adapterItems = ArrayList(emptyList())
    }
}
