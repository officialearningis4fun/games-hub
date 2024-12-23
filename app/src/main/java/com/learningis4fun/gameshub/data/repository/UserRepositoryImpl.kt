package com.learningis4fun.gameshub.data.repository

import androidx.room.withTransaction
import com.learningis4fun.gameshub.commons.domain.model.GameUserCrossRef
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.domain.repository.UserRepository
import com.learningis4fun.gameshub.commons.domain.util.Response
import com.learningis4fun.gameshub.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.data.data_source.local.GamesDatabase
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
        return try {
            db.withTransaction {
                crusadeCardsPreferencesManager.updatePlayer(player)
                db.playerDao().savePlayer(player)
            }
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e.message.toString())
        }
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

    override fun getDevice1PlayerSignInDetails(): Flow<Player> {
        return crusadeCardsPreferencesManager.device1
    }

    override fun getDevice2PlayerSignInDetails(): Flow<Player> {
        return crusadeCardsPreferencesManager.device2
    }

    override fun getDevice3PlayerSignInDetails(): Flow<Player> {
        return crusadeCardsPreferencesManager.device3
    }

    override suspend fun getComputerPlayerIds(): List<String> {
        return listOf(
            getDevice1PlayerSignInDetails().first().uniqueId,
            getDevice2PlayerSignInDetails().first().uniqueId,
            getDevice3PlayerSignInDetails().first().uniqueId,
        )
    }

    override suspend fun getUserId(): String {
        return getPlayerSignInDetails().first().uniqueId
    }
}