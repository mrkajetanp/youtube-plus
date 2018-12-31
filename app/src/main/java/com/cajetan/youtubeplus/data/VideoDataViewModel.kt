package com.cajetan.youtubeplus.data

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData

class VideoDataViewModel(val context: Application) : AndroidViewModel(context) {
    private var mRepository: VideoDataRepository = VideoDataRepository(context)
    private var mAllVideoData: LiveData<List<VideoData>>

    init {
        mAllVideoData = mRepository.getAllVideoData()
    }

    fun getAllVideoData(): LiveData<List<VideoData>> {
        return mAllVideoData
    }

    fun insert(videoData: VideoData) {
        mRepository.insert(videoData)
    }

    fun delete(videoData: VideoData) {
        mRepository.delete(videoData)
    }
}