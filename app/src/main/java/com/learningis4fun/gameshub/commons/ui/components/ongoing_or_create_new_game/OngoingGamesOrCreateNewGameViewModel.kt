package com.learningis4fun.gameshub.commons.ui.components.ongoing_or_create_new_game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learningis4fun.gameshub.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.commons.domain.util.GameState
import com.learningis4fun.gameshub.domain.use_cases.GetOngoingGamesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OngoingGamesOrCreateNewGameViewModel @Inject constructor(
    private val getOngoingGamesUseCase: GetOngoingGamesUseCase,
    private val gameRepository: GameRepository,
) : ViewModel() {

    private val ongoingGamesOrCreateNewGameStateFlow =
        MutableStateFlow<OngoingGamesOrCreateNewGameState>(OngoingGamesOrCreateNewGameState.Loading)
    val ongoingGamesOrCreateNewGameState = ongoingGamesOrCreateNewGameStateFlow.asStateFlow()

    private val gameId = MutableStateFlow("")

    init {
        viewModelScope.launch {
            combine(
                getOngoingGamesUseCase(),
                gameId,
            ) { ongoingGames, gameId ->
                Pair(
                    ongoingGames,
                    gameId
                )
            }.collect { (ongoingGames, gameId) ->

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

                ongoingGamesOrCreateNewGameStateFlow.update {
                    state
                }

            }
        }
    }

    fun setGameId(id: String) {
        gameId.update { id }
    }

}