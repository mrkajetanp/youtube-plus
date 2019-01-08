package com.cajetan.youtubeplus.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(entities = [VideoData::class], version = 1, exportSchema = false)
abstract class VideoDatabase : RoomDatabase() {
    abstract fun videoDataDao(): VideoDataDao

    companion object {
        private var INSTANCE: VideoDatabase? = null

        fun getDatabase(context: Context): VideoDatabase? {
            if (INSTANCE == null) {
                synchronized(VideoDatabase::class) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context.applicationContext,
                                VideoDatabase::class.java, "video_database").build()
                    }
                }
            }

            return INSTANCE
        }

        fun destroyDataBase() {
            INSTANCE = null
        }
    }
}

