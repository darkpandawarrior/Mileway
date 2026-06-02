package com.miletracker.core.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// TODO(ios): real impl via compass-geolocation-mobile Geolocator or bare CLLocationManager
class IosLocationTracker : LocationTracker {
    private val _updates = MutableSharedFlow<GeoPoint>(replay = 1)
    override val updates: Flow<GeoPoint> = _updates.asSharedFlow()

    override suspend fun current(): GeoPoint? = null

    override fun start() {}

    override fun stop() {}
}
