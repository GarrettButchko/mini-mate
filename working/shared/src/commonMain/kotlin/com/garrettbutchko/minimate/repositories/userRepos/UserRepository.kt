package com.garrettbutchko.minimate.repositories.userRepos

import co.touchlab.skie.configuration.annotations.DefaultArgumentInterop
import com.garrettbutchko.minimate.dataModels.gameModels.Game
import com.garrettbutchko.minimate.dataModels.userModels.UserModel
import com.garrettbutchko.minimate.utilities.AppLogger
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.coroutines.*
import kotlin.math.abs
import com.garrettbutchko.minimate.enums.SignInMethod
import com.garrettbutchko.minimate.viewModels.FirebaseUserSmallData
import dev.gitlive.firebase.storage.Data

class UserRepository(
    val localRepo: LocalUserRepository,
    val remoteRepo: RemoteUserRepository
) {
    private val repositoryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /**
     * Replaces loadOrCreateUserAsync.
     * Uses a "Fast-First" approach: returns local immediately, then reconciles in background.
     */
    @DefaultArgumentInterop.Enabled
    suspend fun loadOrCreateUser(
        id: String,
        firebaseUser: FirebaseUserSmallData? = null,
        name: String? = null,
        signInMethod: SignInMethod? = null,
        appleId: String? = null,
        guestGame: Game? = null,
        onUpdate: (UserModel) -> Unit
    ): UserModel {
        // 1️⃣ Local Phase: Check database first
        val local = localRepo.fetch(id)
        if (local != null) {
            AppLogger.debug("UserRepository", "Found local user immediately")
            onUpdate(local)

            // 2️⃣ Background Reconcile: Don't block the UI
            repositoryScope.launch {
                val remote = remoteRepo.fetch(id)
                reconcile(local, remote, id, firebaseUser, name, signInMethod, appleId, guestGame, onUpdate)
            }
            return local
        }

        // 3️⃣ No Local: Fetch remote and reconcile synchronously
        val remote = remoteRepo.fetch(id)
        return reconcile(null, remote, id, firebaseUser, name, signInMethod, appleId, guestGame, onUpdate)
    }

    private suspend fun reconcile(
        local: UserModel?,
        remote: UserModel?,
        id: String,
        firebaseUser: FirebaseUserSmallData?,
        name: String?,
        signInMethod: SignInMethod? = null,
        appleId: String?,
        guestGame: Game?,
        onUpdate: (UserModel) -> Unit
    ): UserModel {
        val result = when {
            local != null && remote != null -> {
                val delta = abs(local.lastUpdated.seconds - remote.lastUpdated.seconds)

                if (delta < 1) { // Already in sync
                    AppLogger.debug("UserRepository", "Already in sync")
                    val updatedLocal = addAccountTypeIfNeeded(local, signInMethod)
                    if (updatedLocal != local) remoteRepo.save(updatedLocal, false)
                    updatedLocal
                } else if (local.lastUpdated.seconds > remote.lastUpdated.seconds) {
                    AppLogger.debug("UserRepository", "Local -> Remote")
                    val updatedLocal = addAccountTypeIfNeeded(local, signInMethod)
                    remoteRepo.save(updatedLocal, false)
                    updatedLocal
                } else {
                    AppLogger.debug("UserRepository", "Remote -> Local")
                    val updatedRemote = addAccountTypeIfNeeded(remote, signInMethod)
                    localRepo.save(updatedRemote, false)
                    updatedRemote
                }
            }
            local != null && remote == null -> {
                AppLogger.debug("UserRepository", "Local -> Remote (no remote)")
                val updatedLocal = addAccountTypeIfNeeded(local, signInMethod)
                remoteRepo.save(updatedLocal, false)
                updatedLocal
            }
            local == null && remote != null -> {
                AppLogger.debug("UserRepository", "Remote -> Local (no local)")
                val updatedRemote = addAccountTypeIfNeeded(remote, signInMethod)
                localRepo.save(updatedRemote, false)
                updatedRemote
            }
            else -> {
                if (firebaseUser == null) {
                    // Returning-user path: remote fetch likely failed transiently.
                    // DO NOT create a blank user — that would overwrite the real Firestore doc.
                    throw IllegalStateException("User $id not found locally or remotely. Remote fetch may have failed.")
                }
                AppLogger.debug("UserRepository", "Creating new user")
                createUser(id, firebaseUser, name, signInMethod, appleId, guestGame)
            }
        }

        // Sync photo URL from Firebase credential if the stored model is missing one
        val incomingPhoto = firebaseUser?.photoURL
        val finalResult = if (incomingPhoto != null && result.photoURL.isNullOrBlank()) {
            AppLogger.debug("UserRepository", "Syncing missing photo URL from Firebase credential")
            val updated = result.copy(photoURL = incomingPhoto, lastUpdated = Timestamp.now())
            localRepo.save(updated, false)
            remoteRepo.save(updated, false)
            updated
        } else {
            result
        }

        val freshLocal = localRepo.fetch(id)
        val toEmit = if (freshLocal != null && freshLocal.lastUpdated.seconds > finalResult.lastUpdated.seconds) {
            freshLocal
        } else {
            finalResult
        }
        onUpdate(toEmit)
        return toEmit
    }

    private suspend fun createUser(
        id: String,
        firebaseUser: FirebaseUserSmallData?,
        name: String?,
        signInMethod: SignInMethod?,
        appleId: String?,
        guestGame: Game?
    ): UserModel {
        val finalName = name ?: firebaseUser?.displayName ?: "User#${id.take(5)}"
        val finalEmail = firebaseUser?.email ?: "Email"

        val gameIDs = if (guestGame != null) listOf(guestGame.id) else emptyList()
        val accountTypes = if (signInMethod != null) listOf(signInMethod.value) else emptyList()

        val newUserModel = UserModel(
            googleId = id,
            appleId = appleId,
            name = finalName,
            photoURL = firebaseUser?.photoURL,
            email = finalEmail,
            gameIDs = gameIDs,
            accountType = accountTypes,
            lastUpdated = Timestamp.now()
        )

        // Save to both sources.
        // Mirror of onUserCreated in functions/src/index.ts — keep both in sync if fields change.
        localRepo.save(newUserModel, false)
        remoteRepo.save(newUserModel, false)
        return newUserModel
    }

    private fun addAccountTypeIfNeeded(userModel: UserModel, type: SignInMethod?): UserModel {
        if (type == null || userModel.accountType.contains(type.value)) return userModel
        return userModel.copy(accountType = userModel.accountType + type.value)
    }

    suspend fun fetch(id: String): UserModel? = remoteRepo.fetch(id)

    suspend fun fetchByEmail(email: String): UserModel? = remoteRepo.fetchByEmail(email)

    suspend fun saveUnified(id: String, userModel: UserModel): Pair<Boolean, Boolean> {
        val localSuccess = localRepo.save(userModel)
        val remoteSuccess = remoteRepo.save(userModel)
        return Pair(localSuccess.isSuccess, remoteSuccess.isSuccess)
    }

    suspend fun deleteUnified(id: String) {
        localRepo.delete(id)
        remoteRepo.delete(id)
    }

    suspend fun uploadProfilePhoto(
        id: String,
        fileData: Data
    ): Result<String> {
        return try {
            // 1. Upload the raw 'Data' object to Firebase Storage via RemoteRepo
            // This returns the download URL String
            val newPhotoUrl = remoteRepo.uploadProfilePhoto(id, fileData).getOrNull()

            if (newPhotoUrl != null) {
                // 2. Fetch the current local user to prepare the update
                val currentLocalUser = localRepo.fetch(id)

                if (currentLocalUser != null) {
                    // 3. Create a brand new copy of the user with the new URL and current time
                    val updatedUser = currentLocalUser.copy(
                        photoURL = newPhotoUrl,
                        lastUpdated = Timestamp.now()
                    )

                    // 4. Save this updated user to BOTH Local (Room) and Remote (Firestore)
                    // This replaces your DispatchQueue.main.async { self.saveUnified(...) } logic
                    val (localOK, remoteOK) = saveUnified(id, updatedUser)

                    if (localOK && remoteOK) {
                        AppLogger.success("UserRepository", "Profile photo URL synced across all data sources")
                    } else {
                        AppLogger.warning("UserRepository", "Photo uploaded, but database sync partially failed. Local: $localOK, Remote: $remoteOK")
                    }
                } else {
                    AppLogger.warning("UserRepository", "No local user found for id: $id. Skipping database update.")
                }
                // 5. Return the successful URL (Equivalent to completion(.success(url)))
                Result.success(newPhotoUrl)
            } else {
                AppLogger.error("UserRepository", "Failed to get new photo URL after upload")
                return Result.failure(Exception("Failed to upload photo"))
            }

        } catch (e: Exception) {
            AppLogger.error("UserRepository", "Failed to upload profile photo for user: $id", e)
            Result.failure(e)
        }
    }
}