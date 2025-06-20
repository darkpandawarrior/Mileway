package com.miletracker.core.ui.di

import com.miletracker.core.ui.theme.ThemeController
import org.koin.dsl.module

/**
 * Koin module for shared UI infrastructure that must outlive any single screen,
 * e.g. the app-wide [ThemeController] read by the shell and written by Settings.
 */
val coreUiModule = module {
    single { ThemeController() }
}
