package com.cajetan.youtubeplus.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface VideoDataDao {
    // TODO: implement some ordering

    @Query("SELECT * FROM video_data_table")
    fun getAll(): LiveData<List<VideoData>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg users: VideoData)

    @Delete
    fun delete(data: VideoData)
}