package com.cajetanp.youtubeplus.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface VideoDataDao {
    // TODO: implement some ordering

    @Query("SELECT * FROM video_data_table")
    fun getAllFavourites(): LiveData<List<VideoData>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAllFavourites(vararg users: VideoData)

    @Query("SELECT * FROM video_data_table WHERE video_id = :id LIMIT 1")
    fun getFavouriteById(id: String): VideoData?

    @Delete
    fun deleteFavourite(data: VideoData)
}