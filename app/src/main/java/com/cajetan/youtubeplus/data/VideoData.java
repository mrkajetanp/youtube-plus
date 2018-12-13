package com.cajetan.youtubeplus.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import javax.annotation.Nonnegative;

@Entity(tableName = "video_data_table")
public class VideoData {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "video_id")
    String videoId;

    public VideoData(@NonNull String videoId) {
        this.videoId = videoId;
    }

    @NonNull
    public String getVideoId() {
        return videoId;
    }
}
