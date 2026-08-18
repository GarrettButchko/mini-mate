package com.garrettbutchko.minimate.viewModels

import android.content.Context
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.garrettbutchko.minimate.dataModels.gameModels.Game
import com.garrettbutchko.minimate.enums.SignInMethod
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

private fun parseGoogleIdTokenCredential(credential: Credential): GoogleIdTokenCredential {
    return when (credential) {
        is CustomCredential -> {
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                GoogleIdTokenCredential.createFrom(credential.data)
            } else {
                throw IllegalStateException("Unexpected custom credential type: ${credential.type}")
            }
        }

        else -> throw IllegalStateException("Unexpected credential type: ${credential.type}")
    }
}

/**
 * Signs in the user using Google Sign-In and Firebase via KMP (Android implementation).
 *
 * @param context The Android context (should be an Activity context for Credential Manager to show UI).
 * @param serverClientId The Web Client ID for your project (from Google Cloud Console or google-services.json).
 * @param completion Callback with the result of the sign-in operation.
 */
fun AuthViewModel.signInWithGoogle(
    context: Context,
    serverClientId: String,
    completion: (Result<FirebaseUser?>, String?, String?) -> Unit
) {
    val credentialManager = CredentialManager.create(context.applicationContext)
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(serverClientId)
        .setAutoSelectEnabled(false)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    coroutineScope.launch {
        // Turn the loading flag on before the picker shows so the SignInView's
        // "Signing you in..." overlay is already up when the picker returns
        // (instead of only being set inside createOrSignInUserAndNavigateToHome,
        // after the credential round-trip already ran).
        setLoading(true)
        try {
            val result = credentialManager.getCredential(context, request)
            val googleCredential = parseGoogleIdTokenCredential(result.credential)
            val photoUrl = googleCredential.profilePictureUri?.toString()
            val email = googleCredential.id
            // Credential Manager Google ID flow returns an ID token; access token is null here.
            signInWithGoogleTokens(googleCredential.idToken, null) { firebaseResult ->
                // On success, handleSignInResult keeps the flag set through the
                // user-load round trip and clears it when done.
                completion(firebaseResult, photoUrl, email)
            }
        } catch (e: GetCredentialException) {
            setLoading(false)
            completion(Result.failure(e), null, null)
        } catch (e: Exception) {
            setLoading(false)
            completion(Result.failure(e), null, null)
        }
    }
}

/**
 * Reauthenticate a Google user and hand back the KMP `AuthCredential`.
 *
 * @param context The Android context.
 * @param serverClientId The Web Client ID.
 * @param loginHint The email of the currently signed-in user to prevent account mismatch.
 * @param completion Callback with the Result<AuthCredential>.
 */
fun AuthViewModel.reauthenticateWithGoogle(
    context: Context,
    serverClientId: String,
    loginHint: String? = null,
    completion: (Result<AuthCredential>) -> Unit
) {
    val credentialManager = CredentialManager.create(context.applicationContext)
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(serverClientId)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    coroutineScope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val googleCredential = parseGoogleIdTokenCredential(result.credential)
            // Verify the credential matches the expected account
            if (loginHint != null && googleCredential.id != loginHint) {
                completion(Result.failure(Exception("Please select the account you are signed in with: $loginHint")))
                return@launch
            }
            // Credential Manager Google ID flow returns an ID token; access token is null here.
            getGoogleCredential(googleCredential.idToken, null) { credRes ->
                completion(credRes)
            }
        } catch (e: Exception) {
            completion(Result.failure(e))
        }
    }
}



fun getGoogleCredential(
    idToken: String,
    accessToken: String?,
    completion: (Result<AuthCredential>) -> Unit
) {
    try {
        val credential = GoogleAuthProvider.credential(idToken, accessToken)
        completion(Result.success(credential))
    } catch (e: Exception) {
        completion(Result.failure(e))
    }
}

/**
 * Android counterpart to iOS's AuthViewModelIOS.kt::signInWithGoogleAndHandleResult — same
 * "chain platform sign-in straight into handleSignInResult" shape, using Android's own
 * Credential Manager flow instead of iOS's raw idToken/accessToken inputs. Lets callers
 * (App.kt in both apps) wire one call instead of manually chaining signInWithGoogle +
 * handleSignInResult with nested lambdas at every call site.
 *
 * No guestGame param — handleSignInResult doesn't take one (Google sign-in doesn't merge
 * guest games in this app, only Apple/email do); iOS's version has the same unused param,
 * not worth copying here. Errors surface via [AuthViewModel.authEvent] instead of a
 * callback — collect it once per screen instead of wiring a lambda through every call.
 */
fun AuthViewModel.signInWithGoogleAndHandleResult(
    context: Context,
    serverClientId: String,
    callbacks: AuthFlowCallbacks = AuthFlowCallbacks()
) {
    signInWithGoogle(context, serverClientId) { result, photoUrl, email ->
        handleSignInResult(
            result = result,
            signInMethod = SignInMethod.GOOGLE,
            photoUrl = photoUrl,
            email = email,
            callbacks = callbacks
        )
    }
}

