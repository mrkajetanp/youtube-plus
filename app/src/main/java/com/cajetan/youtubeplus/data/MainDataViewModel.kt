package com.cajetan.youtubeplus.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class MainDataViewModel(val context: Application) : AndroidViewModel(context) {
    private var mRepository: MainRepository = MainRepository(context)

    private var mFavourites: LiveData<List<VideoData>>
    private var mPlaylists: LiveData<List<PlaylistData>>

    init {
        mFavourites = mRepository.getAllFavourites()
        mPlaylists = mRepository.getAllPlaylists()
    }

    // Favourites

    fun getAllFavourites(): LiveData<List<VideoData>> {
        return mFavourites
    }

    fun containsFavourite(id: String): Boolean {
        return mRepository.containsFavourite(id)
    }

    fun insertFavourite(videoData: VideoData) {
        mRepository.insertFavourite(videoData)
    }

    fun deleteFavourite(videoData: VideoData) {
        mRepository.deleteFavourite(videoData)
    }

    // Playlists

    fun getAllPlaylists(): LiveData<List<PlaylistData>> {
        return mPlaylists
    }

    fun containsPlaylist(id: String): Boolean {
        return mRepository.containsPlaylist(id)
    }

    fun insertPlaylist(playlistData: PlaylistData) {
        mRepository.insertPlaylist(playlistData)
    }

    fun deletePlaylist(playlistData: PlaylistData) {
        mRepository.deletePlaylist(playlistData)
    }
}