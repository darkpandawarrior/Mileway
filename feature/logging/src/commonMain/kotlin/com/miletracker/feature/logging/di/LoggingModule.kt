package com.miletracker.feature.logging.di

import com.miletracker.feature.logging.repository.ExpenseRepository
import com.miletracker.feature.logging.repository.LogMilesDraftRepository
import com.miletracker.feature.logging.repository.LogMilesServiceRepository
import com.miletracker.feature.logging.usecase.LogMilesSubmitUseCase
import com.miletracker.feature.logging.viewmodel.ExpenseViewModel
import com.miletracker.feature.logging.viewmodel.LogMilesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val loggingModule =
    module {
        single { LogMilesServiceRepository(get()) }
        single { LogMilesDraftRepository(get()) }
        single { ExpenseRepository() }
        factory { LogMilesSubmitUseCase(get()) }
        viewModel { LogMilesViewModel(get(), get(), get(), get()) }
        viewModel { ExpenseViewModel(get()) }
    }
