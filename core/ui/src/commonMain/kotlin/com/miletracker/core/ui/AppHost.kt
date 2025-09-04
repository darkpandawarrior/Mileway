package com.miletracker.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.miletracker.core.ui.theme.MileTrackerTheme

@Composable
fun AppHost(content: @Composable () -> Unit) {
    MileTrackerTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}
