package com.learningis4fun.gameshub.commons.ui.components.ongoing_or_create_new_game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learningis4fun.gameshub.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.commons.domain.util.GameState
import com.learningis4fun.gameshub.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.domain.use_cases.GetOngoingGamesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class OngoingGamesOrCreateNewGameViewModel @Inject constructor(
    private val getOngoingGamesUseCase: GetOngoingGamesUseCase,
    private val gameRepository: GameRepository,
    private val preferences: CrusadeCardsPreferencesManager
) : ViewModel() {

    private val ongoingGamesOrCreateNewGameStateFlow =
        MutableStateFlow<OngoingGamesOrCreateNewGameState>(OngoingGamesOrCreateNewGameState.Loading)
    val ongoingGamesOrCreateNewGameState = ongoingGamesOrCreateNewGameStateFlow.asStateFlow()
    private val gameId = MutableStateFlow("")

    init {
        viewModelScope.launch {
            combine(
                getOngoingGamesUseCase(),
                gameRepository.getGame(gameId = gameId.value),
            ) { ongoingGame, game ->
                Pair(
                    ongoingGame,
                    game
                )
            }.collect { (ongoingGame, game) ->

                // loading - default
                // ongoing games
                // create new game
                // nothing

                // when the game is not completed - we have another stage - so show nothing
               val state = if (game == null || game.gameState == GameState.Completed.name) {
                    // if game is completed; check the ongoing games -. if empty -> show create new game
                    if (ongoingGame.games.isNotEmpty()) {
                        // show ongoing
                        OngoingGamesOrCreateNewGameState.OngoingGamesState(
                            games = ongoingGame.games
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