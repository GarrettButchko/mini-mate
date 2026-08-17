package com.garrettbutchko.minimate.viewModels

import co.touchlab.kermit.Logger
import com.garrettbutchko.minimate.dataModels.userModels.UserModel
import com.garrettbutchko.minimate.interfaces.AppNavigationManaging
import com.garrettbutchko.minimate.repositories.gameRepos.LocalGameRepository
import com.garrettbutchko.minimate.repositories.userRepos.RemoteUserRepository
import com.garrettbutchko.minimate.repositories.userRepos.UserRepository
import dev.gitlive.firebase.auth.AuthCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class DeleteAlertType {
    GOOGLE,
    APPLE,
    EMAIL
}

class ProfileViewModel(
    val authModel: AuthViewModel,
    val userRepo: UserRepository,
    val userRemoteRepo: RemoteUserRepository,
    val localGameRepo: LocalGameRepository,
    val viewManager: AppNavigationManaging,
    val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    // Reuse AuthViewModel's already-injected repository instead of constructing a
    // second, disconnected FirebaseAuthRepository instance.
    private val authRepository get() = authModel.authRepository
    private val log = Logger.withTag("ProfileViewModel")

    // MARK: - Published UI State

    // iOS-only: drives ProfileView.swift's `.alert("Edit Name", isPresented: $viewModel.editProfile)`.
    // Android's edit-name dialog owns its own local Compose state instead (see ProfileView.kt).
    // Stays here rather than in an iOS-only extension because Kotlin extension functions can add
    // behavior to a class but not backing state — a StateFlow has to live on the class itself.
    private val _editProfile = MutableStateFlow(false)
    val editProfile: StateFlow<Boolean> = _editProfile.asStateFlow()

    private val _botMessage = MutableStateFlow("")
    val botMessage: StateFlow<String> = _botMessage.asStateFlow()

    private val _isRed = MutableStateFlow(true)
    val isRed: StateFlow<Boolean> = _isRed.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    // Drives both platforms' delete-confirmation alert: iOS's
    // `.alert(item: $viewModel.activeDeleteAlert)`, and Android's equivalent in ProfileView.kt.
    private val _activeDeleteAlert = MutableStateFlow<DeleteAlertType?>(null)
    val activeDeleteAlert: StateFlow<DeleteAlertType?> = _activeDeleteAlert.asStateFlow()

    var oldName: String? = null

    // Update state helpers
    fun setEditProfile(value: Boolean) { _editProfile.value = value } // iOS-only, see _editProfile above
    fun setBotMessage(value: String) { _botMessage.value = value }
    fun setIsRed(value: Boolean) { _isRed.value = value }
    fun setName(value: String) { _name.value = value }
    fun setActiveDeleteAlert(value: DeleteAlertType?) { _activeDeleteAlert.value = value }


    fun cleanupLocalData() {
        val userModel = authModel.userModel.value ?: return
        val gameIDs = userModel.gameIDs
        val userID = userModel.googleId

        coroutineScope.launch {
            delay(2000)
            val completed = localGameRepo.deleteByIds(gameIDs)
            if (completed) {
                log.i { "🗑️ Deleted all local games for user" }
            }
            userRepo.deleteUnified(userID)
        }
    }

    fun saveName() {
        if (oldName != _name.value) {
            val userId = authModel.currentUserIdentifier
            if (userId != null) {
                authModel.updateUserName(_name.value)
                val updatedUserModel = authModel.userModel.value
                if (updatedUserModel != null) {
                    coroutineScope.launch {
                        // saveUnified (not userRemoteRepo.save) so the local cache
                        // reflects the rename too, not just the remote document.
                        userRepo.saveUnified(userId, updatedUserModel)
                    }
                }
            }
        }
    }

    fun passwordReset(userModel: UserModel) {
        val targetEmail = userModel.email
        if (targetEmail.isNullOrBlank()) {
            setBotMessage("User has no email")
            setIsRed(true)
            return
        }

        coroutineScope.launch {
            val result = authRepository.sendPasswordReset(targetEmail)
            if (result.isSuccess) {
                setBotMessage("Password reset email sent!")
                setIsRed(false)
            } else {
                setBotMessage(result.exceptionOrNull()?.message ?: "An error occurred")
                setIsRed(true)
            }
        }
    }

    fun logOut() {
        viewManager.navigateToSignIn()
        authModel.logout()
    }

    private fun getDeleteAlertType(userModel: UserModel): DeleteAlertType {
        return when {
            userModel.accountType.contains("google") -> DeleteAlertType.GOOGLE
            userModel.accountType.contains("apple") -> DeleteAlertType.APPLE
            else -> DeleteAlertType.EMAIL
        }
    }

    fun deleteAccount(userModel: UserModel) {
        setActiveDeleteAlert(getDeleteAlertType(userModel))
    }

    fun emailReauthAndDelete(emailInput: String, passwordInput: String, isSheetPresented: (Boolean) -> Unit) {
        coroutineScope.launch {
            val result = authModel.reauthenticateWithEmail(emailInput, passwordInput)
            result.fold(
                onSuccess = { credential ->
                    handleDeleteAccount(
                        credential,
                        isSheetPresented = isSheetPresented
                    )
                },
                onFailure = { error ->
                    setBotMessage(error.message ?: "Unknown error")
                    setIsRed(true)
                }
            )
        }
    }

    fun handleDeleteAccount(credential: AuthCredential, isSheetPresented: (Boolean) -> Unit) {
        coroutineScope.launch {
            val result = authRepository.deleteAccount(credential)
            result.fold(
                onSuccess = {
                    cleanupLocalData()
                    isSheetPresented(false)
                    viewManager.navigateToSignIn()
                },
                onFailure = { error ->
                    setBotMessage(error.message ?: "Unknown error")
                    setIsRed(true)
                }
            )
        }
    }
}
