package com.miletracker.feature.media.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.miletracker.core.ui.components.topbar.DepthAwareTopBar
import com.miletracker.core.ui.theme.DesignTokens
import com.miletracker.core.ui.theme.DesignTokens.NavigationDepth
import com.miletracker.feature.media.model.AttachmentItem
import com.miletracker.feature.media.model.AttachmentSource
import com.miletracker.feature.media.viewmodel.MediaViewModel

/**
 * Entry screen of the media flow: three source tiles (Camera / Gallery / Files)
 * plus a grid of already-captured attachment thumbnails.
 *
 * @param onNavigateToCamera   navigate to the CameraX capture route
 * @param onNavigateToPreview  navigate to the preview route once a uri is selected
 */
@Composable
fun AttachmentSelectionScreen(
    viewModel: MediaViewModel,
    onNavigateToCamera: () -> Unit,
    onNavigateToPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.onPickedFromGallery(uri.toString())
            onNavigateToPreview()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            DepthAwareTopBar(
                title = "Capture",
                depth = NavigationDepth.ROOT
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(DesignTokens.Spacing.screenHorizontal)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.l)
            ) {
                SourceTile(
                    icon = Icons.Default.CameraAlt,
                    label = "Camera",
                    onClick = {
                        viewModel.onSourceSelected(AttachmentSource.CAMERA)
                        onNavigateToCamera()
                    }
                )
                SourceTile(
                    icon = Icons.Default.PhotoLibrary,
                    label = "Gallery",
                    onClick = {
                        viewModel.onSourceSelected(AttachmentSource.GALLERY)
                        galleryLauncher.launch("image/*")
                    }
                )
                SourceTile(
                    icon = Icons.Default.Folder,
                    label = "Files",
                    onClick = {
                        viewModel.onSourceSelected(AttachmentSource.FILES)
                        galleryLauncher.launch("*/*")
                    }
                )
            }

            Spacer(Modifier.height(DesignTokens.Spacing.sectionSpacing))

            Text(
                text = "Captured (${state.attachments.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(DesignTokens.Spacing.m))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.s),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.s),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.attachments, key = { it.id }) { item ->
                    AttachmentThumbnail(item)
                }
            }
        }
    }
}

@Composable
private fun SourceTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(DesignTokens.ActionTileSize.defaultWidth)
            .clip(DesignTokens.Shape.actionTile)
            .clickable(onClick = onClick)
            .padding(vertical = DesignTokens.Spacing.s),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(DesignTokens.ActionTileSize.circularContainer)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(DesignTokens.IconSize.actionTileLarge)
            )
        }
        Spacer(Modifier.height(DesignTokens.Spacing.s))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AttachmentThumbnail(item: AttachmentItem) {
    Surface(
        shape = DesignTokens.Shape.roundedMd,
        tonalElevation = DesignTokens.Elevation.card,
        modifier = Modifier.aspectRatio(1f)
    ) {
        AsyncImage(
            model = item.uri,
            contentDescription = "Attachment ${item.id}",
            modifier = Modifier
                .fillMaxSize()
                .clip(DesignTokens.Shape.roundedMd)
        )
    }
}
