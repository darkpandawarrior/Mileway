package com.miletracker.feature.media.di

import com.miletracker.feature.media.repository.FakeMediaRepository
import com.miletracker.feature.media.repository.MediaRepository
import com.miletracker.feature.media.viewmodel.MediaViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mediaModule = module {
    single<MediaRepository> { FakeMediaRepository() }
    viewModelOf(::MediaViewModel)
}
