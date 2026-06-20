package com.miletracker.core.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.miletracker.core.platform.PlatformBindings
import org.koin.mp.KoinPlatform

/**
 * iOS [LocalManagerProvider]: resolves the app-scoped [PlatformBindings] from Koin and seeds the
 * manager composition locals.
 *
 * UIViewController-scoped real impls (UP.3 / RV.3 / SH.1) will be constructed here from the root
 * UIViewController; until those land, the no-op Koin bindings are provided.
 */
@Composable
actual fun LocalManagerProvider(content: @Composable () -> Unit) {
    val bindings = remember { KoinPlatform.getKoin().get<PlatformBindings>() }
    ProvideManagers(bindings = bindings, content = content)
}
