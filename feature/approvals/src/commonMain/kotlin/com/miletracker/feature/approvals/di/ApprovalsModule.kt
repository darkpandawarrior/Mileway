package com.miletracker.feature.approvals.di

import com.miletracker.feature.approvals.viewmodel.ApprovalsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val approvalsModule =
    module {
        viewModelOf(::ApprovalsViewModel)
    }
