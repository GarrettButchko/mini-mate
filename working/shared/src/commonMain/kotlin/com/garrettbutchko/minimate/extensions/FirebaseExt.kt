package com.garrettbutchko.minimate.extensions
import dev.gitlive.firebase.auth.FirebaseUser

/**
 * Platform-safe read of the user's photo URL. iOS can't reliably read the raw
 * `photoURL` property (see the iosMain actual), so this is the only way sign-in code
 * should touch it. [override] wins when the caller already has a better URL on hand
 * (e.g. from the platform sign-in SDK's own profile data) — falls back to the
 * platform-safe read otherwise.
 */
expect fun FirebaseUser.safePhotoURL(override: String? = null): String?
