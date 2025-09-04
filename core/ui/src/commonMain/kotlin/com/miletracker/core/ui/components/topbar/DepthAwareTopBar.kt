package com.miletracker.core.ui.components.topbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.miletracker.core.ui.theme.DesignTokens
import com.miletracker.core.ui.theme.DesignTokens.NavigationDepth

/**
 * A navigation depth-aware top app bar implementing the "deeper = calmer" pattern:
 * ROOT renders a bold brand gradient, LEVEL_1 a solid accent, deeper levels a plain surface.
 *
 * @param title       primary title text
 * @param subtitle    optional secondary line shown under the title
 * @param depth       navigation depth controlling the styling (see [NavigationDepth])
 * @param navigationIcon optional leading slot (e.g. a back button)
 * @param actions     optional trailing action slot
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepthAwareTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    depth: NavigationDepth = NavigationDepth.LEVEL_1,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {}
) {
    val config = DesignTokens.topBarConfig(depth)

    val backgroundModifier = if (config.useGradient && config.gradientBrush != null) {
        Modifier.background(config.gradientBrush)
    } else {
        Modifier.background(config.containerColor)
    }

    TopAppBar(
        modifier = modifier.then(backgroundModifier),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
            navigationIconContentColor = config.textColors.iconColor,
            titleContentColor = config.textColors.titleColor,
            actionIconContentColor = config.textColors.iconColor
        ),
        navigationIcon = navigationIcon,
        actions = { actions() },
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = config.textColors.titleColor
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = config.textColors.subtitleColor
                    )
                }
            }
        }
    )
}
