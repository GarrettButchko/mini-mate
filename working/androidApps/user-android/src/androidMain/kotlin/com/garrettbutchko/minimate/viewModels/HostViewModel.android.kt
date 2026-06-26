package com.garrettbutchko.minimate.viewModels

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

fun ByteArray.toBitmap(): Bitmap? {
    if (this.isEmpty()) return null
    return BitmapFactory.decodeByteArray(this, 0, this.size)
}

fun ByteArray.toImageBitmap(): ImageBitmap? {
    return this.toBitmap()?.asImageBitmap()
}