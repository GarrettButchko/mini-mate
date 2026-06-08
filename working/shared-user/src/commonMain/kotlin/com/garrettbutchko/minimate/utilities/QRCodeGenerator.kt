package com.garrettbutchko.minimate.utilities

import qrcode.QRCode
import qrcode.color.Colors

fun generateQRCodeData(string: String): ByteArray {
    return QRCode.ofSquares()
        .withBackgroundColor(Colors.TRANSPARENT)
        .build(string)
        .renderToBytes()
}
