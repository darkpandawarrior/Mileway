package com.miletracker.feature.media.di

import android.content.Context
import com.miletracker.feature.media.repository.MediaRepository
import com.miletracker.feature.media.repository.RealMediaRepository
import com.miletracker.feature.media.viewmodel.MediaViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mediaModule = module {
    // Use the real on-device ML Kit implementation for the live app.
    // FakeMediaRepository remains available as a test double (injected directly in tests).
    single<MediaRepository> { RealMediaRepository(get<Context>()) }
    viewModelOf(::MediaViewModel)
}
