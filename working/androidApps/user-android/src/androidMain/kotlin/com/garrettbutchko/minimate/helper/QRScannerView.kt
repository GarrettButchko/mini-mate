package com.garrettbutchko.minimate.helper

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

/**
 * A Jetpack Compose implementation of QRScannerView using Google Play Services Code Scanner.
 * 
 * Unlike iOS's AVCaptureSession which requires you to build a custom UI and handle permissions,
 * Android provides GmsBarcodeScanning which launches a secure, native bottom-sheet scanner
 * without needing to request CAMERA permissions manually.
 */
@Composable
fun QRScannerView(
    onScan: (String) -> Unit,
    onCanceled: () -> Unit = {}
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()

        val scanner = GmsBarcodeScanning.getClient(context, options)

        scanner.startScan()
            .addOnSuccessListener { barcode ->
                barcode.rawValue?.let { onScan(it) }
            }
            .addOnCanceledListener {
                onCanceled()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                onCanceled()
            }
    }
    
    // Empty box to fulfill composable layout constraints if it's placed in the tree
    Box(modifier = Modifier.fillMaxSize())
}
