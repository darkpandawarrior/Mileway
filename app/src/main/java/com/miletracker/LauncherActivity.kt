package com.miletracker

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miletracker.core.ui.theme.MileTrackerTheme
import com.miletracker.core.ui.theme.ThemeController
import com.miletracker.ui.MileTrackerAppRoot
import com.miletracker.ui.auth.LoginScreen
import com.miletracker.ui.auth.SplashScreen
import org.koin.compose.koinInject

/** App startup stages: splash → fake login → the bottom-navigation shell. */
private enum class AppStage { SPLASH, LOGIN, APP }

/**
 * Single entry point for the app. Plays the splash and fake-login theatre, then hosts the
 * unified bottom-navigation shell ([MileTrackerAppRoot]).
 */
class LauncherActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AppEntry() }
    }
}

@Composable
private fun AppEntry(themeController: ThemeController = koinInject()) {
    val systemDark = isSystemInDarkTheme()
    val override by themeController.darkThemeOverride.collectAsStateWithLifecycle()
    val palette by themeController.accentPalette.collectAsStateWithLifecycle()
    val customSeedHex by themeController.customSeedHex.collectAsStateWithLifecycle()
    val useSystemColors by themeController.useSystemColors.collectAsStateWithLifecycle()
    val paletteStyle by themeController.paletteStyle.collectAsStateWithLifecycle()

    var stage by rememberSaveable { mutableStateOf(AppStage.SPLASH) }

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
                AppStage.APP -> MileTrackerAppRoot()
            }
        }
    }
}
