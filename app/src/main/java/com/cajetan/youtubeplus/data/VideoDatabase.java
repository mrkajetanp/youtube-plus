package com.cajetan.youtubeplus.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {VideoData.class}, version = 1, exportSchema = false)
public abstract class VideoDatabase extends RoomDatabase {
    public abstract VideoDataDao videoDataDao();

    private static volatile VideoDatabase INSTANCE;

    static VideoDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (VideoDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            VideoDatabase.class, "video_database").build();
                }
            }
        }

        return INSTANCE;
    }
}
