package com.cajetan.youtubeplus.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class VideoData {
    @PrimaryKey
    public int vIndex;

    @ColumnInfo(name = "video_id")
    public String videoId;
}
