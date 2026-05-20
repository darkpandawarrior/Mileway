package com.miletracker.core.maps.krossmap

import com.miletracker.core.maps.MapSurface
import org.koin.dsl.module

val krossMapModule = module {
    single<MapSurface> { KrossMapSurface() }
}
