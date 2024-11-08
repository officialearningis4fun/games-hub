package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case

import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Game
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.PlayerIdAndPosition
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.DefaultCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameStageRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.CardStates
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.GameState
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.CardStateEntity
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.util.getDataOrThrowException
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
    ): Response<List<Pair<Player, Boolean>>> {

        return try {

            val gameWithPlayers =
                gameRepository.getGameWithLatestStagePlayers(gameId = currGameId).first()

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

                val eliminatedPlayers: List<Pair<Player, Boolean>> = players
                    .map { player ->
                        val eliminate = eliminatePlayerFromGame(
                            player = player,
                            game = game,
                            eliminationLimitForCrusade = eliminationLimit
                        ).getDataOrThrowException()

                        Pair(player, eliminate)
                    }

                println(eliminatedPlayers)

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

                        val winnerAsEliminatedPlayer = listOf(
                            eliminatedPlayers.first { it.first.uniqueId == winnerId }
                        )

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
                            gameStage = nextStage,
                            players = winnerAsEliminatedPlayer
                        )
                    } else {
                        val nextStage = currentGameStage.nextStage(playerIds = nextStagePlayerIds)
                        setupNextStageGame(
                            game = game,
                            gameStage = nextStage,
                            players = eliminatedPlayers
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
    ): Response<List<Pair<Player, Boolean>>> {
        return try {
            val gameCompletedTime = System.currentTimeMillis()

            val gameToEnd = game.copy(
                gameState = GameState.Completed.name,
                winnerId = winnerId,
                completedTime = gameCompletedTime
            )
            gameRepository.updateRecentGameId("").getDataOrThrowException()
            gameRepository.updateGame(gameToEnd).getDataOrThrowException()
            repository.changeCurrentCardType("").getDataOrThrowException()
            gameCardRepository.deleteAllGameCards().getDataOrThrowException()
            preferenceManager.updateRecentCardState(CardStateEntity(cardStateName = CardStates.Start.name))
            Response.Success(emptyList())
        } catch (e: Exception) {
            Response.Error(e.message.toString())
        }
    }
}