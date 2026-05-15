package com.garrettbutchko.minimate.viewModels

import co.touchlab.kermit.Logger
import com.garrettbutchko.minimate.di.KoinHelper
import com.garrettbutchko.minimate.di.KoinHelperParent

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class JoinViewModel(
    val gameModel: GameViewModel = KoinHelper.getGameViewModel(),
    val authModel: AuthViewModel = KoinHelperParent.getAuthViewModel()
) {
    // MARK: - UI State
    private val _gameCode = MutableStateFlow("")
    val gameCode: StateFlow<String> = _gameCode.asStateFlow()

    private val _inGame = MutableStateFlow(false)
    val inGame: StateFlow<Boolean> = _inGame.asStateFlow()

    private val _showExitAlert = MutableStateFlow(false)
    val showExitAlert: StateFlow<Boolean> = _showExitAlert.asStateFlow()

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    // MARK: - Actions

    fun setGameCode(code: String) {
        _gameCode.value = code
    }

    fun setShowExitAlert(show: Boolean) {
        _showExitAlert.value = show
    }

    fun joinGame() {
        val currentCode = _gameCode.value
        Logger.i("JoinViewModel: joinGame called with code: $currentCode")
        
        if (!canAttemptJoin(currentCode)) {
            Logger.i("JoinViewModel: joinGame aborted - invalid code length")
            return
        }

        val user = authModel.userModel.value
        if (user == null) {
            Logger.i("JoinViewModel: joinGame aborted - no userModel found")
            return
        }
        
        Logger.i("JoinViewModel: Attempting to join game $currentCode for user ${user.googleId}")
        gameModel.joinGame(id = currentCode, userId = user.googleId) { success, error ->
            Logger.i("JoinViewModel: joinGame result - success: $success, error: $error")
            if (success) {
                _inGame.value = true
                _message.value = ""
            } else {
                error?.let {
                    _message.value = it
                }
            }
        }
    }

    fun leaveGame() {
        val user = authModel.userModel.value
        if (user == null) {
            Logger.i("JoinViewModel: leaveGame aborted - no userModel found")
            return
        }

        Logger.i("JoinViewModel: leaveGame called for user ${user.googleId}")
        gameModel.leaveGame(userId = user.googleId)
        _gameCode.value = ""
        _inGame.value = false
    }

    // MARK: - External State Reactions

    /**
     * Called when the Join/Lobby view is dismissed.
     */
    fun joinDidDismiss(showJoin: Boolean) {
        val game = gameModel.game.value
        val shouldLeave = shouldLeaveGameOnHostDismiss(showJoin, game.id, game.started)
        Logger.i("JoinViewModel: hostDidDismiss called - showHost: $showJoin, gameId: ${game.id}, started: ${game.started}, shouldLeave: $shouldLeave")
        
        if (shouldLeave) {
            Logger.i("JoinViewModel: shouldLeave is true, calling leaveGame()")
            leaveGame()
        }
    }

    /**
     * Called when the game's "started" status changes.
     */
    fun gameDidStart(started: Boolean, onNavigate: () -> Unit) {
        val shouldNav = shouldNavigateOnGameStart(started)
        Logger.i("JoinViewModel: gameDidStart reaction - started: $started, shouldNavigate: $shouldNav")
        
        if (shouldNav) {
            Logger.i("JoinViewModel: Triggering navigation callback")
            onNavigate()
        }
    }

    /**
     * Called when the game's "dismissed" status changes.
     */
    fun gameDidDismiss(dismissed: Boolean) {
        val shouldReset = dismissed
        Logger.i("JoinViewModel: gameDidDismiss reaction - dismissed: $dismissed, shouldReset: $shouldReset")
        
        if (shouldReset) {
            Logger.i("JoinViewModel: Resetting game state")
            _message.value = "The host ended the game."
            _gameCode.value = ""
            _inGame.value = false
        }
    }

    // MARK: - Internal Business Logic

    private fun canAttemptJoin(gameCode: String): Boolean {
        return gameCode.isNotBlank() && gameCode.length >= 6
    }

    private fun shouldLeaveGameOnHostDismiss(showHost: Boolean, gameId: String, hasStarted: Boolean): Boolean {
        return !showHost && gameId.isNotEmpty() && !hasStarted
    }

    private fun shouldNavigateOnGameStart(hasStarted: Boolean): Boolean {
        return hasStarted
    }

    private fun shouldResetOnGameDismiss(isDismissed: Boolean): Boolean {
        return isDismissed
    }
}
