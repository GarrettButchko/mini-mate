package com.garrettbutchko.minimate.extensions
import dev.gitlive.firebase.auth.FirebaseUser
expect fun FirebaseUser.safePhotoURL(): String?
