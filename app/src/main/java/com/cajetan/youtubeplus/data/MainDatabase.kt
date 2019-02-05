package com.cajetan.youtubeplus.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(entities = [VideoData::class, PlaylistData::class], version = 2, exportSchema = false)
abstract class MainDatabase : RoomDatabase() {
    abstract fun videoDataDao(): VideoDataDao
    abstract fun playlistDataDao(): PlaylistDataDao

    companion object {
        private var INSTANCE: MainDatabase? = null

        fun getDatabase(context: Context): MainDatabase? {
            if (INSTANCE == null) {
                synchronized(MainDatabase::class) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context.applicationContext,
                                MainDatabase::class.java, "main_database")
                                .fallbackToDestructiveMigration().build()
                    }
                }
            }

            return INSTANCE
        }
    }
}

