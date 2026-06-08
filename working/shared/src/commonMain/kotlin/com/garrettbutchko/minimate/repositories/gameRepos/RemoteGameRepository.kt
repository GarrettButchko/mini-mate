package com.garrettbutchko.minimate.repositories.gameRepos

import co.touchlab.kermit.Logger
import com.garrettbutchko.minimate.dataModels.gameModels.Game
import com.garrettbutchko.minimate.dataModels.gameModels.GameDTO
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.CancellationException

class RemoteGameRepository {

    private val db = Firebase.firestore
    private val collectionName = "games"

    private val log = Logger.withTag("RemoteGameRepo")

    // Save or update a game in Firestore
    suspend fun save(game: Game): Result<Boolean> {
        return try {
            db.collection(collectionName).document(game.id).set(game.toDTO(), merge = true)
            Result.success(true)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            log.e(e) { "❌ Firestore save error: ${e.message}" }
            Result.failure(e)
        }
    }

    suspend fun save(games: List<Game>): Result<Boolean> = coroutineScope {
        if (games.isEmpty()) return@coroutineScope Result.success(true)

        try {
            games.map { game ->
                async {
                    db.collection(collectionName).document(game.id).set(game.toDTO(), merge = true)
                }
            }.awaitAll()
            Result.success(true)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            log.e(e) { "❌ Firestore save multiple error: ${e.message}" }
            Result.failure(e)
        }
    }

    // Fetch a single game by ID
    suspend fun fetch(id: String): GameDTO? {
        return try {
            val snapshot = db.collection(collectionName).document(id).get()
            if (snapshot.exists) {
                snapshot.data<GameDTO>()
            } else {
                null
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            log.e(e) { "❌ Firestore fetch error: ${e.message}" }
            null
        }
    }

    suspend fun fetchAll(ids: List<String>): List<GameDTO> = coroutineScope {
        if (ids.isEmpty()) return@coroutineScope emptyList()

        // Fetch each document by ID in parallel. The FieldPath.documentId inArray query
        // silently returns 0 results on iOS (gitlive-firebase bug), so individual fetches
        // are used for cross-platform reliability.
        try {
            ids.map { id ->
                async {
                    val snapshot = db.collection(collectionName).document(id).get()
                    if (snapshot.exists) {
                        try {
                            snapshot.data<GameDTO>()
                        } catch (e: Exception) {
                            if (e is CancellationException) throw e
                            log.e(e) { "❌ Failed to deserialize doc $id: ${e.message}" }
                            null
                        }
                    } else {
                        log.w { "⚠️ Document $id not found in Firestore" }
                        null
                    }
                }
            }.awaitAll().filterNotNull()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            log.e(e) { "❌ Firestore fetchAll error: ${e.message}" }
            emptyList()
        }
    }

    // Delete a game by ID
    suspend fun delete(id: String): Result<Boolean> {
        return try {
            db.collection(collectionName).document(id).delete()
            Result.success(true)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            log.e(e) { "❌ Firestore delete error: ${e.message}" }
            Result.failure(e)
        }
    }

    suspend fun deleteAll(ids: List<String>): Result<Boolean> {
        if (ids.isEmpty()) return Result.success(true)

        return try {
            val batch = db.batch()
            ids.forEach { id ->
                val ref = db.collection(collectionName).document(id)
                batch.delete(ref)
            }
            batch.commit()
            Result.success(true)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            log.e(e) { "❌ Firestore batch delete error: ${e.message}" }
            Result.failure(e)
        }
    }
}
