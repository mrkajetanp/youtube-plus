package com.cajetan.youtubeplus.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PlaylistDataDao {
    // TODO: implement some ordering
    // TODO: sort out names

    @Query("SELECT * FROM playlist_data_table")
    fun getAllPlaylists(): LiveData<List<PlaylistData>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAllPlaylists(vararg users: PlaylistData)

    @Query("SELECT * FROM playlist_data_table WHERE playlist_id = :id LIMIT 1")
    fun getPlaylistById(id: String): PlaylistData?

    @Delete
    fun deletePlaylist(data: PlaylistData)
}