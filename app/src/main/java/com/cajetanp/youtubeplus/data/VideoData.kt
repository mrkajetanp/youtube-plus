package com.cajetanp.youtubeplus.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_data_table")
data class VideoData(
    @PrimaryKey
    @ColumnInfo(name = "video_id")
    var videoId: String
)
