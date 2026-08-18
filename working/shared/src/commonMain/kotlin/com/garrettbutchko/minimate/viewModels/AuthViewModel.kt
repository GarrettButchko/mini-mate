package com.garrettbutchko.minimate.viewModels

import co.touchlab.skie.configuration.annotations.DefaultArgumentInterop
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.garrettbutchko.minimate.interfaces.AppNavigationManaging
import com.garrettbutchko.minimate.extensions.safePhotoURL
import com.garrettbutchko.minimate.utilities.AppLogger
import dev.gitlive.firebase.auth.EmailAuthProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.milliseconds

data class FirebaseUserSmallData (
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoURL: String?
)

/** A one-shot status message from a sign-in/sign-up flow — an error or just info. */
data class AuthStatusEvent(val message: String?, val isError: Boolean)

/**
 * The lambdas that genuinely differ per call site across createOrSignInUserAndNavigateToHome,
 * handleSignInResult, signUpUIManage, and signInUIManage — bundled so callers only override
 * what they actually need instead of retyping all of them at every call. onErrorMessage isn't
 * here: it's now [AuthViewModel.authEvent], a StateFlow each screen collects once instead of
 * a lambda passed to every call.
 */
data class AuthFlowCallbacks(
    val onClearGuestGame: (Game?) -> Unit = {},
    val onClearForm: () -> Unit = {},
    val onShowSignUp: () -> Unit = {},
    val onShowVerifyEmail: () -> Unit = {},
    val onSuccess: () -> Unit = {}
)

