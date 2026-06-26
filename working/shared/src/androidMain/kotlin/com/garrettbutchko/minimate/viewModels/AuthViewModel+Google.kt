package com.garrettbutchko.minimate.viewModels

import android.content.Context
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.FirebaseUser
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
        try {
            val result = credentialManager.getCredential(context, request)
            val googleCredential = parseGoogleIdTokenCredential(result.credential)
            val photoUrl = googleCredential.profilePictureUri?.toString()
            val email = googleCredential.id
            // Credential Manager Google ID flow returns an ID token; access token is null here.
            signInWithGoogleTokens(googleCredential.idToken, null) { firebaseResult ->
                completion(firebaseResult, photoUrl, email)
            }
        } catch (e: GetCredentialException) {
            completion(Result.failure(e), null, null)
        } catch (e: Exception) {
            completion(Result.failure(e), null, null)
        }
    }
}

/**
 * Reauthenticate a Google user and hand back the tokens (Android implementation).
 * Returns a Result containing a Pair of (idToken, accessToken).
 * Note: Access token might be null with Credential Manager for Google.
 *
 * @param context The Android context.
 * @param serverClientId The Web Client ID.
 * @param completion Callback with the tokens result.
 */

fun AuthViewModel.reauthenticateWithGooglePair(
    context: Context,
    serverClientId: String,
    loginHint: String? = null,
    completion: (Result<Pair<String, String?>>) -> Unit
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
            completion(Result.success(googleCredential.idToken to null))
        } catch (e: Exception) {
            completion(Result.failure(e))
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
    val completionPair: (Result<Pair<String, String?>>) -> Unit = { res ->
        res.fold(
            onSuccess = { tokens ->
                getGoogleCredential(tokens.first, tokens.second) { credRes ->
                    completion(credRes)
                }
            },
            onFailure = {
                completion(Result.failure(it))
            }
        )
    }
    reauthenticateWithGooglePair(context, serverClientId, loginHint, completionPair)
}
