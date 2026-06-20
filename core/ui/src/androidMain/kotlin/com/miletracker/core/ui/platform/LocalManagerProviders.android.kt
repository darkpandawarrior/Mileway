package com.miletracker.core.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.miletracker.core.platform.PlatformBindings
import org.koin.mp.KoinPlatform

/**
 * Android [LocalManagerProvider]: resolves the app-scoped [PlatformBindings] from Koin and seeds the
 * manager composition locals.
 *
 * Activity-scoped real impls (UP.1 / RV.2 / SH.1) will be constructed here from
 * `LocalContext.current` as the host Activity; until those land, the no-op Koin bindings are provided.
 */
@Composable
actual fun LocalManagerProvider(content: @Composable () -> Unit) {
    val bindings = remember { KoinPlatform.getKoin().get<PlatformBindings>() }
    ProvideManagers(bindings = bindings, content = content)
}
