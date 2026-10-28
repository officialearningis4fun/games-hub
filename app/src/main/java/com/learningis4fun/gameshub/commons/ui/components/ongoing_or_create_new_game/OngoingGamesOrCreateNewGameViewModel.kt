package com.learningis4fun.gameshub.commons.ui.components.ongoing_or_create_new_game

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learningis4fun.gameshub.api.use_cases.GetOngoingGamesUseCase
import com.learningis4fun.gameshub.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.commons.domain.util.GameState
import com.learningis4fun.gameshub.util.LogTags
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class OngoingGamesOrCreateNewGameViewModel @Inject constructor(
    private val getOngoingGamesUseCase: GetOngoingGamesUseCase,
    private val gameRepository: GameRepository,
) : ViewModel() {

    private val ongoingGamesOrCreateNewGameStateFlow =
        MutableStateFlow<OngoingGamesOrCreateNewGameState>(OngoingGamesOrCreateNewGameState.Loading)
    val ongoingGamesOrCreateNewGameState = ongoingGamesOrCreateNewGameStateFlow.asStateFlow()

    val handler = CoroutineExceptionHandler { _, throwable ->
        Log.d(LogTags.OngoingGamesOrCreateNewGameViewModel, "throwable: $throwable")
    }
    private val gameId = MutableStateFlow("")

    init {
        viewModelScope.launch(handler) {
            combine(
                getOngoingGamesUseCase(),
                gameId,
            ) { ongoingGames, gameId ->
                Pair(
                    ongoingGames,
                    gameId
                )
            }.collect { (ongoingGames, gameId) ->

                try {
                    Log.d(LogTags.OngoingGamesOrCreateNewGameViewModel, "gameId: $gameId")
                    val game = gameRepository.getGame(gameId).first()
                    // loading - default
                    // ongoing games
                    // create new game
                    // nothing

                    // when the game is not completed - we have another stage - so show nothing
                    val state = if (game == null || game.gameState == GameState.Completed.name) {
                        // if game is completed; check the ongoing games -. if empty -> show create new game
                        if (ongoingGames.isNotEmpty()) {
                            // show ongoing
                            OngoingGamesOrCreateNewGameState.OngoingGamesState(
                                games = ongoingGames
                            )
                        } else {
                            // show create new game
                            OngoingGamesOrCreateNewGameState.CreateNewGameToolsState
                        }
                    } else {
                        // not completed
                        OngoingGamesOrCreateNewGameState.Nothing
                    }
                    Log.d(LogTags.OngoingGamesOrCreateNewGameViewModel, "state: $state")

                    ongoingGamesOrCreateNewGameStateFlow.update {
                        state
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    e.printStackTrace()
                    ongoingGamesOrCreateNewGameStateFlow.update {
                        OngoingGamesOrCreateNewGameState.CreateNewGameToolsState
                    }
                }

            }
        }
    }

    fun setGameId(id: String) {
        gameId.update { id }
    }

}