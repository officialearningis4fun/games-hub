package com.learningis4fun.gameshub.api

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.learningis4fun.gameshub.api.model.UserGame
import com.learningis4fun.gameshub.api.model.dto.CardDto
import com.learningis4fun.gameshub.api.model.dto.FriendDto
import com.learningis4fun.gameshub.api.model.dto.GameCardDto
import com.learningis4fun.gameshub.api.model.dto.GameDto
import com.learningis4fun.gameshub.api.model.dto.GameStageDto
import com.learningis4fun.gameshub.api.model.dto.PlayerIdAndPositionDto
import com.learningis4fun.gameshub.api.model.dto.RecentCardStateDto
import com.learningis4fun.gameshub.api.model.dto.RequestDto
import com.learningis4fun.gameshub.api.model.dto.UserDto
import com.learningis4fun.gameshub.api.model.dto.UserGameDto
import com.learningis4fun.gameshub.commons.domain.model.ActiveGame
import com.learningis4fun.gameshub.commons.domain.model.Card
import com.learningis4fun.gameshub.commons.domain.model.Game
import com.learningis4fun.gameshub.commons.domain.model.GameStage
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.domain.util.CardStates
import com.learningis4fun.gameshub.commons.domain.util.CardsErrorMessages
import com.learningis4fun.gameshub.commons.domain.util.Constants
import com.learningis4fun.gameshub.commons.domain.util.GameState
import com.learningis4fun.gameshub.commons.domain.util.Response
import com.learningis4fun.gameshub.commons.domain.util.toCardState
import com.learningis4fun.gameshub.commons.domain.util.toState
import com.learningis4fun.gameshub.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.data.data_source.relations.GameWithPlayers
import com.learningis4fun.gameshub.data.data_source.remote.GoogleAuthUiClient
import com.learningis4fun.gameshub.domain.model.CardStateEntity
import com.learningis4fun.gameshub.domain.model.CompletedGame
import com.learningis4fun.gameshub.domain.model.GameCard
import com.learningis4fun.gameshub.domain.model.UserCard
import com.learningis4fun.gameshub.presentation.start.OnGoingGame
import com.learningis4fun.gameshub.util.FirebaseCollections
import com.learningis4fun.gameshub.util.FirebaseDatabaseFields
import com.learningis4fun.gameshub.util.GamesHubLogTags
import com.learningis4fun.gameshub.util.LogTags
import com.learningis4fun.gameshub.util.getDataOrThrowException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.collections.map
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
class CrusadeGameServiceImpl @Inject constructor(
    val googleAuthUiClient: GoogleAuthUiClient,
    val preferencesManager: CrusadeCardsPreferencesManager
) : CrusadeGameService {

    override suspend fun playCard(
        userCard: UserCard
    ): Response<Unit> {
        return try {
            val firebaseDatabase = Firebase.database
            val gameCardRef =
                firebaseDatabase.getReference(FirebaseCollections.GAME_CARDS)
                    .child(userCard.gameId)
                    .child(userCard.gameCardId)

            val gameRef =
                firebaseDatabase.getReference(FirebaseCollections.GAMES)
                    .child(userCard.gameId)

            Log.d(LogTags.CRUSADE_GAME_SERVICE_IMPL, "saved userCard: $userCard")
            val playTime = System.currentTimeMillis()
            gameCardRef.updateChildren(
                mapOf(
                    FirebaseDatabaseFields.PLAY_TIME to playTime,
                    FirebaseDatabaseFields.PLAYED to true,
                    FirebaseDatabaseFields.PLAYER_ID to userCard.playerId,
                )
            ).await()
            val cardTypeIfJocker = if (userCard.name.lowercase() == Constants.CardNames.JOKER) {
                ""
            } else userCard.type

            gameRef.updateChildren(
                mapOf(
                    FirebaseDatabaseFields.CURRENT_CARD_TYPE to cardTypeIfJocker,
                    FirebaseDatabaseFields.CURRENT_CARD_NAME to userCard.name.lowercase(),
                    FirebaseDatabaseFields.GAME_CARD_ID to userCard.gameCardId
                )
            ).await()

            Response.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            Response.Error(e.message ?: "Unknown Error")
        }
    }

    override suspend fun pickCards(
        playerId: String,
        gameId: String,
        stageNumber: Int,
        numOfCardsToPick: Int,
    ): Response<Int> {
        return try {
            val firebaseDatabase = Firebase.database
            val baseRef =
                firebaseDatabase.getReference(FirebaseCollections.GAME_CARDS).child(gameId)

            val gameCardsRef = baseRef.orderByChild(FirebaseDatabaseFields.STAGE_NUMBER)
                .equalTo(stageNumber.toDouble())

            val cardsInDustbin = gameCardsRef.get().await().children.mapNotNull {
                runCatching {
                    val gameCardId = it.key ?: return@runCatching null
                    it.getValue(GameCardDto::class.java)?.toGameCard(gameCardId = gameCardId)
                }.onFailure { error ->
                    Log.d(LogTags.CRUSADE_GAME_SERVICE_IMPL, "error: $error")
                }.getOrNull()
            }

            val numOfCardsInDustbin = cardsInDustbin.size

            if (numOfCardsInDustbin >= numOfCardsToPick) {
                // can safely pick
                pickingCardsFromDustbin(
                    playerId = playerId,
                    gameId = gameId,
                    cardsToPick = cardsInDustbin.shuffled().take(numOfCardsToPick)
                )
            } else {
                // request other cards first
                val response = requestCardsFromSceneToBePicked(
                    gameId = gameId,
                    numOfCardsToPick = numOfCardsToPick,
                    numOfCardsAlreadyAvailableToPick = numOfCardsInDustbin,
                    stageNumber = stageNumber
                )

                when (response) {
                    is Response.Error -> {
                        throw Exception(response.message)
                    }

                    is Response.Success -> {

                        val cardsInDustbinAfterRequesting =
                            gameCardsRef.get().await().children.mapNotNull {
                                runCatching {
                                    val gameCardId = it.key ?: return@runCatching null
                                    it.getValue(GameCardDto::class.java)
                                        ?.toGameCard(gameCardId = gameCardId)
                                }.onFailure { error ->
                                    Log.d(LogTags.CRUSADE_GAME_SERVICE_IMPL, "error: $error")
                                }.getOrNull()
                            }.shuffled().take(numOfCardsToPick)

                        pickingCardsFromDustbin(
                            playerId = playerId,
                            gameId = gameId,
                            cardsToPick = cardsInDustbinAfterRequesting
                        )
                    }
                }
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            Response.Error(e.message ?: "Unknown Error")
        }
    }

    private fun pickingCardsFromDustbin(
        playerId: String, cardsToPick: List<GameCard>, gameId: String
    ): Response<Int> {
        return try {
            val firebaseDatabase = Firebase.database
            val baseRef =
                firebaseDatabase.getReference(FirebaseCollections.GAME_CARDS).child(gameId)

            cardsToPick.forEach { gameCard ->
                baseRef.child(gameCard.id).updateChildren(
                    mapOf(
                        FirebaseDatabaseFields.PLAYER_ID to playerId,
                        FirebaseDatabaseFields.PICKED_TIME to System.currentTimeMillis()

                    )
                )
            }

            Response.Success(data = cardsToPick.size)
        } catch (exception: Exception) {
            Response.Error(
                message = exception.localizedMessage ?: "Unknown error occurred while picking cards"
            )
        }
    }

    private suspend fun requestCardsFromSceneToBePicked(
        gameId: String,
        stageNumber: Int,
        numOfCardsToPick: Int,
        numOfCardsAlreadyAvailableToPick: Int
    ): Response<Int> {

        return try {
            val firebaseDatabase = Firebase.database
            val baseRef =
                firebaseDatabase.getReference(FirebaseCollections.GAME_CARDS).child(gameId)

            val cardsInDustbin = getPlayedCards(
                gameId = gameId, stageNumber = stageNumber
            ).getDataOrThrowException().sortedByDescending { it.playTime }
            val sizeOfCardsInDustbin = cardsInDustbin.size

            val numOfTotalCardsAvailableForPicking =
                sizeOfCardsInDustbin - 1 + numOfCardsAlreadyAvailableToPick

            if (numOfTotalCardsAvailableForPicking >= numOfCardsToPick) {

                val cardsToRequest = cardsInDustbin.slice(1..cardsInDustbin.lastIndex)

                // request cards
                cardsToRequest.forEach { gameCard ->
                    baseRef.child(gameCard.id).updateChildren(
                        mapOf(
                            FirebaseDatabaseFields.PLAYER_ID to "",
                            FirebaseDatabaseFields.PICKED_TIME to 0L,
                            FirebaseDatabaseFields.PLAY_TIME to 0L,
                            FirebaseDatabaseFields.STAGE_NUMBER to stageNumber,
                            FirebaseDatabaseFields.PLAYED to false

                        )
                    )
                }

                Response.Success(cardsToRequest.size)

            } else {
                throw Exception(CardsErrorMessages.NOT_ENOUGH_CARDS_TO_BE_PICKED)
            }
        } catch (exception: Exception) {
            Response.Error(
                exception.localizedMessage
                    ?: "Unknown error occurred while requesting cards to be picked"
            )
        }
    }

    override suspend fun updateGamePlayingDirection(
        gameId: String, direction: String
    ): Response<Unit> {
        return try {
            val firebaseDatabase = Firebase.database
            val ref = firebaseDatabase.getReference(FirebaseCollections.GAMES).child(gameId)

            ref.updateChildren(mapOf(FirebaseDatabaseFields.PLAYING_DIRECTION to direction)).await()
            Response.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Response.Error(e.message ?: "Unknown Error")
        }
    }

    override suspend fun getLatestStagePlayerIdsSorted(gameId: String): Response<List<String>> {
        return try {
            val firebaseDatabase = Firebase.database
            val ref = firebaseDatabase.getReference(FirebaseCollections.GAMES).child(gameId)
            val data = ref.get().await().getValue(GameDto::class.java)
                ?.gameStages?.maxByOrNull { it.stageNumber }
                ?.playerIds
                ?.map { it.playerId }
                ?: throw Exception("Game is null")
            Response.Success(data)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Response.Error(e.message ?: "Unknown Error")
        }
    }

    override suspend fun getGame(gameId: String): Response<Game> {
        return try {
            val firebaseDatabase = Firebase.database
            val ref = firebaseDatabase.getReference(FirebaseCollections.GAMES).child(gameId)
            val game = ref.get().await().getValue(GameDto::class.java)?.toGame(gameId = gameId)
                ?: throw Exception("Game is null")
            Response.Success(game)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Response.Error(e.message ?: "Unknown Error")
        }
    }

    override suspend fun changeCardType(
        playerId: String, gameId: String, type: String
    ): Response<Unit> {
        return try {
            Log.d(
                LogTags.CRUSADE_GAME_SERVICE_IMPL,
                "playerId: $playerId gameId: $gameId type: $type"
            )
            val firebaseDatabase = Firebase.database
            val ref = firebaseDatabase
                .getReference(FirebaseCollections.GAMES)
                .child(gameId)

            ref.updateChildren(mapOf(FirebaseDatabaseFields.CURRENT_CARD_TYPE to type)).await()
            Response.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Response.Error(e.message ?: "Unknown Error")
        }
    }

    override suspend fun getCard(
        gameId: String, gameCardId: String
    ): Response<Card> {
        return try {
            val firebaseDatabase = Firebase.database
            val gameCardsRef =
                firebaseDatabase.getReference(FirebaseCollections.GAME_CARDS).child(gameId)
                    .child(gameCardId)
            val snapshot = gameCardsRef.get().await()
            val data = runCatching {
                val gameCardId = snapshot.key ?: return@runCatching null
                snapshot.getValue(GameCardDto::class.java)?.toUserCard(gameCardId = gameCardId)
                    ?.toCard()
            }.onFailure { error ->
                Log.d(LogTags.CRUSADE_GAME_SERVICE_IMPL, "error: $error")
            }.getOrThrow()!!

            Response.Success(data)

        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Response.Error(e.message ?: "Unknown Error")
        }
    }

    override suspend fun getUserCard(gameId: String, gameCardId: String): Response<UserCard> {
        return try {
            val firebaseDatabase = Firebase.database
            val gameCardsRef =
                firebaseDatabase.getReference(FirebaseCollections.GAME_CARDS).child(gameId)
                    .child(gameCardId)
            val snapshot = gameCardsRef.get().await()
            val data = runCatching {
                val gameCardId = snapshot.key ?: return@runCatching null
                snapshot.getValue(GameCardDto::class.java)?.toUserCard(gameCardId = gameCardId)
            }.onFailure { error ->
                Log.d(LogTags.CRUSADE_GAME_SERVICE_IMPL, "error: $error")
            }.getOrThrow()!!

            Response.Success(data)

        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Response.Error(e.message ?: "Unknown Error")
        }
    }

    override suspend fun updateCardState(
        gameId: String, cardStateEntity: CardStateEntity
    ): Response<Unit> {
        return try {
            val firebaseDatabase = Firebase.database
            val ref = firebaseDatabase
                .getReference(FirebaseCollections.GAMES)
                .child(gameId)
            Log.d(LogTags.CRUSADE_GAME_SERVICE_IMPL, "cardStateEntity: $cardStateEntity")

            ref.updateChildren(mapOf(FirebaseDatabaseFields.RECENT_CARD_STATE to cardStateEntity))
                .await()
            Response.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Response.Error(e.message ?: "Unknown Error")
        }
    }

    override suspend fun deletedUnPlayedCards(gameId: String, stageNumber: Int): Response<Unit> {
        return try {
            val firebaseDatabase = Firebase.database
            val baseRef = firebaseDatabase
                .getReference(FirebaseCollections.GAME_CARDS).child(gameId)

            val gamesRef = baseRef
                .orderByChild(FirebaseDatabaseFields.STAGE_NUMBER)
                .equalTo(stageNumber.toDouble(), FirebaseDatabaseFields.STAGE_NUMBER)

            val gameCardIds =
                gamesRef.get().await().children.mapNotNull { snapshot ->
                    snapshot.key
                    val gameCardId = snapshot.key ?: return@mapNotNull null
                    snapshot.getValue(GameCardDto::class.java)
                        ?.toGameCard(gameCardId = gameCardId)
                }.filter { !it.isPlayed }
                    .map { it.id }

            gameCardIds.forEach { gameCardId ->
                baseRef.child(gameCardId).removeValue().await()
            }

            Response.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            Response.Error(e.message ?: "Unknown Error")
        }
    }

    override fun getUnPlayedCardsForUser(
        playerId: String, stageNumber: Int, gameId: String
    ): Flow<List<UserCard>> = callbackFlow {
        val firebaseDatabase = Firebase.database
        val baseRef = firebaseDatabase
            .getReference(FirebaseCollections.GAME_CARDS)
            .child(gameId)
        val ref = baseRef.orderByChild(FirebaseDatabaseFields.STAGE_NUMBER)
            .equalTo(stageNumber.toDouble())

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.children.mapNotNull { childSnapshot ->
                    runCatching {
                        val gameCardId = childSnapshot.key ?: return@runCatching null
                        val userCard = childSnapshot.getValue(GameCardDto::class.java)
                            ?.toUserCard(gameCardId = gameCardId)
                        if (userCard?.isPlayed == false && userCard.playerId == playerId) {
                            userCard
                        } else null
                    }.onFailure { error ->
                        Log.d(LogTags.CRUSADE_GAME_SERVICE_IMPL, "error: $error")
                    }.getOrNull()
                }
                trySend(data).onFailure { error ->
                    Log.w(
                        LogTags.CRUSADE_GAME_SERVICE_IMPL,
                        "Failed to send user cards update to flow",
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

    override suspend fun getUnPlayedCardsForPlayers(
        gameId: String,
        playerIds: List<String>,
        gameStages: List<GameStage>
    ): List<UserCard> {
        return try {

            val unPlayedCards: List<UserCard> = playerIds.mapNotNull { playerId ->

                val latestStageOfPlayer = gameStages
                    .firstOrNull { gameStage -> playerId in gameStage.playerIds.map { it.playerId } }
                    ?: return@mapNotNull null

                Log.d(
                    GamesHubLogTags.GET_LATEST_UN_PLAYED_CARDS,
                    "ID: $playerId latestStage$latestStageOfPlayer"
                )

                getUnPlayedCardsForUser(
                    playerId = playerId,
                    gameId = gameId,
                    stageNumber = latestStageOfPlayer.stageNumber
                ).first()

            }.flatten()

            unPlayedCards
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getPlayedCards(
        stageNumber: Int, gameId: String
    ): Response<List<GameCard>> {
        return try {
            val firebaseDatabase = Firebase.database
            val baseRef =
                firebaseDatabase.getReference(FirebaseCollections.GAME_CARDS).child(gameId)
            val gameCardsRef = baseRef.orderByChild(FirebaseDatabaseFields.STAGE_NUMBER)
                .equalTo(stageNumber.toDouble(), FirebaseDatabaseFields.STAGE_NUMBER)

            val gameCards = gameCardsRef.get().await().children.mapNotNull {
                runCatching {
                    val gameCardId = it.key ?: return@runCatching null
                    it.getValue(GameCardDto::class.java)?.toGameCard(gameCardId = gameCardId)
                }.onFailure { error ->
                    Log.d(LogTags.CRUSADE_GAME_SERVICE_IMPL, "error: $error")
                }.getOrNull()
            }.filter { it.isPlayed }

            Response.Success(gameCards)

        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Response.Error(e.message ?: "Unknown Error")
        }
    }

    override fun getNumOfUnPlayedCardsForUser(
        playerId: String, stageNumber: Int, gameId: String
    ): Flow<Int> {
        return getUnPlayedCardsForUser(
            playerId = playerId,
            stageNumber = stageNumber,
            gameId = gameId
        ).map { it.size }
    }

    override suspend fun getTopPlayedCard(
        gameId: String, playerIds: List<String>, stageNumber: Int
    ): Response<UserCard> {
        return try {
            val firebaseDatabase = Firebase.database
            val baseRef =
                firebaseDatabase.getReference(FirebaseCollections.GAME_CARDS).child(gameId)
            val gameCardsRef = baseRef.orderByChild(FirebaseDatabaseFields.STAGE_NUMBER)
                .equalTo(stageNumber.toDouble(), FirebaseDatabaseFields.STAGE_NUMBER)
                .limitToFirst(1)

            val gameCards = gameCardsRef.get().await().children.mapNotNull {
                runCatching {
                    val gameCardId = it.key ?: return@runCatching null
                    val userCard = it.getValue(GameCardDto::class.java)
                        ?.toUserCard(gameCardId = gameCardId)
                        ?: return@runCatching null

                    if (userCard.isPlayed) {
                        userCard
                    } else {
                        null
                    }
                }.onFailure { error ->
                    Log.d(LogTags.CRUSADE_GAME_SERVICE_IMPL, "error: $error")
                }.getOrNull()
            }

            Response.Success(gameCards.first())

        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Response.Error(e.message ?: "Unknown Error")
        }
    }

    override fun getTopThreePlayedCards(
        gameId: String, playerIds: List<String>, stageNumber: Int
    ): Flow<List<UserCard>> = callbackFlow {
        val firebaseDatabase = Firebase.database
        val baseRef = firebaseDatabase.getReference(FirebaseCollections.GAME_CARDS)
            .child(gameId)
        val ref = baseRef.orderByChild(FirebaseDatabaseFields.STAGE_NUMBER)
            .equalTo(stageNumber.toDouble())

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.children.mapNotNull { childSnapshot ->
                    runCatching {
                        val gameCardId = childSnapshot.key ?: return@runCatching null

                        val userCard = childSnapshot.getValue(GameCardDto::class.java)
                            ?.toUserCard(gameCardId = gameCardId)
                        Log.d(LogTags.CRUSADE_GAME_SERVICE_IMPL, "gameCard on top 3: $userCard")
                        userCard
                    }.onFailure { error ->
                        Log.d(LogTags.CRUSADE_GAME_SERVICE_IMPL, "error: $error")
                    }.getOrNull()
                }.filter { it.isPlayed }
                    .sortedByDescending { it.playTime }
                    .take(3)
                trySend(data).onFailure { error ->
                    Log.w(
                        LogTags.CRUSADE_GAME_SERVICE_IMPL,
                        "Failed to send user cards update to flow",
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

    private fun getGameCardsWithAssignedCards(
        gameId: String,
        howManyCardsPerPlayer: Int,
        playerIds: List<String>,
        cards: List<Card>,
        stageNumber: Int
    ): Response<List<GameCardDto>> {
        return try {

            val gameCards = cards.map { card ->
                GameCardDto(
                    gameId = gameId,
                    cardId = card.id,
                    name = card.name,
                    type = card.type,
                    image = card.image,
                    stageNumber = stageNumber
                )
            }
            val cardsToAssign = gameCards.shuffled()

            val playersCards: MutableList<GameCardDto> = ArrayList(cardsToAssign)

            var lastAccessedIndex = 0
            playerIds.forEachIndexed { index, id ->
                val pickTime = System.currentTimeMillis()
                val startIndex = if (index == 0) {
                    0
                } else {
                    lastAccessedIndex + 1
                }
                val endIndex = startIndex + howManyCardsPerPlayer

                for (count in endIndex downTo startIndex) {
                    playersCards[count] = playersCards[count].copy(
                        playerId = id, pickedTime = pickTime
                    )
                }

                lastAccessedIndex = endIndex
            }

            Response.Success(playersCards)
        } catch (e: Exception) {
            Response.Error(e.message.toString())
        }
    }

    override suspend fun createGameCards(
        gameId: String, stageNumber: Int, playerIds: List<String>
    ): Response<Unit> {
        return try {

            val firebaseDatabase = Firebase.database
            val cardsRef = firebaseDatabase.getReference(FirebaseCollections.CARDS)
            val gameCardsRef =
                firebaseDatabase.getReference(FirebaseCollections.GAME_CARDS).child(gameId)

            val cards = cardsRef.get().await().children.map { cardSnapshot ->
                cardSnapshot.getValue(CardDto::class.java)?.toCard()
                    ?: throw Exception("Card is null")
            }
            val howManyCardsPerPlayer = preferencesManager.numberOfCardsPerPlayer.first()

            val gameCards = getGameCardsWithAssignedCards(
                gameId = gameId,
                playerIds = playerIds,
                cards = cards,
                stageNumber = stageNumber,
                howManyCardsPerPlayer = howManyCardsPerPlayer
            ).getDataOrThrowException()

            gameCards.forEach { gameCardDto ->
                val gameCardRef = gameCardsRef.push()
                gameCardRef.setValue(gameCardDto)
            }

            Response.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Response.Error(e.message ?: "Unknown Error")
        }
    }

    override suspend fun createGame(
        opponentIds: List<String>
    ): Response<String> {
        return try {
            val user = googleAuthUiClient.getSignedInUser() ?: return Response.Error("User is null")

            val firebaseDatabase = Firebase.database
            val games = firebaseDatabase.getReference(FirebaseCollections.GAMES)
            val userGames = firebaseDatabase.getReference(FirebaseCollections.USER_GAMES)

            val newGameRef = games.push()
            val gameId = newGameRef.key ?: throw Exception("Game id is null")
            Log.d(LogTags.CRUSADE_GAME_SERVICE_IMPL, "gameId: $gameId")

            userGames.child(user.id).child(gameId).setValue(
                UserGameDto(
                    owner = true, online = true, completed = false
                )
            ).await()

            opponentIds.forEach { opponentId ->
                userGames.child(opponentId).child(gameId).setValue(
                    UserGameDto(
                        owner = false, online = true, completed = false
                    )
                ).await()
            }

            // friends
            val playerIdAndPositionDtos: MutableList<PlayerIdAndPositionDto> = mutableListOf()
            val opponents = opponentIds.mapIndexed { index, string ->
                PlayerIdAndPositionDto(
                    playerId = string, position = index + 2
                )
            }
            playerIdAndPositionDtos.add(PlayerIdAndPositionDto(playerId = user.id, position = 1))
            playerIdAndPositionDtos.addAll(opponents)

            val cardState = CardStates.Play(
                playerId = user.id,
                canPick = true,
            )
            newGameRef.setValue(
                GameDto(
                    playerIds = playerIdAndPositionDtos,
                    gameStages = listOf(
                        GameStageDto(
                            stageNumber = 1, playerIds = playerIdAndPositionDtos
                        )
                    ),
                    recentCardState = RecentCardStateDto(
                        cardStateName = cardState.name,
                        playerId = cardState.playerId,
                        canPick = cardState.canPick,
                        message = cardState.message
                    )
                )
            ).await()

            createGameCards(
                gameId = gameId,
                stageNumber = 1,
                playerIds = playerIdAndPositionDtos.map { it.playerId }).getDataOrThrowException()

            Response.Success(gameId)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            Response.Error(e.message ?: "Unknown Error")
        }
    }

    private suspend fun getPlayers(playerIds: List<String>): List<Player> {
        val firebaseDatabase = Firebase.database
        val usersRef = firebaseDatabase.getReference(FirebaseCollections.USERS)
        val players = playerIds.mapNotNull { playerId ->
            val userSnapshot = usersRef.child(playerId).get().await()
            val userId = userSnapshot.key ?: return@mapNotNull null

            val user = userSnapshot.getValue(UserDto::class.java) ?: return@mapNotNull null

            Player(
                uniqueId = userId,
                email = user.email,
                phone = "",
                imageUrl = user.photoUrl ?: "",
                profileName = user.name ?: ""
            )
        }
        return players
    }

    fun getGameDto(gameId: String): Flow<GameDto?> = callbackFlow {
        val firebaseDatabase = Firebase.database
        val ref = firebaseDatabase.getReference(FirebaseCollections.GAMES).child(gameId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val gameDto = runCatching {
                    snapshot.getValue(GameDto::class.java) ?: return@runCatching null
                }.onFailure { error ->
                    Log.d(LogTags.CRUSADE_GAME_SERVICE_IMPL, "error: $error")
                }.getOrNull()

                trySend(gameDto).onFailure { error ->
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
    override fun recentGame(gameId: String): Flow<ActiveGame?> {
        return getGameDto(gameId = gameId).mapLatest { gameDto ->
            if (gameDto == null) return@mapLatest null
            val players = getPlayers(playerIds = gameDto.playerIds.map { it.playerId })
            gameDto.toActiveGame(players = players, gameId = gameId)
        }
    }

    override suspend fun deleteGame(gameId: String): Response<Unit> {
        return try {
            val user = googleAuthUiClient.getSignedInUser() ?: throw Exception("User is null")

            val firebaseDatabase = Firebase.database

            val userGameRef = firebaseDatabase
                .getReference(FirebaseCollections.USER_GAMES)
                .child(user.id)
                .child(gameId)

            val userGame = userGameRef
                .get()
                .await()
                .getValue(UserGameDto::class.java)
                ?: throw Exception("User game is not found")

            if (!userGame.owner) {
                throw Exception("You are not the owner of this game")
            }

            val gameRef = firebaseDatabase
                .getReference(FirebaseCollections.GAMES)
                .child(gameId)

            val game = gameRef
                .get()
                .await()
                .getValue(GameDto::class.java)
                ?: throw Exception("game not found")

            val gameCardsRef = firebaseDatabase
                .getReference(FirebaseCollections.GAME_CARDS)
                .child(gameId)

            // delete user-games
            game.playerIds.forEach { (playerId, _) ->
                firebaseDatabase
                    .getReference(FirebaseCollections.USER_GAMES)
                    .child(playerId)
                    .child(gameId)
                    .removeValue()
                    .await()
            }
            gameRef.removeValue().await()
            gameCardsRef.removeValue().await()

            Response.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            Response.Error(e.message ?: "Unknown Error")
        }
    }

    override suspend fun pauseGame(gameId: String, duration: Long): Response<Unit> {
        return try {
            val firebaseDatabase = Firebase.database
            val gameRef = firebaseDatabase
                .getReference(FirebaseCollections.GAMES)
                .child(gameId)

            val game = gameRef.get().await().getValue(GameDto::class.java)
                ?: throw Exception("Game is null")

            when (toState(game.gameState)) {
                GameState.Completed -> throw Exception("Unable to pause completed game")
                GameState.NotStarted -> throw Exception("Unable to pause unstarted game")
                GameState.Paused,
                GameState.Started -> {
                    gameRef.updateChildren(
                        mapOf(
                            FirebaseDatabaseFields.PLAY_TIME to duration,
                            FirebaseDatabaseFields.GAME_STATE to GameState.Paused.name
                        )
                    ).await()
                }
            }

            Response.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            Response.Error(e.message ?: "Unknown Error")
        }
    }

    override suspend fun resumeGame(gameId: String): Response<Unit> {
        return try {
            val firebaseDatabase = Firebase.database
            val gameRef = firebaseDatabase
                .getReference(FirebaseCollections.GAMES)
                .child(gameId)

            val game = gameRef.get().await().getValue(GameDto::class.java)
                ?: throw Exception("Game is null")

            when (toState(game.gameState)) {
                GameState.Completed -> throw Exception("Unable to resume game because it was completed")
                GameState.Paused,
                GameState.NotStarted -> {
                    gameRef.updateChildren(
                        mapOf(
                            FirebaseDatabaseFields.GAME_STATE to GameState.Started.name
                        )
                    ).await()
                }

                GameState.Started -> {}
            }

            Response.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            Response.Error(e.message ?: "Unknown Error")
        }
    }

    override suspend fun resetGame(gameId: String): Response<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun endGame(
        currGameId: String,
        stageNumber: Int,
        duration: Long,
        winnerId: String,
        playerIds: List<String>
    ): Response<Unit> {
        return try {
            val gameCompletedTime = System.currentTimeMillis()

            val firebaseDatabase = Firebase.database
            val gameRef = firebaseDatabase
                .getReference(FirebaseCollections.GAMES)
                .child(currGameId)

            playerIds.forEach { playerId ->
                val userGameRef = firebaseDatabase
                    .getReference(FirebaseCollections.USER_GAMES)
                    .child(playerId)
                    .child(currGameId)

                userGameRef.updateChildren(
                    mapOf(
                        FirebaseDatabaseFields.COMPLETED to true
                    )
                ).await()
            }

            gameRef.updateChildren(
                mapOf(
                    FirebaseDatabaseFields.GAME_STATE to GameState.Completed.name,
                    FirebaseDatabaseFields.COMPLETED_TIME to gameCompletedTime,
                    FirebaseDatabaseFields.WINNER_ID to winnerId
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
                    name = user.name, email = user.email, photoUrl = user.photoUrl
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

        val ref = firebaseDatabase.getReference(FirebaseCollections.FRIENDS).child(user.id)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                println(snapshot)
                println(snapshot.children)
                val friends = snapshot.children.mapNotNull { childSnapshot ->
                    runCatching {
                        childSnapshot.getValue(FriendDto::class.java)
                    }.onFailure { error ->
                        Log.d(LogTags.CRUSADE_GAME_SERVICE_IMPL, "error: $error")
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

            val snapshot = users.orderByChild("email").equalTo(email.trim()).get().await()

            val friendId =
                snapshot.children.firstOrNull()?.key ?: return Response.Error("User not found")

            friends
                .child(user.id)
                .child(friendId)
                .setValue(
                    FriendDto(
                        userId = friendId, request = RequestDto(
                            accepted = false, send = true
                        )
                    )
                )

            friends
                .child(friendId)
                .child(user.id)
                .setValue(
                    FriendDto(
                        userId = user.id,
                        request = RequestDto(
                            accepted = false,
                            send = false
                        )
                    )
                )

            Response.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Response.Error(e.message ?: "Unknown Error")
        }
    }

    private fun getUserGames(
        completed: Boolean
    ): Flow<List<UserGame>> {
        return googleAuthUiClient.signedInUser.flatMapLatest { user ->
            if (user == null) {
                flowOf(emptyList())
            } else {
                callbackFlow {
                    val ref = Firebase.database.getReference(FirebaseCollections.USER_GAMES)
                        .child(user.id)

                    val listener = object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Log.d(
                                LogTags.CRUSADE_GAME_SERVICE_IMPL,
                                "getUserGames changed: $snapshot"
                            )

                            val userGameDtos = snapshot.children.mapNotNull { childSnapshot ->
                                val gameId = childSnapshot.key ?: return@mapNotNull null
                                runCatching {
                                    childSnapshot.getValue(UserGameDto::class.java)
                                        ?.toUserGame(gameId = gameId)
                                }.onFailure { error ->
                                    Log.d(
                                        LogTags.CRUSADE_GAME_SERVICE_IMPL,
                                        "Error parsing game $gameId: $error"
                                    )
                                }.getOrNull()
                            }.filter { it.completed == completed }

                            trySend(userGameDtos).onFailure { error ->
                                Log.w(
                                    LogTags.CRUSADE_GAME_SERVICE_IMPL,
                                    "Failed to send user games update to flow",
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
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getCompletedGames(): Flow<List<CompletedGame>> {
        val firebaseDatabase = Firebase.database
        val gamesRef = firebaseDatabase.getReference(FirebaseCollections.GAMES)
        val usersRef = firebaseDatabase.getReference(FirebaseCollections.USERS)
        return getUserGames(
            completed = true
        ).mapLatest { userGameDtos ->
            userGameDtos.mapNotNull { userGame ->
                val gameId = userGame.gameId
                val gameDto = gamesRef.child(gameId).get().await().getValue(GameDto::class.java)
                    ?: return@mapNotNull null
                val game = gameDto.toGame(gameId = gameId)
                val players = gameDto.playerIds.mapNotNull { playerIdAndPositionDto ->
                    val userSnapshot = usersRef.child(playerIdAndPositionDto.playerId).get().await()
                    val userId = userSnapshot.key ?: return@mapNotNull null

                    val user = userSnapshot.getValue(UserDto::class.java) ?: return@mapNotNull null

                    Player(
                        uniqueId = userId,
                        email = user.email,
                        phone = "",
                        imageUrl = user.photoUrl ?: "",
                        profileName = user.name ?: ""
                    )
                }
                CompletedGame(
                    duration = game.gameDuration, game = game, players = players
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getUnCompletedGames(): Flow<List<OnGoingGame>> {
        val gamesRef = Firebase.database.getReference(FirebaseCollections.GAMES)
        return getUserGames(completed = false).mapLatest { userGameDtos ->
            coroutineScope {
                userGameDtos.map { userGame ->
                    async {
                        runCatching {
                            val gameId = userGame.gameId
                            val snapshot = gamesRef.child(gameId).get().await()
                            val gameDto =
                                snapshot.getValue(GameDto::class.java) ?: return@runCatching null

                            val game = gameDto.toGame(gameId = gameId)
                            val players = getPlayers(
                                playerIds = gameDto.playerIds.map { it.playerId }
                            )

                            OnGoingGame(
                                threeCards = emptyList(),
                                gameWithPlayers = GameWithPlayers(
                                    game = game,
                                    players = players
                                )
                            )
                        }.getOrNull()
                    }
                }.awaitAll().filterNotNull()
            }
        }
    }
}