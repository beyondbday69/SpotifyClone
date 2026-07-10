package com.suspended.app.di

import android.content.Context
import androidx.room.Room
import com.suspended.app.data.local.AppDatabase
import com.suspended.app.data.local.dao.CacheDao
import com.suspended.app.data.local.dao.PlaylistDao
import com.suspended.app.data.local.dao.TrackDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "suspended_db"
        ).build()
    }

    @Provides
    fun provideTrackDao(database: AppDatabase): TrackDao = database.trackDao()

    @Provides
    fun providePlaylistDao(database: AppDatabase): PlaylistDao = database.playlistDao()

    @Provides
    fun provideCacheDao(database: AppDatabase): CacheDao = database.cacheDao()
}
