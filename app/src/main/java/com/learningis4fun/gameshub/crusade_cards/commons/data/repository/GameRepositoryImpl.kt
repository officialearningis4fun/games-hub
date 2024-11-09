package com.learningis4fun.gameshub.crusade_cards.commons.data.repository

import androidx.room.withTransaction
import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.local.GamesDatabase
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Game
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.GameUserCrossRef
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.relations.GameWithPlayers
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.CardStateEntity
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.util.getDataOrThrowException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

@ExperimentalCoroutinesApi
class GameRepositoryImpl @Inject constructor(
    private val db: GamesDatabase,
    private val crusadeCardsPreferencesManager: CrusadeCardsPreferencesManager,
) : GameRepository {

    override suspend fun insertGame(game: Game): Response<Unit> {
        return try {
            db.gameDao().insertGame(game)
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e.message.toString())
        }
    }

    override suspend fun updateGame(game: Game): Response<Unit> {
        return try {
            db.gameDao().updateGame(game)
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }

    override suspend fun deleteGame(gameId: String): Response<Unit> {
        return try {
            val game = db.gameDao().getGameWithPlayers(gameId = gameId).first()
            val playersOfGame = game.players

            db.withTransaction {
                playersOfGame.forEach { player ->
                    db.gameUserCrossRef().deleteGameUserCrossRef(
                        GameUserCrossRef(
                            gameId = gameId,
                            userId = player.uniqueId
                        )
                    )
                }
                db.gameDao().deleteGame(game.game)
                db.gameStageDao().deleteGameStages(gameId = gameId)
                val currentGameId = crusadeCardsPreferencesManager.recentGameId.first()
                if (currentGameId == gameId) {
                    crusadeCardsPreferencesManager.updateRecentGameId("").getDataOrThrowException()
                    crusadeCardsPreferencesManager.updateRecentCardState(CardStateEntity())
                    crusadeCardsPreferencesManager.updateRecentCardType("")
                }
            }
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }

    override suspend fun getNumOfCompletedGames(): Int {
        return db.gameDao().getNumOfCompletedGames()
    }

    override fun getNonCompletedGame(): Flow<Game> {
        return db.gameDao().getNonCompletedGame()
    }

    override fun getCompletedGames(): Flow<List<Game>> {
        return db.gameDao().getCompletedGames()
    }

    override fun getGame(gameId: String): Flow<Game?> {
        return db.gameDao().getCurrGame(gameId)
    }

    override suspend fun saveGameCardState(stateEntity: CardStateEntity) {
        crusadeCardsPreferencesManager.updateRecentCardState(stateEntity)
    }

    override fun getGameWithLatestStagePlayers(gameId: String): Flow<GameWithPlayers> {

        return db.gameDao().getGameWithPlayers(gameId).mapLatest { gameWithPlayers ->

            getGameWithDefaultSortedPlayers(gameWithPlayers = gameWithPlayers)

        }
    }

    override fun getGameWithLatestStagePlayersSorted(gameId: String): Flow<GameWithPlayers> {
        return db.gameDao().getGameWithPlayers(gameId).mapLatest { gameWithPlayers ->
            getGameWithSortedPlayers(gameWithPlayers = gameWithPlayers)
        }
    }

    private suspend fun getGameWithSortedPlayers(
        gameWithPlayers: GameWithPlayers
    ): GameWithPlayers {
        return try {

            val gameId = gameWithPlayers.game.id

            val latestGameStage = db.gameStageDao()
                .getGameStages(gameId).map { gameStage -> gameStage.toGameStage() }
                .firstOrNull()
                ?.playerIds

            val players = gameWithPlayers.players
                .mapNotNull { player ->
                    val playerIdAndPositionToSort =
                        latestGameStage?.find { playerIdAndPosition -> playerIdAndPosition.playerId == player.uniqueId }
                    playerIdAndPositionToSort?.let {
                        Pair(player, playerIdAndPositionToSort.position)
                    }
                }

            val sortedPlayers = when (gameWithPlayers.game.playingDirection) {
                Constants.CrusadePlayingDirection.ANTICLOCKWISE -> {
                    players.sortedByDescending { it.second }
                        .map { it.first }
                }

                else -> {
                    players.sortedBy { it.second }
                        .map { it.first }
                }
            }

            gameWithPlayers.copy(
                players = sortedPlayers
            )
        } catch (e: Exception) {
            gameWithPlayers
        }
    }

    private suspend fun getGameWithDefaultSortedPlayers(
        gameWithPlayers: GameWithPlayers
    ): GameWithPlayers {
        return try {

            val gameId = gameWithPlayers.game.id

            val latestGameStage = db.gameStageDao()
                .getGameStages(gameId).map { gameStage -> gameStage.toGameStage() }
                .firstOrNull()
                ?.playerIds

            val players = gameWithPlayers.players
                .mapNotNull { player ->
                    val playerIdAndPositionToSort =
                        latestGameStage?.find { playerIdAndPosition -> playerIdAndPosition.playerId == player.uniqueId }
                    playerIdAndPositionToSort?.let {
                        Pair(player, playerIdAndPositionToSort.position)
                    }
                }

            val sortedPlayers = players.sortedBy { it.second }
                .map { it.first }

            gameWithPlayers.copy(
                players = sortedPlayers
            )
        } catch (e: Exception) {
            gameWithPlayers
        }
    }

    override fun getGameWithPlayers(gameId: String): Flow<GameWithPlayers> {
        return db.gameDao().getGameWithPlayers(gameId)
    }


    override fun getCompletedGamesWithPlayers(): Flow<List<GameWithPlayers>> {
        return db.gameDao().getCompletedGamesWithPlayers()
    }

    override fun getCurrentGameId(): Flow<String> {
        return crusadeCardsPreferencesManager.recentGameId
    }

    override suspend fun updateRecentGameId(gameId: String): Response<Unit> {
        return try {
            crusadeCardsPreferencesManager.updateRecentGameId(gameId = gameId)
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }

}