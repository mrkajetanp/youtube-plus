package com.cajetan.youtubeplus.data

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

@Database(entities = [VideoData::class], version = 1, exportSchema = false)
abstract class VideoDatabase : RoomDatabase() {
    abstract fun videoDataDao(): VideoDataDao

    companion object {
        var INSTANCE: VideoDatabase? = null

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

