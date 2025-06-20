package com.miletracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.miletracker.ui.MileTrackerAppRoot

/**
 * Single entry point for the app. Hosts the unified bottom-navigation shell
 * ([MileTrackerAppRoot]) which contains every feature as a nested nav graph.
 */
class LauncherActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MileTrackerAppRoot()
        }
    }
}
