package com.garrettbutchko.minimate.viewModels

import android.app.Activity
import android.content.Context
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.garrettbutchko.minimate.utilities.randomNonceString
import com.garrettbutchko.minimate.utilities.sha256
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.FirebaseUser

/**
 * Signs in the user using Apple and Firebase via KMP (Android implementation).
 *
 * Uses Firebase's native "Sign in with Apple" OAuth flow (Chrome Custom Tab) rather
 * than a third-party Apple SDK. The native [FirebaseAuth] singleton is the same one
 * backing the gitlive `Firebase.auth` wrapper, so `authRepository.currentUser` reflects
 * the result immediately after the flow completes.
 *
 * @param context The Activity context (required to launch the sign-in UI).
 * @param completion Callback with the sign-in result plus the name/email/appleId extracted
 *   from Apple's identity data (name/email are usually only present on first sign-in).
 */
fun AuthViewModel.signInWithApple(
    context: Context,
    completion: (Result<FirebaseUser?>, String?, String?, String?) -> Unit
) {
    val activity = context as? Activity
    if (activity == null) {
        setLoading(false)
        completion(
            Result.failure(Exception("Apple sign-in requires an Activity context")),
            null, null, null
        )
        return
    }

    // Apple requires a nonce to guard against replay attacks. Firebase validates the
    // hashed nonce we send against the value Apple returns in the identity token.
    val rawNonce = randomNonceString()
    currentNonce = rawNonce
    val provider = OAuthProvider.newBuilder("apple.com")
        .addCustomParameter("nonce", sha256(rawNonce))
        .build()

    setLoading(true)

    val firebaseAuth = FirebaseAuth.getInstance()
    val task = firebaseAuth.pendingAuthResult
        ?: firebaseAuth.startActivityForSignInWithProvider(activity, provider)

    task.addOnSuccessListener { authResult -> handleAppleAuthResult(authResult, completion) }
        .addOnFailureListener { error ->
            setLoading(false)
            completion(Result.failure(error), null, null, null)
        }
}

/**
 * Reauthenticates the currently signed-in Apple user and hands back the KMP
 * `AuthCredential` (Android implementation).
 *
 * Uses Firebase's native "Sign in with Apple" OAuth flow (Chrome Custom Tab) via
 * [FirebaseUser.startActivityForReauthenticateWithProvider], then wraps the resulting
 * native credential so callers can pass it to [AuthViewModel.deleteAccount].
 *
 * @param context The Activity context (required to launch the re-authentication UI).
 * @param completion Callback with the Result<AuthCredential>.
 */
fun AuthViewModel.reauthenticateWithApple(
    context: Context,
    completion: (Result<AuthCredential>) -> Unit
) {
    val activity = context as? Activity
    if (activity == null) {
        completion(Result.failure(Exception("Apple re-authentication requires an Activity context")))
        return
    }

    val user = FirebaseAuth.getInstance().currentUser
    if (user == null) {
        completion(Result.failure(Exception("No signed-in user")))
        return
    }

    val rawNonce = randomNonceString()
    currentNonce = rawNonce
    val provider = OAuthProvider.newBuilder("apple.com")
        .addCustomParameter("nonce", sha256(rawNonce))
        .build()

    setLoading(true)

    user.startActivityForReauthenticateWithProvider(activity, provider)
        .addOnSuccessListener { authResult ->
            setLoading(false)
            val nativeCredential = authResult.credential
            if (nativeCredential == null) {
                completion(Result.failure(Exception("Apple re-authentication returned no credential")))
            } else {
                completion(Result.success(AuthCredential(nativeCredential)))
            }
        }
        .addOnFailureListener { error ->
            setLoading(false)
            completion(Result.failure(error))
        }
}

private fun AuthViewModel.handleAppleAuthResult(
    authResult: AuthResult,
    completion: (Result<FirebaseUser?>, String?, String?, String?) -> Unit
) {
    val nativeUser = authResult.user
    if (nativeUser == null) {
        setLoading(false)
        completion(Result.failure(Exception("Apple sign-in returned no user")), null, null, null)
        return
    }

    val profile = authResult.additionalUserInfo?.profile
    val email = nativeUser.email ?: (profile?.get("email") as? String)
    val name = (profile?.get("name") as? Map<*, *>)?.let {
        val first = it["firstName"] as? String ?: ""
        val last = it["lastName"] as? String ?: ""
        "$first $last".trim().ifEmpty { null }
    }
    val appleId = nativeUser.uid

    // currentUser reads from the shared native FirebaseAuth instance, so the newly
    // signed-in user is already available as a gitlive FirebaseUser here.
    completion(Result.success(authRepository.currentUser), name, email, appleId)
}
