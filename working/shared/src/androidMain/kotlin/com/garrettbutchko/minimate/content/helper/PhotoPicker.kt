package com.garrettbutchko.minimate.content.helper

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * An Android equivalent of the SwiftUI PhotoPicker wrapper.
 * Because Android uses an Intent/Activity Result rather than a "View" 
 * for picking photos, you use this by passing a boolean state [show].
 *
 * Example usage:
 * ```
 * var showPicker by remember { mutableStateOf(false) }
 * var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
 * 
 * PhotoPicker(
 *     show = showPicker,
 *     onImagePicked = { uri -> selectedImageUri = uri },
 *     onDismiss = { showPicker = false }
 * )
 * ```
 */
@Composable
fun PhotoPicker(
    show: Boolean,
    onImagePicked: (Uri?) -> Unit,
    onDismiss: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        onImagePicked(uri)
        onDismiss()
    }

    LaunchedEffect(show) {
        if (show) {
            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }
}
