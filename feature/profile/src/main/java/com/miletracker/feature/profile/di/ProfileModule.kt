package com.miletracker.feature.profile.di

import com.miletracker.feature.profile.repository.FakeProfileRepository
import com.miletracker.feature.profile.repository.ProfileRepository
import com.miletracker.feature.profile.viewmodel.ProfileViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val profileModule = module {
    single<ProfileRepository> { FakeProfileRepository() }
    viewModelOf(::ProfileViewModel)
}
