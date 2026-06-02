package com.miletracker.feature.agent.di

import com.miletracker.feature.agent.repository.AgentRepository
import com.miletracker.feature.agent.viewmodel.AgentViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val agentModule =
    module {
        single { AgentRepository() }
        viewModelOf(::AgentViewModel)
    }
