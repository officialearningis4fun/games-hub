package com.learningis4fun.gameshub.feature_crusade_cards.commons.data.repository

import androidx.room.withTransaction
import com.learningis4fun.gameshub.feature_crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.feature_crusade_cards.commons.data.data_source.local.GamesDatabase
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.data.data_source.relations.GameWithPlayers
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.CardStateEntity
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.Game
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.GameUserCrossRef
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.repository.GameRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@ExperimentalCoroutinesApi
class GameRepositoryImpl @Inject constructor(
    private val db: GamesDatabase,
    private val crusadeCardsPreferencesManager: CrusadeCardsPreferencesManager
) : GameRepository {

    override suspend fun insertGame(game: Game): Response<String> {
        return try {
            db.gameDao().insertGame(game)
            Response.Success("", "Successfully inserted")
        } catch (e: Exception) {
            Response.Error("", e.localizedMessage ?: "Unknown error occurred")
        }
    }

    override suspend fun updateGame(game: Game): Response<String> {
        return try {
            db.gameDao().updateGame(game)
            Response.Success("", "Successfully updated")
        } catch (e: Exception) {
            Response.Error("", e.localizedMessage ?: "Unknown error occurred")
        }
    }

    override suspend fun deleteGame(game: Game): Response<String> {
        return try {
            val playersOfGame = db.gameDao().getGameWithPlayers(gameId = game.id).first().players
            db.withTransaction {
                playersOfGame.forEach { player ->
                    db.gameUserCrossRef().deleteGameUserCrossRef(
                        GameUserCrossRef(
                            gameId = game.id,
                            userId = player.uniqueId
                        )
                    )
                }
                db.gameDao().deleteGame(game)
            }
            Response.Success("", "Successfully deleted")
        } catch (e: Exception) {
            Response.Error("", e.localizedMessage ?: "Unknown error occurred")
        }
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

    override fun getGameWithPlayers(gameId: String): Flow<GameWithPlayers> {
        return db.gameDao().getGameWithPlayers(gameId)
    }

    override fun getCurrentGameId(): Flow<String> {
        return crusadeCardsPreferencesManager.recentGameId
    }

    override suspend fun updateRecentGameId(gameId: String): Response<String> {
        return try {
            crusadeCardsPreferencesManager.updateRecentGameId(gameId = gameId)
            Response.Success(gameId, "Successfully updated")
        } catch (e: Exception) {
            Response.Error("", e.localizedMessage ?: "Unknown error occurred")
        }
    }

}