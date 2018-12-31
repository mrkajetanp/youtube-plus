package com.cajetan.youtubeplus.data

import android.app.Application
import android.arch.lifecycle.LiveData
import android.os.AsyncTask

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
        InsertTask(mVideoDataDao).execute(videoData)
    }

    fun delete(videoData: VideoData) {
        DeleteTask(mVideoDataDao).execute(videoData)
    }

    private class InsertTask(val asyncTaskDao: VideoDataDao): AsyncTask<VideoData, Void, Void?>() {
        override fun doInBackground(vararg videoData: VideoData): Void? {
            asyncTaskDao.insertAll(videoData[0])
            return null
        }
    }

    private class DeleteTask(val asyncTaskDao: VideoDataDao): AsyncTask<VideoData, Void, Void?>() {
        override fun doInBackground(vararg videoData: VideoData): Void? {
            asyncTaskDao.delete(videoData[0])
            return null
        }
    }

}