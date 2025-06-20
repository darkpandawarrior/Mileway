package com.miletracker.feature.profile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miletracker.core.ui.theme.DesignTokens
import com.miletracker.feature.profile.model.ProfileHeader
import com.miletracker.feature.profile.model.SettingsTile
import com.miletracker.feature.profile.viewmodel.ProfileViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(
    onOpenSettings: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        ProfileHeaderSection(header = state.header)

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignTokens.Spacing.screenHorizontal),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m),
        ) {
            items(state.tiles, key = { it.id }) { tile ->
                SettingsTileCard(
                    tile = tile,
                    onClick = {
                        if (tile.id == ProfileViewModel.TILE_SETTINGS) onOpenSettings()
                    },
                )
            }
        }
    }
}

private const val AvatarSize = 72

@Composable
private fun ProfileHeaderSection(header: ProfileHeader) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DesignTokens.topBarGradientBrush())
            .height(DesignTokens.CardSize.gradientHeaderHeight + AvatarSize.dp),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = DesignTokens.Spacing.screenHorizontal),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.s),
        ) {
            Box(
                modifier = Modifier
                    .size(AvatarSize.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = header.initials,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Text(
                text = header.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
            Text(
                text = header.email,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun SettingsTileCard(
    tile: SettingsTile,
    onClick: () -> Unit,
) {
    Card(
        shape = DesignTokens.Shape.actionTile,
        modifier = Modifier
            .fillMaxWidth()
            .clip(DesignTokens.Shape.actionTile)
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.l),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.s),
        ) {
            Icon(
                imageVector = tile.icon,
                contentDescription = null,
                modifier = Modifier.size(DesignTokens.IconSize.actionTileLarge),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = tile.label,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
