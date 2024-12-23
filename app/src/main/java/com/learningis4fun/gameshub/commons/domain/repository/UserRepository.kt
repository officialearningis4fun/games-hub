package com.learningis4fun.gameshub.commons.domain.repository

import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.domain.util.Response
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    suspend fun savePlayer(player: Player): Response<Unit>

    suspend fun getUserId(): String

    suspend fun getComputerPlayerIds(): List<String>

    suspend fun updatePlayerLogin(player: Player): Response<Unit>

    suspend fun updatePlayer(player: Player): Response<Unit>

    suspend fun deletePlayer(player: Player): Response<Unit>

    fun getPlayer(playerId: String): Flow<Player?>

    fun getPlayerSignInDetails(): Flow<Player>

    fun getDevice1PlayerSignInDetails(): Flow<Player>
    fun getDevice2PlayerSignInDetails(): Flow<Player>
    fun getDevice3PlayerSignInDetails(): Flow<Player>

    fun getPlayers(): Flow<List<Player>>
}