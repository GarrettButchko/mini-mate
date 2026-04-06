package com.garrettbutchko.minimate.viewModels

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

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
    completion: (Result<FirebaseUser?>) -> Unit
) {
    val credentialManager = CredentialManager.create(context)
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
            val credential = result.credential
            
            if (credential is GoogleIdTokenCredential) {
                // Pass the idToken and null for accessToken as Credential Manager for Google ID only returns the ID token.
                signInWithGoogleTokens(credential.idToken, null, completion)
            } else {
                completion(Result.failure(Exception("Unexpected credential type: ${credential.type}")))
            }
        } catch (e: GetCredentialException) {
            completion(Result.failure(e))
        } catch (e: Exception) {
            completion(Result.failure(e))
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
fun AuthViewModel.reauthenticateWithGoogle(
    context: Context,
    serverClientId: String,
    completion: (Result<Pair<String, String?>>) -> Unit
) {
    val credentialManager = CredentialManager.create(context)
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(serverClientId)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    coroutineScope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            
            if (credential is GoogleIdTokenCredential) {
                completion(Result.success(credential.idToken to null))
            } else {
                completion(Result.failure(Exception("Unexpected credential type: ${credential.type}")))
            }
        } catch (e: Exception) {
            completion(Result.failure(e))
        }
    }
}
