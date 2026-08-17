package com.garrettbutchko.minimate.viewModelsAndroidAddOns

import android.content.Context
import com.garrettbutchko.minimate.viewModels.ProfileViewModel
import com.garrettbutchko.minimate.viewModels.reauthenticateWithApple
import com.garrettbutchko.minimate.viewModels.reauthenticateWithGoogle

/**
 * Android counterpart to ProfileViewModelIOS.kt — same shape, same method names, platform-
 * specific bodies. Each function does Android's real reauth (reusing the existing
 * AuthViewModel+Google.kt / +Apple.kt extensions, which already produce a proper
 * AuthCredential) and then hands off to the shared ProfileViewModel.handleDeleteAccount(...)
 * "base" method — the same finishing step iOS's Google reauth path already funnels into.
 */

/**
 * Android's edit-name dialog collects the whole new name locally and calls back once on
 * confirm (see ProfileView.kt), unlike iOS's live two-way-bound TextField that keeps
 * ProfileViewModel's `name` StateFlow in sync as the user types across two separate
 * events (opening the dialog sets `oldName`, confirming calls `saveName()`). Android
 * needs both steps done atomically in one call instead.
 */
fun ProfileViewModel.editName(currentName: String?, newName: String) {
    oldName = currentName
    setName(newName)
    saveName()
}

fun ProfileViewModel.googleReauthAndDelete(
    context: Context,
    serverClientId: String,
    isSheetPresented: (Boolean) -> Unit
) {
    authModel.reauthenticateWithGoogle(
        context = context,
        serverClientId = serverClientId,
        loginHint = authModel.userModel.value?.email
    ) { result ->
        result.fold(
            onSuccess = { credential -> handleDeleteAccount(credential, isSheetPresented) },
            onFailure = { error ->
                setBotMessage(error.message ?: "Google re-authentication failed.")
                setIsRed(true)
            }
        )
    }
}

fun ProfileViewModel.appleReauthAndDelete(
    context: Context,
    isSheetPresented: (Boolean) -> Unit
) {
    authModel.reauthenticateWithApple(context) { result ->
        result.fold(
            onSuccess = { credential -> handleDeleteAccount(credential, isSheetPresented) },
            onFailure = { error ->
                setBotMessage(error.message ?: "Apple re-authentication failed.")
                setIsRed(true)
            }
        )
    }
}
