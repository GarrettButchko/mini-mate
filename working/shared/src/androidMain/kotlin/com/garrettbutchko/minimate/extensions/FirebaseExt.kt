package com.garrettbutchko.minimate.extensions
import dev.gitlive.firebase.auth.FirebaseUser
actual fun FirebaseUser.safePhotoURL(): String? = this.photoURL
