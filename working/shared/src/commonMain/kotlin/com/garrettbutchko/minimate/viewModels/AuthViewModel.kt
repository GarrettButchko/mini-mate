package com.garrettbutchko.minimate.viewModels

import co.touchlab.skie.configuration.annotations.DefaultArgumentInterop
import co.touchlab.kermit.Logger
import com.garrettbutchko.minimate.dataModels.gameModels.Game
import com.garrettbutchko.minimate.dataModels.userModels.UserModel
import com.garrettbutchko.minimate.enums.SignInMethod
import com.garrettbutchko.minimate.repositories.FirebaseAuthRepository
import com.garrettbutchko.minimate.repositories.userRepos.UserRepository
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.garrettbutchko.minimate.interfaces.AppNavigationManaging
import com.garrettbutchko.minimate.extensions.safePhotoURL
import dev.gitlive.firebase.auth.EmailAuthProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class FirebaseUserSmallData (
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoURL: String?
)

open class AuthViewModel(
    val authRepository: FirebaseAuthRepository,
    val viewManager: AppNavigationManaging,
    val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : KoinComponent {
    
    // Breaking circular dependency with lazy injection
    val userRepository: UserRepository by inject()

    private val log = Logger.withTag("AuthViewModel")

    private val _firebaseUser = MutableStateFlow<FirebaseUser?>(authRepository.currentUser)
    val firebaseUser: StateFlow<FirebaseUser?> = _firebaseUser.asStateFlow()

    private val _currentUserIdentifier = MutableStateFlow(authRepository.currentUser?.uid)
    val currentUserIdentifierFlow: StateFlow<String?> = _currentUserIdentifier.asStateFlow()

    private val _userModel = MutableStateFlow<UserModel?>(null)
    val userModel: StateFlow<UserModel?> = _userModel.asStateFlow()

    private val _isLoadingUser = MutableStateFlow(false)
    val isLoadingUser: StateFlow<Boolean> = _isLoadingUser.asStateFlow()

    /**
     * True once loadOrCreateUser has fully reconciled local + remote this session
     * (either via the sign-in pipeline or a landing screen's cold-start load).
     * Landing screens use this — not `userModel == null` — to decide whether to
     * load, because [hydrateUserFromLocal] populates userModel from the SQLite
     * cache before any reconcile has happened.
     */
    var hasSyncedUserThisSession: Boolean = false
        private set

    fun markUserSynced() {
        hasSyncedUserThisSession = true
    }

    var currentNonce: String? = null

    init {
        // Hydrate the in-memory user model from the local DB immediately so a
        // returning user's cached profile shows on the first frame instead of a
        // blank placeholder while startup network work (IAP init, session
        // verification) runs. The later loadOrCreateUser reconcile overwrites
        // this with fresh data.
        hydrateUserFromLocal()
    }

    private fun hydrateUserFromLocal() {
        val uid = authRepository.currentUser?.uid ?: return
        coroutineScope.launch {
            val local = userRepository.localRepo.fetch(uid)
            // Don't clobber a model the sign-in pipeline set in the meantime
            if (local != null && _userModel.value == null) {
                _userModel.value = local
            }
        }
    }

    val currentUserIdentifier: String?
        get() = _currentUserIdentifier.value

    private fun syncCurrentUserIdentifier() {
        _currentUserIdentifier.value = _firebaseUser.value?.uid
    }

    fun setUserModel(userModel: UserModel?) {
        _userModel.value = userModel
    }

    fun setLoading(state: Boolean) {
        _isLoadingUser.value = state
    }

    fun updateUserName(name: String) {
        val currentUser = _userModel.value
        if (currentUser != null) {
            _userModel.value = currentUser.copy(name = name)
        }
    }

    /**
     * Async check run at startup. If the Firebase account was deleted on another device,
     * cleans up all local and remote data and navigates back to sign-in.
     * Returns true if the session is still valid (or offline), false if cleaned up.
     */
    suspend fun verifyAndCleanupIfDeleted(): Boolean {
        val result = authRepository.verifyAuthStillValid()

        // Network/unknown error → can't confirm deletion, leave user signed in
        if (result.isFailure) return true
        // Valid session → proceed normally
        if (result.getOrDefault(true)) return true

        log.i("Account deleted on another device — cleaning up")
        val userId = currentUserIdentifier

        runCatching { authRepository.logout() }
        if (userId != null) {
            runCatching { userRepository.deleteUnified(userId) }
        }
        _firebaseUser.value = null
        syncCurrentUserIdentifier()
        _userModel.value = null
        viewManager.navigateToSignIn()
        return false
    }

    fun refreshUID() {
        _firebaseUser.value = authRepository.currentUser
        syncCurrentUserIdentifier()
    }

    suspend fun refreshVerificationStatus(): Boolean {
        return authRepository.refreshVerificationStatus()
    }

    suspend fun createUser(email: String, password: String): Result<FirebaseUser?> {
        val result = authRepository.createUser(email, password)
        if (result.isSuccess) {
            _firebaseUser.value = result.getOrNull()
            syncCurrentUserIdentifier()
        }
        return result
    }

    suspend fun signIn(email: String, password: String): Result<FirebaseUser?> {
        val result = authRepository.signIn(email, password)
        if (result.isSuccess) {
            _firebaseUser.value = result.getOrNull()
            syncCurrentUserIdentifier()
        }
        return result
    }

    open fun logout() {
        coroutineScope.launch {
            authRepository.logout()
            _firebaseUser.value = null
            syncCurrentUserIdentifier()
        }
    }

    @DefaultArgumentInterop.Enabled
    suspend fun deleteAccount(credential: AuthCredential? = null): Result<Unit> {
        return if (credential != null) {
            val result = authRepository.deleteAccount(credential)
            if (result.isSuccess) {
                _firebaseUser.value = null
                syncCurrentUserIdentifier()
            }
            result
        } else {
            Result.failure(Exception("Missing credentials for deletion"))
        }
    }

    suspend fun reauthenticateWithEmail(email: String, password: String): Result<AuthCredential> {
        return try {
            // Firebase uses EmailAuthProvider.credential
            val credential = EmailAuthProvider.credential(email, password)
            val result = authRepository.reauthenticate(credential)
            if (result.isSuccess) {
                Result.success(credential)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Unknown reauthentication error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    open suspend fun reauthenticateWithGoogle(): Result<AuthCredential> {
        return Result.failure(Exception("Not implemented on this platform"))
    }

    fun signInWithGoogleTokens(
        idToken: String,
        accessToken: String?,
        completion: (Result<FirebaseUser?>) -> Unit
    ) {
        val credential = GoogleAuthProvider.credential(idToken, accessToken)

        coroutineScope.launch {
            val result = authRepository.signInWithCredential(credential)
            if (result.isSuccess) {
                val user = result.getOrNull()
                _firebaseUser.value = user
                syncCurrentUserIdentifier()
                completion(Result.success(user))
            } else {
                completion(Result.failure(result.exceptionOrNull() ?: Exception("Unknown error during Google sign in")))
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

    @DefaultArgumentInterop.Enabled
    fun createOrSignInUserAndNavigateToHome(
        user: FirebaseUserSmallData,
        name: String? = null,
        signInMethod: SignInMethod? = null,
        appleId: String? = null,
        navToHome: Boolean = true,
        guestGame: Game? = null,
        onErrorMessage: (String?, Boolean) -> Unit,
        onClearGuestGame: (Game?) -> Unit,
        completion: () -> Unit = {}
    ) {
        onErrorMessage(null, false)
        coroutineScope.launch {
            _isLoadingUser.value = true
            try {
                // Wait for UserRepository to resolve the user
                val loadedUser = userRepository.loadOrCreateUser(
                    id = user.uid,
                    firebaseUser = user,
                    name = user.displayName ?: name,
                    signInMethod = signInMethod,
                    appleId = appleId,
                    guestGame = guestGame
                ) { immediateLocalUser ->
                    setUserModel(immediateLocalUser)
                }
                
                // Final fully-resolved remote/local reconciled user
                setUserModel(loadedUser)
                hasSyncedUserThisSession = true
                
                // If the guest game was used as part of creation, we clear it out.
                // UserRepository doesn't expose a "creation" boolean directly in loadOrCreateUser anymore, 
                // but we can just invoke onClearGuestGame if a guestGame was provided.
                if (guestGame != null) {
                    onClearGuestGame(null)
                }

                if (navToHome) {
                    viewManager.navigateAfterSignIn()
                }
            } catch (e: Exception) {
                log.e(e) { "Failed to load or create user" }
                onErrorMessage(e.message, false)
            } finally {
                _isLoadingUser.value = false
                completion()
            }
        }
    }

    @DefaultArgumentInterop.Enabled
    fun signUpUIManage(
        emailInput: String,
        passwordInput: String,
        guestGame: Game? = null,
        onClearForm: () -> Unit,
        onSuccess: () -> Unit,
        onErrorMessage: (String?, Boolean) -> Unit,
        onClearGuestGame: (Game?) -> Unit
    ) {
        coroutineScope.launch {
            _isLoadingUser.value = true
            val result = createUser(emailInput, passwordInput)

            if (result.isFailure) {
                _isLoadingUser.value = false
                onErrorMessage(result.exceptionOrNull()?.message ?: "Sign up failed", false)
                return@launch
            }

            val firebaseUser = result.getOrNull()
            if (firebaseUser != null) {
                val userData = FirebaseUserSmallData(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = firebaseUser.displayName,
                    photoURL = firebaseUser.safePhotoURL()
                )

                createOrSignInUserAndNavigateToHome(
                    user = userData,
                    signInMethod = SignInMethod.EMAIL,
                    navToHome = false,
                    guestGame = guestGame,
                    onErrorMessage = onErrorMessage,
                    onClearGuestGame = onClearGuestGame
                ) {
                    coroutineScope.launch {
                        // Keep the loading state alive through the verification
                        // email step (createOrSignIn's finally just cleared it).
                        _isLoadingUser.value = true
                        val verificationResult = authRepository.sendEmailVerification()

                        if (verificationResult.isFailure) {
                            val error = verificationResult.exceptionOrNull()?.message ?: "Unknown error"
                            onErrorMessage("Couldn’t send verification email: $error", false)
                        } else {
                            onClearForm()
                            onSuccess()
                            onErrorMessage("Please Verify Your Email To Continue", true)
                            logout()
                        }
                        _isLoadingUser.value = false
                    }
                }
            } else {
                _isLoadingUser.value = false
            }
        }
    }

    @DefaultArgumentInterop.Enabled
    fun handleSignInResult(
        result: Result<FirebaseUser?>,
        photoUrl: String? = null,
        email: String? = null,
        onErrorMessage: (String?, Boolean) -> Unit,
        onClearGuestGame: (Game?) -> Unit
    ) {
        result.fold(
            onSuccess = { firebaseUser ->
                if (firebaseUser != null) {
                    _firebaseUser.value = firebaseUser
                    syncCurrentUserIdentifier()

                    val newPhotoURL = photoUrl ?: firebaseUser.photoURL

                    val userData = FirebaseUserSmallData(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email ?: email,
                        displayName = firebaseUser.displayName,
                        photoURL = newPhotoURL
                    )
                    createOrSignInUserAndNavigateToHome(
                        user = userData,
                        signInMethod = SignInMethod.GOOGLE,
                        onErrorMessage = onErrorMessage,
                        onClearGuestGame = onClearGuestGame
                    )
                } else {
                    _isLoadingUser.value = false
                    onErrorMessage("User data is missing.", true)
                }
            },
            onFailure = { error ->
                _isLoadingUser.value = false
                onErrorMessage(error.message ?: "An unknown error occurred", true)
            }
        )
    }

    @DefaultArgumentInterop.Enabled
    fun handleAppleSignInResult(
        result: Result<FirebaseUser?>,
        name: String? = null,
        email: String? = null,
        appleId: String? = null,
        guestGame: Game? = null,
        onErrorMessage: (String?, Boolean) -> Unit,
        onClearGuestGame: (Game?) -> Unit
    ) {
        result.fold(
            onSuccess = { firebaseUser ->
                if (firebaseUser != null) {
                    _firebaseUser.value = firebaseUser
                    syncCurrentUserIdentifier()
                    val userData = FirebaseUserSmallData(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email ?: email,
                        displayName = firebaseUser.displayName,
                        photoURL = firebaseUser.safePhotoURL()
                    )
                    createOrSignInUserAndNavigateToHome(
                        user = userData,
                        name = name,
                        signInMethod = SignInMethod.APPLE,
                        appleId = appleId,
                        guestGame = guestGame,
                        onErrorMessage = onErrorMessage,
                        onClearGuestGame = onClearGuestGame
                    )
                } else {
                    _isLoadingUser.value = false
                    onErrorMessage("User data is missing.", true)
                }
            },
            onFailure = { error ->
                _isLoadingUser.value = false
                onErrorMessage(error.message ?: "An unknown error occurred", true)
            }
        )
    }

    @DefaultArgumentInterop.Enabled
    fun signInUIManage(
        emailInput: String,
        passwordInput: String,
        guestGame: Game? = null,
        onClearForm: () -> Unit,
        onShowSignUp: () -> Unit,
        onErrorMessage: (String?, Boolean) -> Unit,
        onClearGuestGame: (Game?) -> Unit
    ) {
        coroutineScope.launch {
            _isLoadingUser.value = true
            val result = signIn(emailInput, passwordInput)

            if (result.isFailure) {
                _isLoadingUser.value = false
                onShowSignUp()
                onErrorMessage("No User Found Please Sign Up", false)
                return@launch
            }
            
            val firebaseUser = result.getOrNull()

            if (firebaseUser != null) {
                if (firebaseUser.isEmailVerified) {

                    val userData = FirebaseUserSmallData(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email,
                        displayName = firebaseUser.displayName,
                        photoURL = firebaseUser.safePhotoURL()
                    )

                    createOrSignInUserAndNavigateToHome(
                        user = userData,
                        signInMethod = SignInMethod.EMAIL,
                        guestGame = guestGame,
                        onErrorMessage = onErrorMessage,
                        onClearGuestGame = onClearGuestGame
                    ){}

                } else {

                    val verificationResult = authRepository.sendEmailVerification()

                    if (verificationResult.isFailure) {

                        val error = verificationResult.exceptionOrNull()?.message ?: "Unknown error"

                        onErrorMessage("Couldn’t send verification email: ${error}", false)
                    } else {
                        onClearForm()
                        onErrorMessage("Please Verify Your Email To Continue", true)
                        logout() // Logout AFTER email is sent
                    }
                    _isLoadingUser.value = false
                }
            } else {
                _isLoadingUser.value = false
            }
        }
    }
}