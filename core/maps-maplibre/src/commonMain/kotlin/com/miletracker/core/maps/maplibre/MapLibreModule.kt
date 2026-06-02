package com.miletracker.core.maps.maplibre

import com.miletracker.core.maps.MapSurface
import org.koin.dsl.module

val mapLibreModule =
    module {
        single<MapSurface> { MapLibreSurface() }
    }
