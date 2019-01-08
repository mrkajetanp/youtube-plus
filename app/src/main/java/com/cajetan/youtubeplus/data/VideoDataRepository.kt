package com.cajetan.youtubeplus.data

import android.app.Application
import androidx.lifecycle.LiveData
import org.jetbrains.anko.doAsync

class VideoDataRepository(application: Application) {
    private var mVideoDataDao: VideoDataDao = VideoDatabase.getDatabase(application)!!.videoDataDao()
    private var mAllVideoData: LiveData<List<VideoData>>

    init {
        mAllVideoData = mVideoDataDao.getAll()
    }

    fun getAllVideoData(): LiveData<List<VideoData>> {
        return mAllVideoData
    }

    fun insert(videoData: VideoData) {
        doAsync {
            mVideoDataDao.insertAll(videoData)
        }
    }

    fun delete(videoData: VideoData) {
        doAsync {
            mVideoDataDao.delete(videoData)
        }
    }
}