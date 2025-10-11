package com.miletracker.feature.payables.di

import com.miletracker.feature.payables.repository.PayablesRepository
import com.miletracker.feature.payables.viewmodel.PayablesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val payablesModule = module {
    single { PayablesRepository() }
    viewModel { PayablesViewModel(get()) }
}
