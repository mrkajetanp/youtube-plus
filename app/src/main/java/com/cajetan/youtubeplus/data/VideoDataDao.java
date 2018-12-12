package com.cajetan.youtubeplus.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface VideoDataDao {
//    @Query("SELECT * FROM video_data")
//    List<VideoData> getAll();

    @Insert
    void insertAll(VideoData... users);

    @Delete
    void delete(VideoData user);
}