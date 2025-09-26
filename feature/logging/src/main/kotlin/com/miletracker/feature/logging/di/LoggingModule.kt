package com.miletracker.feature.logging.di

import com.miletracker.feature.logging.repository.LogMilesDraftRepository
import com.miletracker.feature.logging.repository.LogMilesServiceRepository
import com.miletracker.feature.logging.viewmodel.LogMilesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin wiring for the Log Miles feature.
 *
 * The single [LogMilesViewModel] backs the entire two-step flow + history; the
 * nav graph anchors it to the Step 1 back-stack entry so all destinations share
 * one instance (see `loggingGraph`). Constructor dependencies are resolved by
 * type: vehicle pricing + network api come from upstream modules, the service and
 * draft repositories from this module.
 */
val loggingModule = module {
    single { LogMilesServiceRepository(get()) }
    single { LogMilesDraftRepository(get()) }
    viewModel { LogMilesViewModel(get(), get(), get(), get()) }
}
