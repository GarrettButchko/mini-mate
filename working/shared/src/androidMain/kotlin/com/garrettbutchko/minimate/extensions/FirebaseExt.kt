package com.garrettbutchko.minimate.extensions
import dev.gitlive.firebase.auth.FirebaseUser
actual fun FirebaseUser.safePhotoURL(override: String?): String? = override ?: this.photoURL
