package com.learningis4fun.gameshub.presentation.game_winner.offline

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.learningis4fun.gameshub.api.use_cases.GetLatestUnPlayedCards
import com.learningis4fun.gameshub.commons.domain.model.Game
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.commons.domain.repository.GameStageRepository
import com.learningis4fun.gameshub.commons.domain.repository.UserRepository
import com.learningis4fun.gameshub.commons.domain.util.GameState
import com.learningis4fun.gameshub.commons.domain.util.toUIDuration
import com.learningis4fun.gameshub.commons.domain.util.toWinnerNames
import com.learningis4fun.gameshub.commons.ui.routes.WinnerScreenRoute
import com.learningis4fun.gameshub.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.data.data_source.relations.GameWithPlayers
import com.learningis4fun.gameshub.presentation.game_winner.PlayedGameWinnerOrNextStage
import com.learningis4fun.gameshub.util.getDataOrThrowException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlayedGameWinnerViewModel @OptIn(ExperimentalCoroutinesApi::class)
@Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gameRepository: GameRepository,
    private val userRepository: UserRepository,
    private val preferences: CrusadeCardsPreferencesManager,
    private val gameStageRepository: GameStageRepository,
    private val getLatestUnPlayedCards: GetLatestUnPlayedCards
) : ViewModel() {

    private val gameId = savedStateHandle.toRoute<WinnerScreenRoute>().wonGameId

    private val playedGameStateFlow =
        MutableStateFlow<PlayedGameWinnerOrNextStage>(PlayedGameWinnerOrNextStage.Loading)

    val playedGameState = playedGameStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                gameRepository.getGame(gameId = gameId),
                preferences.numberOfCardsPerPlayer
            ) { game, numberOfCardsPerPlayer ->
                Pair(
                    game,
                    numberOfCardsPerPlayer
                )
            }.collect { (game, numberOfCardsPerPlayer) ->

                game?.let {
                    loadWinnerScreenInfo(
                        game = game,
                        numberOfCardsPerPlayer = numberOfCardsPerPlayer
                    )
                }
            }
        }

        viewModelScope.launch {
            playedGameStateFlow.collectLatest {
                println("playedGameStateFlow: $it")
            }
        }

    }

    private suspend fun loadWinnerScreenInfo(
        game: Game,
        numberOfCardsPerPlayer: Int
    ) {
        try {
            println("winner: $game")

            if (game.gameState == GameState.Completed.name) {
                // completed
                val gameWithPlayers = gameRepository
                    .getGameWithPlayers(gameId = gameId)
                    .first() ?: return

                val completedState = loadWinnerInfo(
                    gameWithPlayers = gameWithPlayers
                )
                playedGameStateFlow.update {
                    PlayedGameWinnerOrNextStage.PlayedGameWinnerState(
                        loading = completedState.loading,
                        gameDuration = completedState.gameDuration,
                        winnerImageUrl = completedState.winnerImageUrl,
                        otherPlayers = completedState.otherPlayers,
                        unPlayedCards = completedState.unPlayedCards,
                        numberOfCardsPerPlayer = numberOfCardsPerPlayer,
                        winnerNames = completedState.winnerNames,
                        error = completedState.error,
                    )
                }
            } else {

                val gameStages = gameStageRepository.getGameStages(gameId).getDataOrThrowException()

                // pre stage
                val currentStageNumber = if (gameStages.size > 1) gameStages.take(2)
                    .last().stageNumber else gameStages.first().stageNumber

                val nextStageNumber = gameStages.first().stageNumber

                // load next stage
                val gameWithPlayers = gameRepository
                    .getGameWithPlayers(gameId = gameId)
                    .first() ?: return

                val nextStagePlayers = gameRepository
                    .getGameWithLatestStagePlayers(gameId = gameId)
                    .first()
                    .players

                val eliminatedPlayers =
                    gameWithPlayers
                        .players
                        .filterNot { player: Player -> player in nextStagePlayers }

                val winner = gameWithPlayers
                    .players.firstOrNull { it.uniqueId == game.winnerId }

                val loggedInUser = userRepository.getUserId()

                val winnerNames = loggedInUser.toWinnerNames(winner)

                val playerIds = (nextStagePlayers.union(eliminatedPlayers))
                    .map { it.uniqueId }
                    .filterNot { it == winner?.uniqueId }

                val unPlayedCards = getLatestUnPlayedCards(
                    gameId = gameId,
                    playerIds = playerIds,
                    stagesToIgnore = listOf(nextStageNumber)
                )

                playedGameStateFlow.update {
                    PlayedGameWinnerOrNextStage.PlayedGameNextStageState(
                        loading = false,
                        gameDuration = game.gameDuration.toUIDuration(),
                        winnerImageUrl = winner?.imageUrl ?: "",
                        eliminatedPlayers = eliminatedPlayers,
                        nextStagePlayers = nextStagePlayers,
                        unPlayedCards = unPlayedCards,
                        winnerNames = winnerNames,
                        error = null
                    )
                }

            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private suspend fun loadWinnerInfo(
        gameWithPlayers: GameWithPlayers
    ): PlayedGameWinnerOrNextStage.PlayedGameWinnerState {
        return try {

            val completedGame = gameWithPlayers
                .toCompletedGame()

            val loggedInUser = userRepository.getPlayerSignInDetails().first()

            val otherPlayers = completedGame.otherPlayers.map { player ->
                player.copy(
                    profileName = loggedInUser.uniqueId.toWinnerNames(player)
                )
            }

            val unPlayedCards = getLatestUnPlayedCards(
                gameId = gameId,
                playerIds = otherPlayers.map { it.uniqueId }
            )

            val winner = completedGame.winner
                ?: throw Exception("Unexpected error occurred; please restart the app")

            PlayedGameWinnerOrNextStage.PlayedGameWinnerState(
                winnerImageUrl = completedGame.winner.imageUrl,
                gameDuration = completedGame.duration.toUIDuration(),
                otherPlayers = otherPlayers,
                unPlayedCards = unPlayedCards,
                loading = false,
                winnerNames = loggedInUser.uniqueId.toWinnerNames(winner),
                error = null
            )

        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }
            e.printStackTrace()
            PlayedGameWinnerOrNextStage.PlayedGameWinnerState(
                error = e.message.toString()
            )
        }
    }
}