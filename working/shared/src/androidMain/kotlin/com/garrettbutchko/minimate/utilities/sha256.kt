package com.garrettbutchko.minimate.utilities

import java.security.MessageDigest

actual fun sha256(text: String): String {
    // 1. Convert input string to UTF-8 bytes
    val bytes = text.encodeToByteArray()
    
    // 2. Get SHA-256 MessageDigest instance
    val md = MessageDigest.getInstance("SHA-256")
    
    // 3. Compute the hash
    val digest = md.digest(bytes)
    
    // 4. Convert byte array to a hex string (matches your iOS formatting)
    return digest.joinToString("") {
        it.toInt().and(0xFF).toString(16).padStart(2, '0')
    }
}
