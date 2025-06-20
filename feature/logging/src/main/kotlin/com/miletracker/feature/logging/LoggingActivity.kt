package com.miletracker.feature.logging

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.miletracker.core.ui.AppHost
import com.miletracker.feature.logging.ui.screens.LogMilesScreen

class LoggingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AppHost { LogMilesScreen() } }
    }
}
