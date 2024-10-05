package com.learningis4fun.gameshub.crusade_cards.commons.data.repository

import androidx.room.withTransaction
import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.local.GamesDatabase
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.GameUserCrossRef
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.UserRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@ExperimentalCoroutinesApi
class UserRepositoryImpl @ExperimentalCoroutinesApi
@Inject constructor(
    private val db: GamesDatabase,
    private val crusadeCardsPreferencesManager: CrusadeCardsPreferencesManager
) : UserRepository {

    @ExperimentalCoroutinesApi
    override suspend fun updatePlayerLogin(player: Player): Response<Unit> {
        return crusadeCardsPreferencesManager.updatePlayer(player)
    }

    override suspend fun savePlayer(player: Player): Response<Unit> {
        return try {
            db.playerDao().savePlayer(player)
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }

    override suspend fun updatePlayer(player: Player): Response<Unit> {
        return try {
            db.playerDao().savePlayer(player)
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }

    override suspend fun deletePlayer(player: Player): Response<Unit> {
        return try {
            val gamesOfUser =
                db.playerDao().getGamesOfPlayer(playerId = player.uniqueId).first().games
            db.withTransaction {
                gamesOfUser.forEach {
                    db.gameUserCrossRef().deleteGameUserCrossRef(
                        GameUserCrossRef(
                            gameId = it.id,
                            userId = player.uniqueId
                        )
                    )
                }
                db.playerDao().deletePlayer(player)
            }
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }

    override fun getPlayer(playerId: String): Flow<Player?> {
        return db.playerDao().getPlayer(playerId)
    }

    override fun getPlayers(): Flow<List<Player>> {
        return db.playerDao().getPlayers()
    }

    override fun getPlayerSignInDetails(): Flow<Player> {
        return crusadeCardsPreferencesManager.user
    }

    override fun getAndroidPlayerSignInDetails(): Flow<Player> {
        return crusadeCardsPreferencesManager.AndroidUser
    }

    override suspend fun getAndroidUserId(): String {
        return getAndroidPlayerSignInDetails().first().uniqueId
    }

    override suspend fun getUserId(): String {
        return getPlayerSignInDetails().first().uniqueId
    }
}