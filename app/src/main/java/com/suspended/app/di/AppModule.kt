package com.suspended.app.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.suspended.app.domain.repository.MusicRepository
import com.suspended.app.domain.repository.DownloadRepository
import com.suspended.app.data.repository.MusicRepositoryImpl
import com.suspended.app.data.repository.DownloadRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindMusicRepository(impl: MusicRepositoryImpl): MusicRepository

    @Binds
    @Singleton
    abstract fun bindDownloadRepository(impl: DownloadRepositoryImpl): DownloadRepository

    companion object {
        @Provides
        @Singleton
        fun provideGson(): Gson = GsonBuilder().create()
    }
}
