package com.miletracker.feature.profile.di

import com.miletracker.feature.profile.repository.AdvanceRepository
import com.miletracker.feature.profile.repository.FakeProfileRepository
import com.miletracker.feature.profile.repository.ProfileRepository
import com.miletracker.feature.profile.viewmodel.AdvanceViewModel
import com.miletracker.feature.profile.viewmodel.DemoSettingsViewModel
import com.miletracker.feature.profile.viewmodel.ProfileViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val profileModule =
    module {
        single<ProfileRepository> { FakeProfileRepository() }
        single { AdvanceRepository() }
        viewModelOf(::ProfileViewModel)
        viewModel { AdvanceViewModel(get()) }
        viewModelOf(::DemoSettingsViewModel)
    }
