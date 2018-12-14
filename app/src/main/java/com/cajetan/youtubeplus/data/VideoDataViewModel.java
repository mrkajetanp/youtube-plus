package com.cajetan.youtubeplus.data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

public class VideoDataViewModel extends AndroidViewModel {
    private VideoDataRepository mRepository;
    private LiveData<List<VideoData>> mAllVideoData;

    public VideoDataViewModel(Application application) {
        super(application);
        mRepository = new VideoDataRepository(application);
        mAllVideoData = mRepository.getAllVideoData();
    }

    public LiveData<List<VideoData>> getAllVideoData() {
        return mAllVideoData;
    }

    public void insert(VideoData videoData) {
        mRepository.insert(videoData);
    }

    public void delete(VideoData videoData) {
        mRepository.delete(videoData);
    }
}
