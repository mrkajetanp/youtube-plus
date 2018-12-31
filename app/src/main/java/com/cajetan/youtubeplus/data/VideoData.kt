package com.cajetan.youtubeplus.data

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "video_data_table")
data class VideoData(
    @PrimaryKey
    @ColumnInfo(name = "video_id")
    var videoId: String
)
