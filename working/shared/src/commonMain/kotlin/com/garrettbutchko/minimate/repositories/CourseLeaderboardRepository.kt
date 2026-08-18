package com.garrettbutchko.minimate.repositories

import com.garrettbutchko.minimate.dataModels.playerModels.LeaderboardEntry
import com.garrettbutchko.minimate.dataModels.playerModels.Player
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.Direction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.garrettbutchko.minimate.utilities.AppLogger

class CourseLeaderboardRepository {

    private val db = Firebase.firestore

    // MARK: - Internal References

    private fun allTimeEntriesRef(courseID: String): CollectionReference {
        return db.collection("courses").document(courseID)
            .collection("allTimeLeaderboard")
    }

    // MARK: - Fetch Data

    suspend fun fetchTopAllTime(courseID: String, limit: Int = 25): List<LeaderboardEntry> {
        return try {
            val snapshot = allTimeEntriesRef(courseID)
                .orderBy("totalStrokes", Direction.ASCENDING)
                .limit(limit.toLong())
                .get()
            snapshot.documents.map { it.data() }
        } catch (e: Exception) {
            AppLogger.error("CourseLeaderboardRepository", "Failed to fetch top all time leaderboard for course: $courseID", e)
            emptyList()
        }
    }

    // MARK: - Live Listening

    fun listenTopAllTime(courseID: String, limit: Int = 25): Flow<List<LeaderboardEntry>> {
        return allTimeEntriesRef(courseID)
            .orderBy("totalStrokes", Direction.ASCENDING)
            .limit(limit.toLong())
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { it.data<LeaderboardEntry>() }
            }
    }

    // MARK: - Submit Score

    suspend fun submitScore(courseID: String, player: Player): Result<Boolean> {
        val entry = player.toDTO().convertToLBREP() ?: return Result.success(false)
        val docRef = allTimeEntriesRef(courseID).document(entry.id)

        return try {
            db.runTransaction {
                val snapshot = get(docRef)
                
                val oldBest = if (snapshot.exists) {
                    // Explicitly ask for Int? to avoid "always non-null" warnings and handle missing fields
                    snapshot.get<Int?>("totalStrokes") ?: Int.MAX_VALUE
                } else {
                    Int.MAX_VALUE
                }

                val finalStrokes = if (entry.totalStrokes < oldBest) entry.totalStrokes else oldBest
                val finalEntry = entry.copy(totalStrokes = finalStrokes)

                set(docRef, finalEntry, merge = true)
            }
            Result.success(true)
        } catch (e: Exception) {
            AppLogger.error("CourseLeaderboardRepository", "Failed to submit score for course: $courseID, player: ${player.id}", e)
            Result.failure(e)
        }
    }

    // MARK: - Delete Entry

    suspend fun deleteEntry(courseID: String, playerID: String): Result<Boolean> {
        return try {
            allTimeEntriesRef(courseID).document(playerID).delete()
            Result.success(true)
        } catch (e: Exception) {
            AppLogger.error("CourseLeaderboardRepository", "Failed to delete entry for course: $courseID, player: $playerID", e)
            Result.failure(e)
        }
    }

    // MARK: - Debug

    suspend fun seedDebugEntries(courseID: String): Result<Boolean> {
        val names = listOf(
            "Alice Johnson", "Bob Smith", "Charlie Brown", "Diana Prince", "Ethan Hunt",
            "Fiona Green", "George King", "Hannah White", "Ivan Black", "Julia Roberts",
            "Kevin Hart", "Laura Palmer", "Mike Tyson", "Nina Simone", "Oscar Wilde",
            "Paula Abdul", "Quinn Hughes", "Rachel Green", "Steve Jobs", "Tina Turner",
            "Uma Thurman", "Victor Stone", "Wendy Williams", "Xander Harris", "Yara Greyjoy"
        )
        val ballColors = listOf("red", "blue", "green", "yellow", "purple", "orange", "pink", "mint")
        val strokePool = listOf(20, 24, 27, 29, 31, 33, 35, 36, 38, 40, 41, 43, 45, 47, 49, 51, 53, 55, 57, 59, 61, 63, 65, 67, 70)

        return try {
            val batch = db.batch()
            val ref = allTimeEntriesRef(courseID)
            names.forEachIndexed { index, name ->
                val entry = LeaderboardEntry(
                    id = "debug_player_$index",
                    userId = "debug_user_$index",
                    name = name,
                    photoURL = null,
                    ballColorDT = ballColors[index % ballColors.size],
                    totalStrokes = strokePool[index % strokePool.size],
                    email = "debug$index@test.com"
                )
                batch.set(ref.document(entry.id), entry, merge = true)
            }
            batch.commit()
            Result.success(true)
        } catch (e: Exception) {
            AppLogger.error("CourseLeaderboardRepository", "Failed to seed debug entries for course: $courseID", e)
            Result.failure(e)
        }
    }

    // MARK: - Bulk Delete

    suspend fun deleteAllEntries(courseID: String): Result<Boolean> {
        return try {
            val ref = allTimeEntriesRef(courseID)
            val snapshot = ref.get()
            
            if (snapshot.documents.isEmpty()) {
                return Result.success(true)
            }

            val batch = db.batch()
            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit()

            Result.success(true)
        } catch (e: Exception) {
            AppLogger.error("CourseLeaderboardRepository", "Failed to delete all entries for course: $courseID", e)
            Result.failure(e)
        }
    }
}