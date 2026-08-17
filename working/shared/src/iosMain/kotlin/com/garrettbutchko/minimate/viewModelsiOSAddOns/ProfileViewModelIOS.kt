@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package com.garrettbutchko.minimate.viewModelsiOSAddOns

import co.touchlab.kermit.Logger
import com.garrettbutchko.minimate.dataModels.userModels.UserModel
import com.garrettbutchko.minimate.utilities.randomNonceString
import com.garrettbutchko.minimate.utilities.sha256
import dev.gitlive.firebase.auth.OAuthProvider
import dev.gitlive.firebase.storage.Data
import kotlinx.coroutines.launch
import platform.AuthenticationServices.*
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import com.garrettbutchko.minimate.auth.AppleRauthCoordinator
import com.garrettbutchko.minimate.viewModels.ProfileViewModel
import dev.gitlive.firebase.auth.GoogleAuthProvider
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi

private var activeReauthCoordinator: AppleRauthCoordinator? = null
private var currentNonce: String? = null
private val log = Logger.withTag("ProfileViewModelIOS")

/**
 * Finishing step for iOS's Google reauth-and-delete: ProfileViewModelSwift.swift runs
 * Apple's native Google Sign-In flow itself (Swift-side, via AuthViewModelSwift), then
 * hands the resulting idToken/accessToken here to build the credential and delegate to
 * the shared [ProfileViewModel.handleDeleteAccount]. Android's equivalent
 * (ProfileViewModelAndroid.kt) already has a full AuthCredential by the time it reaches
 * Kotlin, so it calls handleDeleteAccount directly instead of going through this.
 */
fun ProfileViewModel.googleReauthAndDelete(idToken: String, accessToken: String?, isSheetPresented: (Boolean) -> Unit) {
    try {
        val credential = GoogleAuthProvider.credential(idToken, accessToken)
        handleDeleteAccount(credential, isSheetPresented)
    } catch (e: Exception) {
        setBotMessage(e.message ?: "Unknown error")
        setIsRed(true)
    }
}

fun ProfileViewModel.startAppleReauthAndDelete(onSheetPresentChange: (Boolean) -> Unit) {
    val provider = ASAuthorizationAppleIDProvider()
    val request = provider.createRequest()
    request.requestedScopes = listOf<ASAuthorizationScope>()

    val nonce = randomNonceString()
    currentNonce = nonce
    // Apple requires a SHA256 hash of the raw nonce.
    request.nonce = sha256(nonce)

    val coordinator = AppleRauthCoordinator { result ->
        activeReauthCoordinator = null // Clear strong reference
        result.onSuccess { authorization ->
            deleteAppleAccount(authorization) { deletionSuccess ->
                if (deletionSuccess) {
                    onSheetPresentChange(false)
                    viewManager.navigateToSignIn()
                }
            }
        }.onFailure { error ->
            setBotMessage(error.message ?: "Authorization failed")
            setIsRed(true)
        }
    }
    
    activeReauthCoordinator = coordinator // Retain the coordinator

    val controller = ASAuthorizationController(listOf(request))
    controller.delegate = coordinator
    controller.presentationContextProvider = coordinator
    controller.performRequests()
}

private fun ProfileViewModel.deleteAppleAccount(
    authorization: ASAuthorization,
    completion: (Boolean) -> Unit
) {
    val appleCred = authorization.credential as? ASAuthorizationAppleIDCredential
    val nonce = currentNonce
    val tokenData = appleCred?.identityToken
    
    if (appleCred == null || nonce == null || tokenData == null) {
        setBotMessage("Invalid Apple credential")
        setIsRed(true)
        completion(false)
        return
    }

    val idToken = NSString.create(data = tokenData, encoding = NSUTF8StringEncoding) as String?
    if (idToken == null) {
        setBotMessage("Could not parse identity token")
        setIsRed(true)
        completion(false)
        return
    }

    val oauthCred = OAuthProvider.credential(providerId = "apple.com", idToken = idToken, accessToken = null, rawNonce = nonce)

    coroutineScope.launch {
        val userModel = authModel.userModel.value
        if (userModel != null) {
            // Full document replace — keeps googleId, appleId, name, email; nulls everything else.
            // The cleanupOrphanedGames cloud trigger fires on this write and handles game deletion.
            val stub = UserModel(
                googleId = userModel.googleId,
                appleId = userModel.appleId,
                name = userModel.name,
                email = userModel.email,
                accountType = userModel.accountType
            )
            userRemoteRepo.saveReplace(stub)
            // Clean up local games and local user doc.
            localGameRepo.deleteByIds(userModel.gameIDs)
            userRepo.localRepo.delete(userModel.googleId)
        }
        // Sign out without deleting the Firebase Auth account.
        authModel.logout()
        completion(true)
    }
}

fun ProfileViewModel.managePictureChange(newImage: UIImage?) {
    val userId = authModel.currentUserIdentifier
    if (newImage == null || userId == null) {
        setBotMessage("Photo upload failed: Missing image or user ID")
        setIsRed(true)
        return
    }

    val jpegData = UIImageJPEGRepresentation(newImage, 0.8)
    if (jpegData == null) {
        setBotMessage("Photo upload failed: Could not process image")
        setIsRed(true)
        return
    }

    val data = Data(jpegData)

    coroutineScope.launch {
        val result = userRepo.uploadProfilePhoto(userId, data)
        if (result.isSuccess) {
            log.i { "✅ Photo URL: ${result.getOrNull()}" }
        } else {
            setBotMessage("Photo upload failed: ${result.exceptionOrNull()?.message}")
            setIsRed(true)
        }
    }
}
