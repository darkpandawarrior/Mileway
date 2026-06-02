package com.miletracker.feature.travel.di

import com.miletracker.feature.travel.repository.TravelRepository
import org.koin.dsl.module

val travelModule =
    module {
        single { TravelRepository() }
    }
