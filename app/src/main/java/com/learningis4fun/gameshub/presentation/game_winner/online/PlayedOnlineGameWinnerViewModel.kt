package com.learningis4fun.gameshub.presentation.game_winner.online

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.learningis4fun.gameshub.api.CrusadeGameService
import com.learningis4fun.gameshub.commons.domain.model.ActiveGame
import com.learningis4fun.gameshub.commons.domain.model.Game
import com.learningis4fun.gameshub.commons.domain.model.GameStage
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.domain.model.User
import com.learningis4fun.gameshub.commons.domain.util.GameState
import com.learningis4fun.gameshub.commons.domain.util.toUIDuration
import com.learningis4fun.gameshub.commons.domain.util.toWinnerNames
import com.learningis4fun.gameshub.commons.ui.routes.WinnerScreenRoute
import com.learningis4fun.gameshub.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.data.data_source.relations.GameWithPlayers
import com.learningis4fun.gameshub.data.data_source.remote.GoogleAuthUiClient
import com.learningis4fun.gameshub.presentation.game_winner.PlayedGameWinnerOrNextStage
import com.learningis4fun.gameshub.util.GamesHubLogTags
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlayedOnlineGameWinnerViewModel @OptIn(ExperimentalCoroutinesApi::class)
@Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val preferences: CrusadeCardsPreferencesManager,
    private val crusadeGameService: CrusadeGameService,
    private val googleAuthUiClient: GoogleAuthUiClient
) : ViewModel() {

    private val gameId = savedStateHandle.toRoute<WinnerScreenRoute>().wonGameId
    private val playedGameStateFlow =
        MutableStateFlow<PlayedGameWinnerOrNextStage>(PlayedGameWinnerOrNextStage.Loading)
    private val activeGameFlow = MutableStateFlow<ActiveGame?>(null)

    val playedGameState = playedGameStateFlow.asStateFlow()

    init {

        viewModelScope.launch {
            crusadeGameService.recentGame(gameId).collectLatest { game ->
                activeGameFlow.update { game }
            }
        }

        viewModelScope.launch {
            combine(
                activeGameFlow,
                preferences.numberOfCardsPerPlayer
            ) { game, numberOfCardsPerPlayer ->
                Pair(
                    game,
                    numberOfCardsPerPlayer
                )
            }.collect { (game, numberOfCardsPerPlayer) ->

                game?.let {
                    loadWinnerScreenInfo(
                        game = game.toGame(gameId = gameId),
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

    fun getGameStages(): List<GameStage> {
        val gameStages = activeGameFlow.value?.gameStages ?: throw Exception("Stage not found")
        return gameStages
    }

    private fun getGameWithLatestStagePlayers(): Flow<GameWithPlayers?> {
        return activeGameFlow.map { recentGame ->
            if (recentGame == null) return@map null
            val stagePlayerIds = getLatestGameStage().playerIds.map { it.playerId }
            GameWithPlayers(
                game = recentGame.toGame(gameId = gameId),
                players = recentGame.players.filter { it.uniqueId in stagePlayerIds }
            )
        }
    }

    fun getLatestGameStage(): GameStage {
        val gameStages = activeGameFlow.value?.gameStages ?: throw Exception("Stage not found")
        return runCatching {
            gameStages.maxByOrNull { it.stageNumber } ?: throw Exception("Stage not found")
        }.onFailure {
            Log.d(GamesHubLogTags.PLAY_CARDS_VIEW_MODEL, "failed to get latest stage: $it")
        }.getOrThrow()
    }

    fun getUser(): User {
        return googleAuthUiClient.signedInUser.value ?: throw Exception("User not found")
    }

    private suspend fun loadWinnerScreenInfo(
        game: Game,
        numberOfCardsPerPlayer: Int
    ) {
        try {
            println("winner: $game")

            if (game.gameState == GameState.Completed.name) {
                // completed
                val gameWithPlayers = getGameWithLatestStagePlayers()
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

                val gameStages = getGameStages()

                val nextStageNumber = gameStages.first().stageNumber

                // load next stage
                val gamePlayers = activeGameFlow
                    .first()
                    ?.players ?: return

                val nextStagePlayers = getGameWithLatestStagePlayers()
                    .first()
                    ?.players ?: return

                val eliminatedPlayers =
                    gamePlayers
                        .filterNot { player: Player -> player in nextStagePlayers }

                val winner = gamePlayers
                    .firstOrNull { it.uniqueId == game.winnerId }

                val loggedInUser = getUser().id

                val winnerNames = loggedInUser.toWinnerNames(winner)

                val playerIds = (nextStagePlayers.union(eliminatedPlayers))
                    .map { it.uniqueId }
                    .filterNot { it == winner?.uniqueId }

                val unPlayedCards = crusadeGameService.getUnPlayedCardsForPlayers(
                    gameId = gameId,
                    playerIds = playerIds,
                    gameStages = gameStages.filter { it.stageNumber == nextStageNumber },
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

            val loggedInUser = getUser()

            val otherPlayers = completedGame.otherPlayers.map { player ->
                player.copy(
                    profileName = loggedInUser.id.toWinnerNames(player)
                )
            }

            val unPlayedCards = crusadeGameService.getUnPlayedCardsForPlayers(
                gameId = gameId,
                gameStages = getGameStages(),
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
                winnerNames = loggedInUser.id.toWinnerNames(winner),
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