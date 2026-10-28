package com.learningis4fun.gameshub.api

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.learningis4fun.gameshub.api.model.FriendDto
import com.learningis4fun.gameshub.api.model.GameDto
import com.learningis4fun.gameshub.api.model.GameStageDto
import com.learningis4fun.gameshub.api.model.PlayerIdAndPositionDto
import com.learningis4fun.gameshub.api.model.RequestDto
import com.learningis4fun.gameshub.api.model.UserDto
import com.learningis4fun.gameshub.api.model.UserGameDto
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.domain.util.Response
import com.learningis4fun.gameshub.data.data_source.remote.GoogleAuthUiClient
import com.learningis4fun.gameshub.data.data_source.remote.dto.CardOfGameDto
import com.learningis4fun.gameshub.domain.model.CompletedGame
import com.learningis4fun.gameshub.domain.model.UserCard
import com.learningis4fun.gameshub.util.FirebaseCollections
import com.learningis4fun.gameshub.util.LogTags
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class CrusadeGameServiceImpl @Inject constructor(
    val googleAuthUiClient: GoogleAuthUiClient
) : CrusadeGameService {

    override suspend fun playCard(
        playerId: String,
        cardOfGameDto: CardOfGameDto
    ): Response<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun pickCards(
        playerId: String,
        gameId: String
    ): Response<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun allowOthersPlay(
        playerId: String,
        gameId: String
    ): Response<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun defineJokerAction(
        playerId: String,
        gameId: String,
        action: String
    ): Response<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun changeCardType(
        playerId: String,
        gameId: String,
        type: String
    ): Response<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun onUserCardClick(userCard: UserCard): Response<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun createGame(): Response<Unit> {
        return try {
            val user = googleAuthUiClient.getSignedInUser() ?: return Response.Error("User is null")

            val firebaseDatabase = Firebase.database
            val games = firebaseDatabase.getReference(FirebaseCollections.GAMES)
            val userGames = firebaseDatabase.getReference(FirebaseCollections.USER_GAMES)
            val friends = firebaseDatabase.getReference(FirebaseCollections.FRIENDS)

            val newGameRef = games.push()
            val gameId = newGameRef.key ?: throw Exception("Game id is null")
            Log.d(LogTags.CRUSADE_GAME_SERVICE_IMPL, "gameId: $gameId")

            userGames.child(user.id).child(gameId).setValue(
                UserGameDto(
                    owner = true,
                    online = false
                )
            ).await()

            // friends

            val playerIds = listOf(
                PlayerIdAndPositionDto(
                    playerId = user.id,
                    position = 1
                ),
                PlayerIdAndPositionDto(
                    playerId = "jrgEoBKqlXNZJtJ4zKatmWcJR9H2",
                    position = 2
                )
            )

            newGameRef.setValue(
                GameDto(
                    playerIds = playerIds,
                    gameStage = GameStageDto(
                        playerIds = playerIds
                    )
                )
            ).await()

            Response.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            Response.Error(e.message ?: "Unknown Error")
        }
    }

    override suspend fun saveAccount(): Response<Unit> {
        return try {
            Log.d(LogTags.CRUSADE_GAME_SERVICE_IMPL, "saveAccount")
            val firebaseDatabase = Firebase.database
            val user = googleAuthUiClient.getSignedInUser() ?: return Response.Error("User is null")
            val users = firebaseDatabase.getReference(FirebaseCollections.USERS)
            users.child(user.id).setValue(
                UserDto(
                    name = user.name,
                    email = user.email,
                    photoUrl = user.photoUrl
                )
            ).await()
            Log.d(LogTags.CRUSADE_GAME_SERVICE_IMPL, "savedAccount")
            Response.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            Log.d(LogTags.CRUSADE_GAME_SERVICE_IMPL, "e.message: ${e.message}")
            Response.Error(e.message ?: "Unknown Error")
        }
    }

    private fun getFriendDtos(): Flow<List<FriendDto>> = callbackFlow {
        val user = googleAuthUiClient.getSignedInUser()
        if (user == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val firebaseDatabase = Firebase.database

        val ref = firebaseDatabase
            .getReference(FirebaseCollections.FRIENDS)
            .child(user.id)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                println(snapshot)
                println(snapshot.children)
                val friends = snapshot.children.mapNotNull { childSnapshot ->
                    runCatching {
                        childSnapshot.getValue(FriendDto::class.java)
                    }.onFailure { error ->
                        Log.d(LogTags.CRUSADE_GAME_SERVICE_IMPL, "erorr: $error")
                    }.getOrNull()
                }
                trySend(friends).onFailure { error ->
                    Log.w(
                        LogTags.CRUSADE_GAME_SERVICE_IMPL,
                        "Failed to send friends update to flow",
                        error
                    )

                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)

        awaitClose { ref.removeEventListener(listener) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getFriends(): Flow<List<Player>> {

        val firebaseDatabase = Firebase.database

        return getFriendDtos().mapLatest { friendDtos ->
            val friends = coroutineScope {
                friendDtos.map { friendDto ->
                    async {
                        val user = firebaseDatabase.getReference(FirebaseCollections.USERS)
                            .child(friendDto.userId).get().await().getValue(UserDto::class.java)
                            ?: return@async null
                        Player(
                            uniqueId = friendDto.userId,
                            email = user.email,
                            profileName = user.name ?: "",
                            imageUrl = user.photoUrl ?: ""
                        )
                    }
                }
            }.awaitAll()
            return@mapLatest friends.filterNotNull()
        }
    }

    override suspend fun addUserToFriends(email: String): Response<Unit> {
        return try {
            val user = googleAuthUiClient.getSignedInUser() ?: return Response.Error("User is null")
            val friends = Firebase.database.getReference(FirebaseCollections.FRIENDS)
            val users = Firebase.database.getReference(FirebaseCollections.USERS)

            val snapshot = users.orderByChild("email")
                .equalTo(email.trim())
                .get()
                .await()

            val friendId =
                snapshot.children.firstOrNull()?.key ?: return Response.Error("User not found")

            friends.child(user.id)
                .child(friendId)
                .setValue(
                    FriendDto(
                        userId = friendId,
                        request = RequestDto(
                            accepted = false,
                            send = true
                        )
                    )
                )
            Response.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Response.Error(e.message ?: "Unknown Error")
        }
    }

    override fun getCompletedGames(): Flow<List<CompletedGame>> {
        TODO("Not yet implemented")
    }

    override fun getUnCompletedGames(): Flow<List<CompletedGame>> {
        TODO("Not yet implemented")
    }
}