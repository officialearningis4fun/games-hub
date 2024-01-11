package com.learningis4fun.gameshub.crusade_cards.commons.data.repository

import androidx.room.withTransaction
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.local.GamesDatabase
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.AuthStates
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.GameUserCrossRef
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.UserRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ExperimentalCoroutinesApi
class UserRepositoryImpl @ExperimentalCoroutinesApi
@Inject constructor(
    private val db: GamesDatabase,
    private val crusadeCardsPreferencesManager: CrusadeCardsPreferencesManager
) : UserRepository {

    private val authStates = MutableStateFlow<AuthStates>(AuthStates.NotSignedIn())
    private val player = Player(
        uniqueId = "uniqueId",
        email = "hlomphoshadrakmohapi@gmail.com",
        phone = "56142906",
        profileName = "Hlompho Mohapi",
        imageUrl = ""
    )

    override suspend fun googleSignInWithFirebase(account: GoogleSignInAccount): Response<String> {
        return withContext(Dispatchers.IO) {
            try {
                authStates.emit(AuthStates.SignedIn(player = player))
                Response.Success(data = player.email, "Signed in")
            } catch (e: Exception) {
                Response.Error("", message = e.localizedMessage.toString())
            }
        }
    }

    private fun firebaseAuthStateListener() = flow {
        authStates.emit(AuthStates.SignedIn(player))
        emit("user")
    }

    override fun authStateListener(): Flow<AuthStates> {
        return firebaseAuthStateListener()
            .onEach {
                syncSignIn()
            }.mapLatest {
                AuthStates.SignedIn()
            }
    }

    @ExperimentalCoroutinesApi
    override suspend fun updatePlayerLogin(player: Player): Response<String> {
        return crusadeCardsPreferencesManager.updatePlayer(player)
    }

    override suspend fun insertPlayer(player: Player): Response<String> {
        return try {
            db.playerDao().insertPlayer(player)
            Response.Success("", "Successfully inserted")
        } catch (e: Exception) {
            Response.Error("", e.localizedMessage ?: "Unknown error occurred")
        }
    }

    override suspend fun updatePlayer(player: Player): Response<String> {
        return try {
            db.playerDao().updatePlayer(player)
            Response.Success("", "Successfully updated")
        } catch (e: Exception) {
            Response.Error("", e.localizedMessage ?: "Unknown error occurred")
        }
    }

    override suspend fun deletePlayer(player: Player): Response<String> {
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
            Response.Success("", "Successfully deleted")
        } catch (e: Exception) {
            Response.Error("", e.localizedMessage ?: "Unknown error occurred")
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

    override suspend fun syncSignIn() {
        insertPlayer(player)
        val com = crusadeCardsPreferencesManager.AndroidUser.first()
        insertPlayer(com)
        crusadeCardsPreferencesManager.updatePlayer(player)
    }

    override fun signUserOut() {
        authStates.update {
            AuthStates.NotSignedIn()
        }
    }
}