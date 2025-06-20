package com.miletracker.stub.di

import com.miletracker.core.network.api.MileTrackerNetworkApi
import com.miletracker.core.network.config.ConfigProvider
import com.miletracker.stub.DemoConfigManager
import com.miletracker.stub.FakeTrackingNetworkApi
import org.koin.dsl.module

val stubModule = module {
    single { DemoConfigManager() }
    single<ConfigProvider> { get<DemoConfigManager>() }
    single<MileTrackerNetworkApi> { FakeTrackingNetworkApi() }
}
