package com.learningis4fun.gameshub.domain.use_cases

import com.learningis4fun.gameshub.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.commons.domain.model.Game
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.domain.model.PlayerIdAndPosition
import com.learningis4fun.gameshub.commons.domain.repository.DefaultCardRepository
import com.learningis4fun.gameshub.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.commons.domain.repository.GameStageRepository
import com.learningis4fun.gameshub.commons.domain.util.CardStates
import com.learningis4fun.gameshub.commons.domain.util.GameState
import com.learningis4fun.gameshub.commons.domain.util.Response
import com.learningis4fun.gameshub.util.getDataOrThrowException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@ExperimentalCoroutinesApi
class EndGameUseCase @Inject constructor(
    private val preferenceManager: CrusadeCardsPreferencesManager,
    private val repository: DefaultCardRepository,
    private val gameRepository: GameRepository,
    private val gameCardRepository: GameCardRepository,
    private val eliminatePlayerFromGame: EliminatePlayerFromGame,
    private val gameStageRepository: GameStageRepository,
    private val setupNextStageGame: SetupNextStageGame
) {

    /**
     * stop the counting time
     *
     * change game state to completed
     *
     * set the winMessage to have user who won
     *
     * delete all gameCards, set cardState to start
     *
     * delete gameUserCrossRef with gameId
     *
     * change playCardState to navigate to winScreen (popping off the playCardsScreen)
     */
    suspend operator fun invoke(
        currGameId: String,
        duration: Long,
        winnerId: String
    ): Response<Unit> {

        return try {

            val gameWithPlayers =
                gameRepository.getGameWithLatestStagePlayers(gameId = currGameId).first()

            // pause the game
            gameRepository.updateGame(
                gameWithPlayers.game.copy(
                    gameDuration = duration,
                    gameState = GameState.Paused.name
                )
            )
            val game = gameWithPlayers.game.copy(
                gameDuration = duration,
                gameState = GameState.Paused.name,
                winnerId = winnerId
            )

            val currentGameStage = gameStageRepository
                .getGameStages(gameId = game.id)
                .getDataOrThrowException()
                .firstOrNull()
                ?: throw Exception("The stage for a game not found")

            val players = gameWithPlayers.players

            return if (players.size == 2) {
                // no next stage end game
                endGame(
                    game = game,
                    winnerId = winnerId
                )
            } else {
                val eliminationLimit = preferenceManager.eliminationCountLimitForCrusade.first()

                val eliminatedPlayers: List<Pair<Player, Boolean>> =
                    players
                        .map { player ->
                            val eliminate = eliminatePlayerFromGame(
                                player = player,
                                game = game,
                                stageNumber = currentGameStage.stageNumber,
                                eliminationLimitForCrusade = eliminationLimit
                            ).getDataOrThrowException()

                            Pair(player, eliminate)
                        }

                // the winner should play first in the next game
                val nextStagePlayerIds = eliminatedPlayers
                    .filterNot { it.second }
                    .mapIndexed { index, pair ->
                        PlayerIdAndPosition(
                            playerId = pair.first.uniqueId,
                            position = index + 1
                        )
                    }
                    .ifEmpty { null }

                if (nextStagePlayerIds == null || nextStagePlayerIds.size == 1) {
                    // end game
                    endGame(
                        game = game,
                        winnerId = winnerId
                    )
                } else {
                    // if no player is eliminated - The winner is eliminated

                    if (eliminatedPlayers.count { it.second } == 0) {

                        val nextStagePlayerIdsWithoutWinner = nextStagePlayerIds
                            .filterNot { it.playerId == winnerId }
                            .map { (playerId, position) ->
                                PlayerIdAndPosition(
                                    position = position,
                                    playerId = playerId
                                )
                            }

                        val nextStage =
                            currentGameStage.nextStage(playerIds = nextStagePlayerIdsWithoutWinner)
                        setupNextStageGame(
                            game = game,
                            nextStage = nextStage,
                            currentStageNumber = currentGameStage.stageNumber
                        )
                    } else {
                        val nextStage = currentGameStage.nextStage(playerIds = nextStagePlayerIds)
                        setupNextStageGame(
                            game = game,
                            nextStage = nextStage,
                            currentStageNumber = currentGameStage.stageNumber
                        )

                    }

                }


            }
        } catch (e: Exception) {
            e.printStackTrace()
            Response.Error(e.message.toString())
        }

    }

    private suspend fun endGame(
        game: Game,
        winnerId: String
    ): Response<Unit> {
        return try {

            val gameCompletedTime = System.currentTimeMillis()

            val gameToEnd = game.copy(
                gameState = GameState.Completed.name,
                winnerId = winnerId,
                completedTime = gameCompletedTime
            )
            val currentGameStage = gameStageRepository.getGameStages(gameId = game.id)
                .getDataOrThrowException()
                .first()

            gameRepository.updateGame(gameToEnd).getDataOrThrowException()
            gameRepository.updateRecentGameId("").getDataOrThrowException()
            repository.changeCurrentCardType("").getDataOrThrowException()

            gameCardRepository.deleteAllGameCardsWithoutPlayer(
                gameId = game.id,
                stageNumber = currentGameStage.stageNumber
            ).getDataOrThrowException()

            preferenceManager.updateRecentCardState(
                com.learningis4fun.gameshub.domain.model.CardStateEntity(
                    cardStateName = CardStates.Start.name
                )
            )
            println("ended game")
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e.message.toString())
        }
    }
}