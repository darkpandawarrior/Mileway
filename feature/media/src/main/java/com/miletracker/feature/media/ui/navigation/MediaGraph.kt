package com.miletracker.feature.media.ui.navigation

import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.miletracker.feature.media.ui.camera.CameraCaptureScreen
import com.miletracker.feature.media.ui.screens.AttachmentPreviewScreen
import com.miletracker.feature.media.ui.screens.AttachmentSelectionScreen
import com.miletracker.feature.media.viewmodel.MediaViewModel
import org.koin.compose.viewmodel.koinViewModel

object MediaRoutes {
    const val SELECTION = "media_selection"
    const val CAMERA = "media_camera"
    const val PREVIEW = "media_preview"
}

/**
 * Wires the three media routes into a host [NavHostController].
 *
 * All screens share a single [MediaViewModel] instance scoped to the SELECTION
 * back-stack entry so the pending capture and attachment list survive navigation
 * between the selection, camera and preview destinations.
 */
fun NavGraphBuilder.mediaGraph(navController: NavHostController) {
    composable(MediaRoutes.SELECTION) { entry ->
        val parentEntry = remember(entry) { navController.getBackStackEntry(MediaRoutes.SELECTION) }
        val vm: MediaViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
        AttachmentSelectionScreen(
            viewModel = vm,
            onNavigateToCamera = { navController.navigate(MediaRoutes.CAMERA) },
            onNavigateToPreview = { navController.navigate(MediaRoutes.PREVIEW) }
        )
    }

    composable(MediaRoutes.CAMERA) { entry ->
        val parentEntry = remember(entry) { navController.getBackStackEntry(MediaRoutes.SELECTION) }
        val vm: MediaViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
        CameraCaptureScreen(
            onCaptured = { uri ->
                vm.onCaptured(uri)
                navController.navigate(MediaRoutes.PREVIEW)
            }
        )
    }

    composable(MediaRoutes.PREVIEW) { entry ->
        val parentEntry = remember(entry) { navController.getBackStackEntry(MediaRoutes.SELECTION) }
        val vm: MediaViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
        AttachmentPreviewScreen(
            viewModel = vm,
            onRetake = { navController.popBackStack() },
            onUsePhoto = { navController.popBackStack(MediaRoutes.SELECTION, inclusive = false) }
        )
    }
}
