package com.cajetan.youtubeplus.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface VideoDataDao {

    // TODO: implement some ordering

    @Query("SELECT * FROM video_data_table")
    LiveData<List<VideoData>> getAll();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(VideoData... users);

    @Delete
    void delete(VideoData data);
}