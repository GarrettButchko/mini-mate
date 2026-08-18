@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package com.garrettbutchko.minimate.viewModelsiOSAddOns

import com.garrettbutchko.minimate.dataModels.gameModels.Game
import com.garrettbutchko.minimate.enums.SignInMethod
import com.garrettbutchko.minimate.utilities.randomNonceString
import com.garrettbutchko.minimate.utilities.sha256
import com.garrettbutchko.minimate.viewModels.AuthFlowCallbacks
import com.garrettbutchko.minimate.viewModels.AuthViewModel
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.OAuthProvider
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.launch
import platform.AuthenticationServices.*
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create

/** iOS-only shim: re-read the current Firebase user into the VM and re-sync the identifier. */
fun AuthViewModel.refreshUID() {
    _firebaseUser.value = authRepository.currentUser
    syncCurrentUserIdentifier()
}

fun AuthViewModel.signInWithGoogleAndHandleResult(
    idToken: String,
    accessToken: String?,
    photoUrl: String?,
    email: String?,
    guestGame: Game? = null,
    callbacks: AuthFlowCallbacks = AuthFlowCallbacks()
) {
    val credential = GoogleAuthProvider.credential(idToken, accessToken)
    setLoading(true)
    coroutineScope.launch {
        val result = authRepository.signInWithCredential(credential)
        handleSignInResult(
            result = result,
            signInMethod = SignInMethod.GOOGLE,
            photoUrl = photoUrl,
            email = email,
            callbacks = callbacks
        )
    }
}

fun AuthViewModel.handleSignInWithAppleRequest(request: ASAuthorizationAppleIDRequest) {
    // Request full name and email scopes to ensure we get user info on first sign-in
    request.requestedScopes = listOf(ASAuthorizationScopeFullName, ASAuthorizationScopeEmail)
    
    val nonce = randomNonceString()
    currentNonce = nonce
    request.nonce = sha256(nonce)
}

fun AuthViewModel.signInWithApple(
    authorization: ASAuthorization,
    guestGame: Game? = null,
    callbacks: AuthFlowCallbacks = AuthFlowCallbacks()
) {
    val appleCred = authorization.credential as? ASAuthorizationAppleIDCredential
    val nonce = currentNonce
    val tokenData = appleCred?.identityToken

    if (appleCred == null || nonce == null || tokenData == null) {
        postAuthEvent("Invalid Apple credential", true)
        return
    }

    val idToken = NSString.create(data = tokenData, encoding = NSUTF8StringEncoding)?.toString()
    if (idToken == null) {
        postAuthEvent("Could not parse identity token", true)
        return
    }

    val accessTokenData = appleCred.authorizationCode
    val accessToken = if (accessTokenData != null) {
        NSString.create(data = accessTokenData, encoding = NSUTF8StringEncoding)?.toString()
    } else null
    
    // Extract name and email if provided (usually only on first sign-in)
    val name = appleCred.fullName?.let {
        val first = it.givenName ?: ""
        val last = it.familyName ?: ""
        "$first $last".trim().ifEmpty { null }
    }
    val appleEmail = appleCred.email

    val appleId = appleCred.user

    val oauthCred = OAuthProvider.credential(
        providerId = "apple.com",
        idToken = idToken,
        accessToken = accessToken,
        rawNonce = nonce
    )

    setLoading(true)
    coroutineScope.launch {
        val result = authRepository.signInWithCredential(oauthCred)

        // Hand over to the shared sign-in result handler
        handleSignInResult(
            result = result,
            signInMethod = SignInMethod.APPLE,
            name = name,
            email = appleEmail,
            appleId = appleId,
            guestGame = guestGame,
            callbacks = callbacks
        )
    }
}

fun AuthViewModel.deleteAppleAccount(
    authorization: ASAuthorization,
    completion: (Result<Unit>) -> Unit
) {
    val appleCred = authorization.credential as? ASAuthorizationAppleIDCredential
    val nonce = currentNonce
    val tokenData = appleCred?.identityToken
    
    if (appleCred == null || nonce == null || tokenData == null) {
        completion(Result.failure(Exception("Invalid Apple credential")))
        return
    }

    val idToken = NSString.create(data = tokenData, encoding = NSUTF8StringEncoding)?.toString()
    if (idToken == null) {
        completion(Result.failure(Exception("Could not parse identity token")))
        return
    }

    val oauthCred = OAuthProvider.credential(
        providerId = "apple.com",
        idToken = idToken,
        accessToken = null,
        rawNonce = nonce
    )

    coroutineScope.launch {
        val result = deleteAccount(oauthCred)
        completion(result)
    }
}
