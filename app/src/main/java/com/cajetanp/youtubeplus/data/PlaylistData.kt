package com.cajetanp.youtubeplus.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlist_data_table")
data class PlaylistData(
    @PrimaryKey
    @ColumnInfo(name = "playlist_id")
    var playlistId: String
)
