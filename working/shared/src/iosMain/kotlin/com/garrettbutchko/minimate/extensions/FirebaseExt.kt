package com.garrettbutchko.minimate.extensions
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual fun FirebaseUser.safePhotoURL(): String? {
    return null
}
