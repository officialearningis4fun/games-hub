package com.learningis4fun.gameshub.feature_crusade_cards.common.domain.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.AuthStates
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.repository.UserRepository
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.Response
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeUserRepositoryImpl : UserRepository {

    private val _players: MutableList<Player> = ArrayList()
    private var _player = Player()

    suspend fun insertDefaultPlayers(){
        val players = listOf(
            Player(uniqueId = Constants.Players.ANDROID_PLAYER.id),
            Player(uniqueId = Constants.Players.GUEST_PLAYER.id)
        )
        _player = Player(uniqueId = Constants.Players.GUEST_PLAYER.id)
        players.forEach {
            insertPlayer(it)
        }
    }

    override suspend fun insertPlayer(player: Player): Response<String> {
        return try {
            _players.add(player)
            Response.Success("", "Successfully updated")
        } catch (e: Exception) {
            Response.Error("", e.localizedMessage ?: "Unknown error occurred")
        }
    }

    override suspend fun getUserId(): String {
        return _players.find { it.uniqueId == Constants.Players.GUEST_PLAYER.id }?.uniqueId ?: ""
    }

    override suspend fun getAndroidUserId(): String {
        return _players.find { it.uniqueId == Constants.Players.ANDROID_PLAYER.id }?.uniqueId ?: ""
    }

    override suspend fun updatePlayerLogin(player: Player): Response<String> {
        TODO("Not yet implemented")
    }

    override suspend fun updatePlayer(player: Player): Response<String> {
        TODO("Not yet implemented")
    }

    override suspend fun deletePlayer(player: Player): Response<String> {
        TODO("Not yet implemented")
    }

    override fun getPlayer(playerId: String): Flow<Player?> {
        return flowOf(
            _players.find { it.uniqueId == playerId }
        )
    }

    override fun getPlayerSignInDetails(): Flow<Player> {
        return flowOf(Player(uniqueId = Constants.Players.GUEST_PLAYER.id))
    }

    override fun getAndroidPlayerSignInDetails(): Flow<Player> {
        return flowOf(Player(uniqueId = Constants.Players.ANDROID_PLAYER.id))
    }

    override fun getPlayers(): Flow<List<Player>> {
        return flowOf(_players)
    }

    override suspend fun googleSignInWithFirebase(account: GoogleSignInAccount): Response<String> {
        TODO("Not yet implemented")
    }

    override fun authStateListener(): Flow<AuthStates> {
        TODO("Not yet implemented")
    }

    override suspend fun syncSignIn() {
        TODO("Not yet implemented")
    }

    override fun signUserOut() {
        TODO("Not yet implemented")
    }
}