package com.garrettbutchko.minimate.repositories

import co.touchlab.kermit.Logger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.AuthCredential

class FirebaseAuthRepository {
    private val auth = Firebase.auth

    private val log = Logger.withTag("FirebaseAuthRepo")

    suspend fun createUser(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password)
            Result.success(result.user)
        } catch (e: Exception) {
            log.e(e) { "❌ Create user error: ${e.message}" }
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password)
            Result.success(result.user)
        } catch (e: Exception) {
            log.e(e) { "❌ Sign-in error: ${e.message}" }
            Result.failure(e)
        }
    }

    suspend fun signInWithCredential(credential: AuthCredential): Result<FirebaseUser?> {
        return try {
            val result = auth.signInWithCredential(credential)
            Result.success(result.user)
        } catch (e: Exception) {
            log.e(e) { "❌ Sign-in with credential error: ${e.message}" }
            Result.failure(e)
        }
    }

    suspend fun logout() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            log.e(e) { "❌ Sign-out error: ${e.message}" }
        }
    }

    suspend fun deleteAccount(credential: AuthCredential): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("No signed-in user")
            user.reauthenticate(credential)
            user.delete()
            Result.success(Unit)
        } catch (e: Exception) {
            log.e(e) { "❌ Delete account error: ${e.message}" }
            Result.failure(e)
        }
    }

    suspend fun reauthenticate(credential: AuthCredential): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("No signed-in user")
            user.reauthenticate(credential)
            Result.success(Unit)
        } catch (e: Exception) {
            log.e(e) { "❌ Reauthenticate error: ${e.message}" }
            Result.failure(e)
        }
    }

    suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            auth.currentUser?.sendEmailVerification()
            Result.success(Unit)
        } catch (e: Exception) {
            log.e(e) { "❌ Send email verification error: ${e.message}" }
            Result.failure(e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            log.e(e) { "❌ Send password reset error: ${e.message}" }
            Result.failure(e)
        }
    }

    /**
     * Returns Success(true) if the Firebase auth session is still valid,
     * Success(false) if the account was deleted on another device,
     * or Failure if the check couldn't complete (e.g. offline) — caller should assume valid.
     */
    suspend fun verifyAuthStillValid(): Result<Boolean> {
        val user = auth.currentUser ?: return Result.success(false)
        return try {
            user.reload()
            Result.success(true)
        } catch (e: Exception) {
            val msg = e.message?.lowercase() ?: ""
            val isDeleted = msg.contains("user-not-found") ||
                msg.contains("user_not_found") ||
                msg.contains("user has been deleted") ||
                msg.contains("no user record") ||
                msg.contains("user not found")
            if (isDeleted) Result.success(false) else Result.failure(e)
        }
    }

    suspend fun refreshVerificationStatus(): Boolean {
        return try {
            val user = auth.currentUser ?: return false
            user.reload()
            user.isEmailVerified
        } catch (e: Exception) {
            log.e(e) { "Reload error: ${e.message}" }
            false
        }
    }

    val currentUser: FirebaseUser?
        get() = auth.currentUser
}
