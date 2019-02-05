package com.cajetan.youtubeplus.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import org.jetbrains.anko.doAsync

class MainRepository(application: Application) {
    private var mVideoDataDao: VideoDataDao = MainDatabase.getDatabase(application)!!.videoDataDao()
    private var mAllFavourites: LiveData<List<VideoData>>

    private var mPlaylistDataDao: PlaylistDataDao = MainDatabase.getDatabase(application)!!.playlistDataDao()
    private var mAllPlaylists: LiveData<List<PlaylistData>>

    init {
        mAllFavourites = mVideoDataDao.getAllFavourites()
        mAllPlaylists = mPlaylistDataDao.getAllPlaylists()
    }

    fun getAllFavourites(): LiveData<List<VideoData>> {
        return mAllFavourites
    }

    fun containsFavourite(videoId: String): Boolean {
        Log.d("Repository", "Size with $videoId is ${mVideoDataDao.getFavouriteById(videoId)}")
        return mVideoDataDao.getFavouriteById(videoId) != null
    }

    fun insertFavourite(videoData: VideoData) {
        doAsync {
            mVideoDataDao.insertAllFavourites(videoData)
        }
    }

    fun deleteFavourite(videoData: VideoData) {
        doAsync {
            mVideoDataDao.deleteFavourite(videoData)
        }
    }

    fun getAllPlaylists(): LiveData<List<PlaylistData>> {
        return mAllPlaylists
    }

    fun containsPlaylist(id: String): Boolean {
        return mPlaylistDataDao.getPlaylistById(id) != null
    }

    fun insertPlaylist(playlistData: PlaylistData) {
        doAsync {
            mPlaylistDataDao.insertAllPlaylists(playlistData)
        }
    }

    fun deletePlaylist(playlistData: PlaylistData) {
        doAsync {
            mPlaylistDataDao.deletePlaylist(playlistData)
        }
    }
}