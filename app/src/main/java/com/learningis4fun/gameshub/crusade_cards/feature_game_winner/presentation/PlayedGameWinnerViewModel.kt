package com.learningis4fun.gameshub.crusade_cards.feature_game_winner.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Game
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameStageRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.UserRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.GameState
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.toUIDuration
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.toWinnerNames
import com.learningis4fun.gameshub.crusade_cards.commons.ui.routes.WinnerScreenRoute
import com.learningis4fun.gameshub.crusade_cards.feature_start.domain.use_cases.GetOngoingGamesUseCase
import com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.OngoingGamesOrNewGameState
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.relations.GameWithPlayers
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.util.getDataOrThrowException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val getOngoingGamesUseCase: GetOngoingGamesUseCase,
    private val gameCardRepository: GameCardRepository,
    private val gameStageRepository: GameStageRepository
) : ViewModel() {

    private val gameId = savedStateHandle.toRoute<WinnerScreenRoute>().wonGameId

    private val playedGameStateFlow =
        MutableStateFlow<PlayedGameWinnerOrNextStage>(PlayedGameWinnerOrNextStage.Loading)

    val playedGameState = playedGameStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                getOngoingGamesUseCase(),
                gameRepository.getGame(gameId = gameId),
                preferences.numberOfCardsPerPlayer
            ) { ongoingGame, game, numberOfCardsPerPlayer ->
                Triple(
                    ongoingGame,
                    game,
                    numberOfCardsPerPlayer
                )
            }.collect { (ongoingGame, game, numberOfCardsPerPlayer) ->

                game?.let {
                    loadWinnerScreenInfo(
                        ongoingGamesState = ongoingGame,
                        game = game,
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
                    preferences.decreaseNumberOfCardsPerPlayer()
                }
            }

            PlayedGameWinnerEvents.IncreaseNumberOfCards -> {
                viewModelScope.launch {
                    preferences.increaseNumberOfCardsPerPlayer()
                }
            }

            is PlayedGameWinnerEvents.DeleteGame -> {
                viewModelScope.launch {
                    try {
                        gameRepository.deleteGame(gameId).getDataOrThrowException()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            is PlayedGameWinnerEvents.PlayerProfileClick -> {

            }
        }
    }

    private suspend fun loadWinnerScreenInfo(
        ongoingGamesState: OngoingGamesOrNewGameState.OngoingGamesState,
        game: Game,
        numberOfCardsPerPlayer: Int
    ) {
        try {
            println("winner: $game")

            if (game.gameState == GameState.Completed.name) {

                val ongoingGamesOrNewGameState: OngoingGamesOrNewGameState =
                    if (ongoingGamesState.games.isNotEmpty()) {
                        ongoingGamesState
                    } else {
                        OngoingGamesOrNewGameState.NewGameState
                    }

                // completed
                val gameWithPlayers = gameRepository
                    .getGameWithPlayers(gameId = gameId)
                    .first()

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
                        ongoingGamesOrNewGameState = ongoingGamesOrNewGameState
                    )
                }
            } else {

                val gameStages = gameStageRepository.getGameStages(gameId).getDataOrThrowException()

                // next stage
                val nextStageNumber = gameStages.first().stageNumber

                // pre stage
                val currentStageNumber = if (gameStages.size > 1) gameStages.take(2)
                    .last().stageNumber else gameStages.first().stageNumber

                // load next stage
                val gameWithPlayers = gameRepository
                    .getGameWithPlayers(gameId = gameId)
                    .first()


                val nextStagePlayers = gameRepository
                    .getGameWithLatestStagePlayers(gameId = gameId)
                    .first()
                    .players

                val eliminatedPlayers =
                    gameWithPlayers
                        .players
                        .filterNot { player: Player -> player in nextStagePlayers }

                val winner = gameWithPlayers.players.firstOrNull { it.uniqueId == game.winnerId }

                val loggedInUser = userRepository.getUserId()

                val winnerNames = loggedInUser.toWinnerNames(winner)

                val unPlayedCardsForNextStagePlayers = nextStagePlayers
                    .map { it.uniqueId }
                    .flatMap { playerId ->
                        gameCardRepository
                            .getUnPlayedCardsForPlayer(
                                gameId = gameId,
                                playerId = playerId,
                                stageNumber = nextStageNumber
                            )
                            .first()
                    }

                val unPlayedCardsForEliminatedPlayers = eliminatedPlayers
                    .map { it.uniqueId }
                    .flatMap { playerId ->
                        gameCardRepository
                            .getUnPlayedCardsForPlayer(
                                gameId = gameId,
                                playerId = playerId,
                                stageNumber = currentStageNumber
                            )
                            .first()
                    }

                val unPlayedCards =
                    unPlayedCardsForEliminatedPlayers.union(unPlayedCardsForNextStagePlayers)
                        .toList()

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

            val unPlayedCards = otherPlayers.flatMap { player ->
                gameCardRepository.getUnPlayedCardsForPlayer(
                    playerId = player.uniqueId,
                    gameId = completedGame.game.id,
                    stageNumber = 0
                ).first()
            }

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