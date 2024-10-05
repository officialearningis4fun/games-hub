package com.learningis4fun.gameshub.crusade_cards.commons.domain.repository

import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.AuthStates
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    suspend fun savePlayer(player: Player): Response<Unit>

    suspend fun getUserId(): String

    suspend fun getAndroidUserId(): String

    suspend fun updatePlayerLogin(player: Player): Response<Unit>

    suspend fun updatePlayer(player: Player): Response<Unit>

    suspend fun deletePlayer(player: Player): Response<Unit>

    fun getPlayer(playerId: String): Flow<Player?>

    fun getPlayerSignInDetails(): Flow<Player>

    fun getAndroidPlayerSignInDetails(): Flow<Player>

    fun getPlayers(): Flow<List<Player>>
}