package com.miletracker.feature.profile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.tooling.preview.Preview
import com.miletracker.core.ui.theme.MileTrackerTheme
import com.miletracker.feature.profile.ui.previews.LightDarkPreview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.miletracker.core.security.RootDetector

@Composable
fun RootGuardScreen(
    onContinue: () -> Unit,
    signals: List<String> = RootDetector.check().signals,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB71C1C))
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.GppBad,
            contentDescription = "Root detected",
            tint = Color.White,
            modifier = Modifier.size(96.dp),
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Root Detected",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "This device appears to be rooted or running in an insecure environment. " +
                    "The app may not function correctly.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.87f),
            textAlign = TextAlign.Center,
        )

        if (signals.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Signals detected:",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            signals.forEach { signal ->
                Text(
                    text = "• $signal",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFFB71C1C),
            ),
        ) {
            Text("Continue Anyway (Demo)", fontWeight = FontWeight.SemiBold)
        }
    }
}

@LightDarkPreview
@Composable
private fun RootGuardScreenPreview() {
    MileTrackerTheme {
        RootGuardScreen(
            onContinue = {},
            signals = listOf("su binary found at /system/xbin/su", "test-keys build", "Magisk detected"),
        )
    }
}

@Preview(name = "Clean device", showBackground = true)
@Composable
private fun RootGuardScreenCleanPreview() {
    MileTrackerTheme {
        RootGuardScreen(
            onContinue = {},
            signals = emptyList(),
        )
    }
}
