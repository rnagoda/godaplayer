package com.godaplayer.app.di

import com.godaplayer.app.data.repository.EqPresetRepositoryImpl
import com.godaplayer.app.data.repository.PlaylistRepositoryImpl
import com.godaplayer.app.data.repository.ScanFolderRepositoryImpl
import com.godaplayer.app.data.repository.SongRepositoryImpl
import com.godaplayer.app.domain.repository.EqPresetRepository
import com.godaplayer.app.domain.repository.PlaylistRepository
import com.godaplayer.app.domain.repository.ScanFolderRepository
import com.godaplayer.app.domain.repository.SongRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSongRepository(impl: SongRepositoryImpl): SongRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(impl: PlaylistRepositoryImpl): PlaylistRepository

    @Binds
    @Singleton
    abstract fun bindScanFolderRepository(impl: ScanFolderRepositoryImpl): ScanFolderRepository

    @Binds
    @Singleton
    abstract fun bindEqPresetRepository(impl: EqPresetRepositoryImpl): EqPresetRepository
}
