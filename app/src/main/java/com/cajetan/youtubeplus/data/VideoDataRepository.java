package com.cajetan.youtubeplus.data;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

public class VideoDataRepository {
    private VideoDataDao mVideoDataDao;
    private LiveData<List<VideoData>> mAllVideoData;

    VideoDataRepository(Application application) {
        VideoDatabase db = VideoDatabase.getDatabase(application);
        mVideoDataDao = db.videoDataDao();
        mAllVideoData = mVideoDataDao.getAll();
    }

    LiveData<List<VideoData>> getAllVideoData() {
        return mAllVideoData;
    }

    void insert(VideoData videoData) {
        new InsertTask(mVideoDataDao).execute(videoData);
    }

    void delete(VideoData videoData) {
        new DeleteTask(mVideoDataDao).execute(videoData);
    }

    private static class InsertTask extends AsyncTask<VideoData, Void, Void> {
        private VideoDataDao mAsyncTaskDao;

        InsertTask(VideoDataDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(VideoData... videoData) {
            mAsyncTaskDao.insertAll(videoData[0]);
            return null;
        }
    }

    private static class DeleteTask extends AsyncTask<VideoData, Void, Void> {
        private VideoDataDao mAsyncTaskDao;

        DeleteTask(VideoDataDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(VideoData... videoData) {
            mAsyncTaskDao.delete(videoData[0]);
            return null;
        }
    }
}
