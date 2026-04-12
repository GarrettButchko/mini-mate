package com.garrettbutchko.minimate.content.utilities

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.utilities.generateQRCodeData

fun generateQRCodeImageBitmap(string: String): ImageBitmap? {
    val bytes = generateQRCodeData(string)
    if (bytes.isEmpty()) return null

    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    return bitmap?.asImageBitmap()
}

@Composable
fun QRCodeDisplay(qrDataString: String) {
    // 1. Convert the string to an ImageBitmap (using your function)
    val qrCodeBitmap = remember(qrDataString) {
        generateQRCodeImageBitmap(qrDataString)
    }

    if (qrCodeBitmap != null) {
        // 2. Display it using the Compose Image component
        Image(
            bitmap = qrCodeBitmap,
            contentDescription = "QR Code for $qrDataString",
            modifier = Modifier.size(200.dp)
        )
    } else {
        Text("Failed to load QR Code")
    }
}
