package com.miletracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miletracker.core.network.config.ConfigProvider
import com.miletracker.core.ui.platform.LocalManagerProvider
import com.miletracker.core.ui.platform.UpdateGate
import com.miletracker.core.ui.theme.MileTrackerTheme
import com.miletracker.core.ui.theme.ThemeController
import com.miletracker.ui.AppGraph
import com.miletracker.ui.MileTrackerAppRoot
import com.miletracker.ui.auth.LoginScreen
import com.miletracker.ui.auth.SplashScreen
import org.koin.compose.koinInject

/** App startup stages: splash → fake login → the bottom-navigation shell. */
private enum class AppStage { SPLASH, LOGIN, APP }

/**
 * Single entry point for the app. Plays the splash and fake-login theatre, then hosts the
 * unified bottom-navigation shell ([MileTrackerAppRoot]).
 *
 * Handles `miletracker://` deep links by mapping the host segment to a graph route and
 * forwarding it to [MileTrackerAppRoot] as [initialRoute].
 */
class LauncherActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AppEntry(initialRoute = intent.deepLinkRoute()) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        setContent { AppEntry(initialRoute = intent.deepLinkRoute()) }
    }
}

private fun Intent.deepLinkRoute(): String? {
    if (action != Intent.ACTION_VIEW) return null
    val uri: Uri = data ?: return null
    if (uri.scheme != "miletracker") return null
    return when (uri.host) {
        "home" -> AppGraph.HOME
        "track" -> AppGraph.TRACK
        "log" -> AppGraph.LOG
        "profile" -> AppGraph.PROFILE
        else -> null
    }
}

@Composable
private fun AppEntry(
    initialRoute: String? = null,
    themeController: ThemeController = koinInject(),
    configProvider: ConfigProvider = koinInject(),
) {
    val updateConfig = remember(configProvider) { configProvider.getUpdateConfig() }
    val systemDark = isSystemInDarkTheme()
    val override by themeController.darkThemeOverride.collectAsStateWithLifecycle()
    val palette by themeController.accentPalette.collectAsStateWithLifecycle()
    val customSeedHex by themeController.customSeedHex.collectAsStateWithLifecycle()
    val useSystemColors by themeController.useSystemColors.collectAsStateWithLifecycle()
    val paletteStyle by themeController.paletteStyle.collectAsStateWithLifecycle()

    var stage by rememberSaveable { mutableStateOf(AppStage.SPLASH) }

    // PF.4: seed the Activity-scoped platform managers (in-app update / review / share / …) so any
    // shared screen can read them via `LocalAppReviewManager.current` with no expect/actual at the call site.
    LocalManagerProvider {
        MileTrackerTheme(
            darkTheme = override ?: systemDark,
            palette = palette,
            customSeedHex = customSeedHex,
            useSystemColors = useSystemColors,
            paletteStyle = paletteStyle,
        ) {
            AnimatedContent(
                targetState = stage,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "appStage",
            ) { current ->
                when (current) {
                    AppStage.SPLASH -> SplashScreen(onFinished = { stage = AppStage.LOGIN })
                    AppStage.LOGIN -> LoginScreen(onSignedIn = { stage = AppStage.APP })
                    AppStage.APP ->
                        UpdateGate(config = updateConfig) {
                            MileTrackerAppRoot(deepLinkRoute = initialRoute)
                        }
                }
            }
        }
    }
}
