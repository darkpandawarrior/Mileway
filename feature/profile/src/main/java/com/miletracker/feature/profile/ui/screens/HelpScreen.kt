package com.miletracker.feature.profile.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.miletracker.core.ui.components.topbar.DepthAwareTopBar
import com.miletracker.core.ui.theme.DesignTokens
import com.miletracker.core.ui.theme.DesignTokens.NavigationDepth

private data class Faq(val q: String, val a: String)

private val faqs = listOf(
    Faq("How is distance tracked?", "Start a journey on the Track tab — the app records GPS points and computes the distance. This demo replays seeded sample trips."),
    Faq("What is Log Miles for?", "Use Log Miles to claim a trip by entering the distance manually, without live GPS tracking."),
    Faq("How do odometer & receipts work?", "On the submission screen you can scan an odometer reading with the camera (OCR is mocked) and attach receipt photos as proof."),
    Faq("Is any data sent to a server?", "No. This is an offline demo — all data is mocked locally and no network calls are made."),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            DepthAwareTopBar(
                title = "Help",
                depth = NavigationDepth.LEVEL_1,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(DesignTokens.Spacing.l),
        ) {
            faqs.forEach { faq ->
                Text(faq.q, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    faq.a,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = DesignTokens.Spacing.xs, bottom = DesignTokens.Spacing.l),
                )
            }
        }
    }
}