open class AuthViewModel(
    val authRepository: FirebaseAuthRepository,
    val viewManager: AppNavigationManaging,
    val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : KoinComponent {
    
    // Breaking circular dependency with lazy injection
    val userRepository: UserRepository by inject()

    internal val _firebaseUser = MutableStateFlow<FirebaseUser?>(authRepository.currentUser)
    val firebaseUser: StateFlow<FirebaseUser?> = _firebaseUser.asStateFlow()

    private val _currentUserIdentifier = MutableStateFlow(authRepository.currentUser?.uid)
    val currentUserIdentifierFlow: StateFlow<String?> = _currentUserIdentifier.asStateFlow()

    private val _userModel = MutableStateFlow<UserModel?>(null)
    val userModel: StateFlow<UserModel?> = _userModel.asStateFlow()

    private val _isLoadingUser = MutableStateFlow(false)
    val isLoadingUser: StateFlow<Boolean> = _isLoadingUser.asStateFlow()

    private val _authEvent = MutableStateFlow<AuthStatusEvent?>(null)
    val authEvent: StateFlow<AuthStatusEvent?> = _authEvent.asStateFlow()

    fun consumeAuthEvent() { _authEvent.value = null }

    // internal, not private: platform-specific extensions (AuthViewModelIOS.kt,
    // AuthViewModel+Apple.kt/+Google.kt) that fail before ever reaching
    // handleSignInResult still need to be able to post to the same event channel.
    internal fun postAuthEvent(message: String?, isError: Boolean) {
        _authEvent.value = AuthStatusEvent(message, isError)
    }

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

    internal fun syncCurrentUserIdentifier() {
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

        AppLogger.info("AuthViewModel", "Account deleted on another device — cleaning up")
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

    /**
     * Bootstraps the signed-in user for this session: verifies the Firebase session is
     * still valid (cleaning up + navigating to sign-in if the account was deleted on
     * another device), then loads-or-creates the [UserModel] — reusing this session's
     * already-synced model instead of re-fetching if the sign-in pipeline just did it.
     *
     * Shared between admin-android and user-android so both apps' cold-start bootstrap
     * behaves identically (retry-on-transient-failure, dedupe against a fresh sign-in).
     *
     * @return `null` if the session was invalidated (already handled — caller has
     * nothing more to do). Throws on a genuine load failure so the caller can decide
     * how to respond (e.g. keep showing stale data).
     */
    suspend fun bootstrapUserSession(userId: String): UserModel? {
        val sessionValid = verifyAndCleanupIfDeleted()
        if (!sessionValid) return null

        val syncedUser = _userModel.value.takeIf { hasSyncedUserThisSession }
        if (syncedUser != null) return syncedUser

        val loadedUser = try {
            userRepository.loadOrCreateUser(id = userId) { refreshedModel -> setUserModel(refreshedModel) }
        } catch (e: IllegalStateException) {
            AppLogger.warning("AuthViewModel", "User load failed (transient?): ${e.message}. Retrying in 3s...")
            delay(3_000.milliseconds)
            userRepository.loadOrCreateUser(id = userId) { refreshedModel -> setUserModel(refreshedModel) }
        }
        setUserModel(loadedUser)
        markUserSynced()
        return loadedUser
    }

    suspend fun refreshVerificationStatus(): Boolean {
        return authRepository.refreshVerificationStatus()
    }

    suspend fun resendVerificationEmail(): Boolean {
        return authRepository.sendEmailVerification().isSuccess
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
    /**
     * Only ever called internally (by handleSignInResult, signUpUIManage, signInUIManage —
     * no external caller on either platform), so this stays private instead of exposing
     * its own lambda-heavy signature.
     */
    private fun createOrSignInUserAndNavigateToHome(
        user: FirebaseUserSmallData,
        name: String? = null,
        signInMethod: SignInMethod? = null,
        appleId: String? = null,
        navToHome: Boolean = true,
        guestGame: Game? = null,
        callbacks: AuthFlowCallbacks = AuthFlowCallbacks(),
        completion: () -> Unit = {}
    ) {
        postAuthEvent(null, false)
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
                    callbacks.onClearGuestGame(null)
                }

                if (navToHome) {
                    viewManager.navigateAfterSignIn()
                }
            } catch (e: Exception) {
                AppLogger.error("AuthViewModel", "Failed to load or create user", e)
                postAuthEvent(e.message, false)
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
        callbacks: AuthFlowCallbacks = AuthFlowCallbacks(),
        keepSignedIn: Boolean = false
    ) {
        coroutineScope.launch {
            _isLoadingUser.value = true
            val result = createUser(emailInput, passwordInput)

            if (result.isFailure) {
                _isLoadingUser.value = false
                postAuthEvent(result.exceptionOrNull()?.message ?: "Sign up failed", false)
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
                    callbacks = callbacks
                ) {
                    coroutineScope.launch {
                        // Keep the loading state alive through the verification
                        // email step (createOrSignIn's finally just cleared it).
                        _isLoadingUser.value = true
                        val verificationResult = authRepository.sendEmailVerification()

                        if (verificationResult.isFailure) {
                            val error = verificationResult.exceptionOrNull()?.message ?: "Unknown error"
                            postAuthEvent("Couldn’t send verification email: $error", false)
                        } else {
                            callbacks.onClearForm()
                            callbacks.onSuccess()
                            callbacks.onShowVerifyEmail()
                            if (!keepSignedIn) {
                                postAuthEvent("Please Verify Your Email To Continue", true)
                                logout()
                            }
                        }
                        _isLoadingUser.value = false
                    }
                }
            } else {
                _isLoadingUser.value = false
            }
        }
    }

    /**
     * Common finish-line for every provider's sign-in flow: given a Firebase auth result
     * plus whatever profile fields that provider already extracted, resolve/create the
     * UserModel and navigate home. Merged from what used to be two near-identical
     * functions (handleSignInResult for Google, handleAppleSignInResult for Apple) —
     * they differed only in which optional fields get threaded through and the
     * SignInMethod value, so [signInMethod] is now just another parameter.
     *
     * [photoUrl] is an override for [FirebaseUser.safePhotoURL] (e.g. Google's SDK often
     * already hands back a profile picture URL) — passing null just falls back to the
     * platform-safe read, same as the other safePhotoURL() call sites in this class.
     */
    @DefaultArgumentInterop.Enabled
    fun handleSignInResult(
        result: Result<FirebaseUser?>,
        signInMethod: SignInMethod,
        name: String? = null,
        photoUrl: String? = null,
        email: String? = null,
        appleId: String? = null,
        guestGame: Game? = null,
        callbacks: AuthFlowCallbacks = AuthFlowCallbacks()
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
                        photoURL = firebaseUser.safePhotoURL(photoUrl)
                    )
                    createOrSignInUserAndNavigateToHome(
                        user = userData,
                        name = name,
                        signInMethod = signInMethod,
                        appleId = appleId,
                        guestGame = guestGame,
                        callbacks = callbacks
                    )
                } else {
                    _isLoadingUser.value = false
                    postAuthEvent("User data is missing.", true)
                }
            },
            onFailure = { error ->
                _isLoadingUser.value = false
                postAuthEvent(error.message ?: "An unknown error occurred", true)
            }
        )
    }

    @DefaultArgumentInterop.Enabled
    fun signInUIManage(
        emailInput: String,
        passwordInput: String,
        guestGame: Game? = null,
        callbacks: AuthFlowCallbacks = AuthFlowCallbacks(),
        keepSignedIn: Boolean = false
    ) {
        coroutineScope.launch {
            _isLoadingUser.value = true
            val result = signIn(emailInput, passwordInput)

            if (result.isFailure) {
                _isLoadingUser.value = false
                callbacks.onShowSignUp()
                postAuthEvent("No User Found Please Sign Up", false)
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
                        callbacks = callbacks
                    ){}

                } else {

                    val verificationResult = authRepository.sendEmailVerification()

                    if (verificationResult.isFailure) {

                        val error = verificationResult.exceptionOrNull()?.message ?: "Unknown error"

                        postAuthEvent("Couldn’t send verification email: ${error}", false)
                    } else {
                        callbacks.onClearForm()
                        callbacks.onShowVerifyEmail()
                        if (!keepSignedIn) {
                            postAuthEvent("Please Verify Your Email To Continue", true)
                            logout() // Logout AFTER email is sent
                        }
                    }
                    _isLoadingUser.value = false
                }
            } else {
                _isLoadingUser.value = false
            }
        }
    }
}