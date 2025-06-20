package com.miletracker.feature.media.ui.camera

import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.miletracker.core.ui.theme.DesignTokens
import java.io.File

/**
 * Real CameraX preview + capture. Requests the CAMERA permission, binds a
 * [LifecycleCameraController] to a [PreviewView], and on capture writes a JPEG to
 * the app cache dir before handing the uri back to the caller.
 *
 * @param onCaptured  invoked with the saved file uri string once a photo is taken
 */
@Composable
fun CameraCaptureScreen(
    onCaptured: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val permission = rememberCameraPermissionState()

    // Ask for the permission on first composition if we don't already have it.
    LaunchedEffect(Unit) {
        if (!permission.hasPermission) permission.request()
    }

    if (!permission.hasPermission) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(DesignTokens.Spacing.xl),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Camera permission is required to capture a photo.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = DesignTokens.Spacing.l)
            )
            Button(onClick = permission.request) {
                Text("Grant permission")
            }
        }
        return
    }

    val controller = remember {
        LifecycleCameraController(context).apply {
            bindToLifecycle(lifecycleOwner)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    this.controller = controller
                }
            }
        )

        FloatingActionButton(
            onClick = {
                val file = File(context.cacheDir, "capture_${System.currentTimeMillis()}.jpg")
                val output = ImageCapture.OutputFileOptions.Builder(file).build()
                controller.takePicture(
                    output,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(results: ImageCapture.OutputFileResults) {
                            val uri = results.savedUri ?: Uri.fromFile(file)
                            onCaptured(uri.toString())
                        }

                        override fun onError(exception: ImageCaptureException) {
                            // Demo: surface failures silently; a real app would emit an event.
                        }
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = DesignTokens.Spacing.xxl)
        ) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = "Capture photo"
            )
        }
    }
}
