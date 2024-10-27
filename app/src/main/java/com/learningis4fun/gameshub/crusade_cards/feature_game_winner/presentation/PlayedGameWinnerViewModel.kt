package com.learningis4fun.gameshub.crusade_cards.feature_game_winner.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.UserRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.toUIDuration
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.toWinnerNames
import com.learningis4fun.gameshub.crusade_cards.commons.ui.routes.WinnerScreenRoute
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.relations.GameWithPlayers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlayedGameWinnerViewModel @OptIn(ExperimentalCoroutinesApi::class)
@Inject constructor(
    savedStateHandle: SavedStateHandle,
    gameRepository: GameRepository,
    private val userRepository: UserRepository,
    private val preferences: CrusadeCardsPreferencesManager
) : ViewModel() {

    private val gameId = savedStateHandle.toRoute<WinnerScreenRoute>().wonGameId

    private val playedGameWinnerStateFlow = MutableStateFlow(PlayedGameWinnerState())
    val playedGameWinnerState = playedGameWinnerStateFlow.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val completedGame = gameRepository
        .getGameWithPlayers(gameId = gameId)
        .flatMapLatest {
            flow {
                val initialState = PlayedGameWinnerState()
                emit(initialState)
                val state = loadWinnerInfo(it)
                emit(state)
            }
        }

    init {
        viewModelScope.launch {
            combine(
                completedGame,
                preferences.numberOfCardsPerPlayer
            ) { completedGame, numberOfCardsPerPlayer ->
                Pair(
                    completedGame,
                    numberOfCardsPerPlayer
                )
            }.collect { (completedGame, numberOfCardsPerPlayer) ->
                playedGameWinnerStateFlow.update {
                    completedGame.copy(
                        numberOfCardsPerPlayer = numberOfCardsPerPlayer
                    )
                }
            }
        }

    }

    fun onEvent(event: PlayedGameWinnerEvents) {
        when (event) {
            PlayedGameWinnerEvents.DecreaseNumberOfCards -> {
                viewModelScope.launch {
                    val numberOfCardsPerPlayer =
                        playedGameWinnerStateFlow.value.numberOfCardsPerPlayer - 1
                    if (numberOfCardsPerPlayer >= Constants.NumberOfCardsOptions.DEFAULT_CARDS_PER_PLAYER) {
                        preferences.updateNumberOfCardsPerPlayer(numberOfCardsPerPlayer)
                    }
                }
            }

            PlayedGameWinnerEvents.IncreaseNumberOfCards -> {
                viewModelScope.launch {
                    val numberOfCardsPerPlayer =
                        playedGameWinnerStateFlow.value.numberOfCardsPerPlayer + 1
                    if (numberOfCardsPerPlayer <= Constants.NumberOfCardsOptions.HIGHEST_CARDS_PER_PLAYER) {
                        preferences.updateNumberOfCardsPerPlayer(numberOfCardsPerPlayer)
                    }
                }
            }
        }
    }


    private suspend fun loadWinnerInfo(
        gameWithPlayers: GameWithPlayers
    ): PlayedGameWinnerState {
        return try {

            val completedGame = gameWithPlayers
                .toCompletedGame()

            val loggedInUser = userRepository.getPlayerSignInDetails().first()

            val otherPlayers = completedGame.otherPlayers.map { player ->
                player.copy(
                    profileName = loggedInUser.uniqueId.toWinnerNames(player)
                )
            }

            val winner = completedGame.winner
                ?: throw Exception("Unexpected error occurred; please restart the app")

            playedGameWinnerStateFlow.update {
                it.copy(
                    winnerImageUrl = completedGame.winner.imageUrl,
                    gameDuration = completedGame.duration.toUIDuration(),
                    otherPlayers = otherPlayers,
                    loading = false,
                    winnerNames = loggedInUser.uniqueId.toWinnerNames(winner),
                    error = null
                )
            }

            PlayedGameWinnerState(
                winnerImageUrl = completedGame.winner.imageUrl,
                gameDuration = completedGame.duration.toUIDuration(),
                otherPlayers = otherPlayers,
                loading = false,
                winnerNames = loggedInUser.uniqueId.toWinnerNames(winner),
                error = null
            )

        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }
            e.printStackTrace()
            PlayedGameWinnerState(
                error = e.message.toString()
            )
        }
    }
}