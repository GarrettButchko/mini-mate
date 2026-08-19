package com.garrettbutchko.minimate.viewModels

import com.garrettbutchko.minimate.dataModels.playerModels.LeaderboardEntry
import com.garrettbutchko.minimate.repositories.CourseLeaderboardRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

open class LeaderBoardViewModel(
    val lbRepo: CourseLeaderboardRepository = CourseLeaderboardRepository(),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _allTimeLeaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val allTimeLeaderboard: StateFlow<List<LeaderboardEntry>> = _allTimeLeaderboard.asStateFlow()

    private val _debugError = MutableStateFlow<String?>(null)
    val debugError: StateFlow<String?> = _debugError.asStateFlow()

    private var listenJob: Job? = null

    // MARK: - Lifecycle

    fun setAllTimeLeaderboard(leaderboard: List<LeaderboardEntry>) {
        _allTimeLeaderboard.value = leaderboard
    }

    fun onAppear(courseID: String) {
        listenJob?.cancel()
        listenJob = coroutineScope.launch {
            lbRepo.listenTopAllTime(courseID).collect { entries ->
                _allTimeLeaderboard.value = entries
            }
        }
    }

    // ALWAYS do this to prevent memory leaks and unnecessary Firebase costs
    fun onDisappear() {
        listenJob?.cancel()
        listenJob = null
    }

    // MARK: - Actions
    
    fun seedDebugData(courseID: String) {
        _debugError.value = "Seeding…"
        coroutineScope.launch {
            lbRepo.seedDebugEntries(courseID)
                .onSuccess { _debugError.value = "Seeded 25 players" }
                .onFailure { _debugError.value = it.message ?: "Failed to seed debug players" }
        }
    }

    fun clearDebugData(courseID: String) {
        _debugError.value = "Clearing…"
        coroutineScope.launch {
            lbRepo.deleteAllEntries(courseID)
                .onSuccess { _debugError.value = "Cleared all entries" }
                .onFailure { _debugError.value = it.message ?: "Failed to clear entries" }
        }
    }

    fun deletePlayerEntry(courseID: String, playerID: String) {
        coroutineScope.launch {
            lbRepo.deleteEntry(courseID, playerID)
            
            // Optimistic UI update
            val currentList = _allTimeLeaderboard.value.toMutableList()
            currentList.removeAll { it.id == playerID }
            _allTimeLeaderboard.value = currentList
        }
    }
}
