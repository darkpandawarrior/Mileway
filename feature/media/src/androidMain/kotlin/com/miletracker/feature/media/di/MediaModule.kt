package com.miletracker.feature.media.di

import android.content.Context
import com.miletracker.core.data.library.MediaLibraryDao
import com.miletracker.feature.media.repository.MediaLibraryRepository
import com.miletracker.feature.media.repository.MediaRepository
import com.miletracker.feature.media.repository.RealMediaRepository
import com.miletracker.feature.media.viewmodel.CloudLibraryViewModel
import com.miletracker.feature.media.viewmodel.MediaViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mediaModule =
    module {
        single<MediaRepository> { RealMediaRepository(get<Context>()) }
        single { MediaLibraryRepository(get<MediaLibraryDao>()) }
        viewModelOf(::MediaViewModel)
        viewModelOf(::CloudLibraryViewModel)
    }
