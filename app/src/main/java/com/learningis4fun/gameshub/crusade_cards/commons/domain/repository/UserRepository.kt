package com.learningis4fun.gameshub.crusade_cards.commons.domain.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.AuthStates
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    suspend fun insertPlayer(player: Player): Response<String>

    suspend fun getUserId(): String

    suspend fun getAndroidUserId(): String

    suspend fun updatePlayerLogin(player: Player): Response<String>

    suspend fun updatePlayer(player: Player): Response<String>

    suspend fun deletePlayer(player: Player): Response<String>

    fun getPlayer(playerId: String): Flow<Player?>

    fun getPlayerSignInDetails(): Flow<Player>

    fun getAndroidPlayerSignInDetails(): Flow<Player>

    fun getPlayers(): Flow<List<Player>>

    suspend fun googleSignInWithFirebase(account: GoogleSignInAccount): Response<String>

    fun authStateListener(): Flow<AuthStates>

    suspend fun syncSignIn()

    fun signUserOut()
}