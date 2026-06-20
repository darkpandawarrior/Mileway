package com.miletracker.feature.cards.di

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Cards feature DI. Locale-aware mock provider + MVI ViewModels are registered here (Q.3); for Q.1 this is
 * the wired-but-empty entry point that proves the module is on the graph.
 */
val cardsModule: Module = module { }
