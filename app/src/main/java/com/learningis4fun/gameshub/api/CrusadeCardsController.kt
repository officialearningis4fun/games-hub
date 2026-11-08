package com.learningis4fun.gameshub.api

import android.util.Log
import com.learningis4fun.gameshub.commons.domain.util.CardStates
import com.learningis4fun.gameshub.commons.domain.util.CardStatesDefaultMsg
import com.learningis4fun.gameshub.commons.domain.util.Constants
import com.learningis4fun.gameshub.commons.domain.util.Response
import com.learningis4fun.gameshub.domain.model.UserCard
import com.learningis4fun.gameshub.util.GamesHubLogTags
import com.learningis4fun.gameshub.util.LogTags
import com.learningis4fun.gameshub.util.getDataOrThrowException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.abs


@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
class CrusadeCardsController @Inject constructor(
    private val crusadeGameService: CrusadeGameService,
) {

    /**
     * No tests present
     */
    suspend fun requestUserPlay(
        card: UserCard,
        canPick: Boolean,
        currentCardName: String,
        currentCardType: String
    ): CardStates {

        return requestPlay(
            playerId = card.playerId,
            gameId = card.gameId,
            currentCardType = currentCardType,
            currentCardName = currentCardName,
            userCard = card,
            canPick = canPick
        )
    }

    suspend fun requestPlayerChangeType(
        playerId: String,
        type: String,
        gameId: String
    ): CardStates {

        Log.d(GamesHubLogTags.ONLINE_CRUSADE_CARDS_CONTROLLER, "playerId: $playerId type: $type gameId: $gameId")

        return when (val result = allowChangeType(
            playerId = playerId,
            gameId = gameId,
            type = type
        )) {
            is Response.Error -> CardStates.Error(
                playerId = playerId,
                message = result.message.toString()
            )

            is Response.Success -> CardStates.ChangedType(
                playerId = playerId
            )
        }
    }

    suspend fun requestPlayerPick(
        gameId: String,
        playerId: String,
        cardsToPick: Int,
        stageNumber: Int,
        canPlay: Boolean
    ): CardStates {
        return pickCardsFromLibrary(
            playerId = playerId,
            gameId = gameId,
            stageNumber = stageNumber,
            numOfCardsToPick = cardsToPick,
            canPlay = canPlay
        )
    }

    suspend fun requestPlayerHitBack(
        playerId: String,
        cardsToPick: Int,
        userCard: UserCard
    ): CardStates {
        return requestHitBackOrPick(
            playerId = playerId,
            userCard = userCard,
            cardsToPick = cardsToPick
        )
    }

    suspend fun requestPlayerHit(
        playerId: String,
        userCard: UserCard
    ): CardStates {
        return requestHitOfAlreadyPlayedCard(
            gameId = userCard.gameId,
            playerId = playerId,
            userCardName = userCard.name
        )
    }

    suspend fun requestPlay(
        playerId: String,
        gameId: String,
        currentCardType: String,
        currentCardName: String,
        userCard: UserCard,
        canPick: Boolean
    ): CardStates {

        Log.d(GamesHubLogTags.ONLINE_CRUSADE_CARDS_CONTROLLER, "gameId: $gameId playerId: $playerId userCard: ${userCard.card} currentCardType: $currentCardType currentCardName: $currentCardName canPick: $canPick currentCardType: $currentCardType")

        if (playerId.isEmpty()) {
            return CardStates.Error(
                playerId = playerId,
                message = "Make sure, the playerId, gameId or card is not empty"
            )
        }

        if (gameId.isEmpty()) {
            return CardStates.Error(
                playerId = playerId,
                message = "Make sure, the gameId is not empty"
            )
        }

        if (userCard == UserCard()) {
            return CardStates.Error(
                playerId = playerId,
                message = "Make sure, the card is not empty"
            )
        }

        return when {
            /**
             * if current card type is empty -> play card
             * if current card name & card type are empty -> play card
             */
            currentCardType.isEmpty() || (currentCardName.isEmpty() && currentCardType.isEmpty()) -> {
                Log.d(
                    GamesHubLogTags.ONLINE_CRUSADE_CARDS_CONTROLLER,
                    "Can play; cardType:${currentCardType}-cardName:${currentCardName}"
                )
                playCard(
                    userCard = userCard
                )
            }
            // else -> check if you can play
            else -> {

                if (shouldCardBePlayed(
                        cardName = userCard.name,
                        cardType = userCard.type,
                        currentCardName = currentCardName,
                        currentCardType = currentCardType
                    )
                ) {
                    playCard(
                        userCard = userCard
                    )
                } else {
                    // the card should not be played...
                    Log.d(GamesHubLogTags.ONLINE_CRUSADE_CARDS_CONTROLLER, "same user requested to play with error message of previous card")
                    // same user requested to play with error message of previous card
                    CardStates.Play(
                        playerId = playerId,
                        canPick = canPick,
                        message = CardStatesDefaultMsg.PlayError
                    )

                }
            }
        }
    }

    private suspend fun requestHitOfAlreadyPlayedCard(
        gameId: String,
        userCardName: String,
        playerId: String
    ): CardStates {
        return allowHit(
            gameId = gameId,
            playerId = playerId,
            prevCardsToPick = 0,
            cardName = userCardName
        )
    }

    /**
     * if card is null, return not hit
     * if card is not 2, return error
     * if card is 2 or joker, return hit
     */
    suspend fun requestHitBackOrPick(
        userCard: UserCard,
        playerId: String?,
        cardsToPick: Int
    ): CardStates {

        val id = playerId ?: userCard.playerId

        val allowHit = allowHit(
            gameId = userCard.gameId,
            playerId = userCard.playerId,
            prevCardsToPick = cardsToPick,
            cardName = userCard.name
        )

        return when (allowHit) {
            is CardStates.Hit -> {

                when (val updateResult =
                    crusadeGameService.playCard(userCard)) {
                    is Response.Success -> {
                        val newCardType = if (userCard.name == Constants.CardNames.JOKER) ""
                        else userCard.type

                        when (val response = crusadeGameService.changeCardType(
                            playerId = userCard.playerId,
                            gameId = userCard.gameId,
                            type = newCardType
                        )) {
                            is Response.Error -> {
                                CardStates.Error(
                                    playerId = id,
                                    message = "${response.message} while hitting back"
                                )
                            }

                            is Response.Success -> {
                                allowHit
                            }
                        }
                    }

                    is Response.Error -> {
                        CardStates.Error(playerId = id, message = updateResult.message.toString())
                    }
                }

            }

            else -> {
                // show prev state
                CardStates.Hit(
                    playerId = id,
                    cardsToPick = cardsToPick,
                    message = CardStatesDefaultMsg.HitError
                )
            }
        }
    }

    private fun howManyUsersToSkip(players: List<String>): Int {
        val size = players.size
        return when (size) {
            in 4..5 -> 2
            in 6..7 -> 3
            in 8..10 -> 4
            else -> 1
        }
    }

    suspend fun playerIds(gameId: String): List<String> {
        return crusadeGameService.getLatestStagePlayerIdsSorted(gameId = gameId)
            .getDataOrThrowException()
    }

    @ExperimentalStdlibApi
    suspend fun findNextPlayerToPlayAboveSeven(currPlayerId: String, gameId: String): String {
        val players = playerIds(gameId = gameId)
        Log.d(
            GamesHubLogTags.ONLINE_CRUSADE_CARDS_CONTROLLER,
            "seven played: players: $players"
        )

        return if (players.size == 2) {
            currPlayerId
        } else {
            val howManyToSkip = howManyUsersToSkip(players = players)
            val indexOfCurrPlayer = players.indexOf(currPlayerId)

            val pos = players.lastIndex - (indexOfCurrPlayer + howManyToSkip + 1)

            val indexOfId = if (pos < 0) {
                abs(pos) - 1
            } else {
                players.lastIndex - pos
            }

            players[indexOfId]
        }
    }

    suspend fun findPreviousPlayerToPlayAboveJackAndChangePlayingDirection(
        currPlayerId: String,
        gameId: String
    ): String? {
        return try {

            // switch the direction of play
            val game = crusadeGameService.getGame(gameId).getDataOrThrowException()

            val playersBeforeGameUpdate = playerIds(gameId)
                .mapIndexed { index, s -> Pair(s, index + 1) }

            crusadeGameService.updateGamePlayingDirection(
                gameId = gameId,
                direction = when (game.playingDirection) {
                    Constants.CrusadePlayingDirection.CLOCKWISE -> Constants.CrusadePlayingDirection.ANTICLOCKWISE
                    Constants.CrusadePlayingDirection.ANTICLOCKWISE -> Constants.CrusadePlayingDirection.CLOCKWISE
                    else -> game.playingDirection
                }
            ).getDataOrThrowException()

            // have other players not sho up in another stage
            // some time all the players show up while other time they don't
            val players = playersBeforeGameUpdate
                .sortedByDescending { it.second }
                .map { it.first }

            val wrongPlayers = playerIds(gameId)

            Log.d(GamesHubLogTags.ONLINE_CRUSADE_CARDS_CONTROLLER, "players $players")
            Log.d(
                GamesHubLogTags.ONLINE_CRUSADE_CARDS_CONTROLLER,
                "wrong players $wrongPlayers"
            )
            Log.d(
                GamesHubLogTags.ONLINE_CRUSADE_CARDS_CONTROLLER,
                "players before gameUpdate $playersBeforeGameUpdate"
            )

            val indexOfCurrPlayer = players.indexOf(currPlayerId)

            val nextPlayerId = if (players.size == 2) {
                currPlayerId
            } else {
                // 1, 2, 3, 4, 5, 6, 7: get the prev player is and rotate the play direction
                // 7, 6, 5, 4, 3, 2, 1: get the prev player is and rotate the play direction

                if (indexOfCurrPlayer == players.lastIndex) {
                    players[0]
                } else {
                    players[indexOfCurrPlayer + 1]
                }
            }
            nextPlayerId
        } catch (exception: Exception) {
            if (exception is CancellationException) throw exception
            exception.printStackTrace()

            null
        }
    }

    /**
     * if user's card name is two -> user hit or hit-back
     * if user's card name is eight -> user can change card
     * if user's card name is joker -> user can change card or hit or hit-back
     * if user's card name is seven -> user should skip or have chance to play again
     * if user's card name is seven -> user should skip/reverse play direction or have chance to play again
     */
    suspend fun playCard(
        userCard: UserCard,
        prevCardsToPick: Int = 0
    ): CardStates {
        return when (userCard.name) {
            Constants.CardNames.TWO -> {

                val allowHit = allowHit(
                    gameId = userCard.gameId,
                    playerId = userCard.playerId,
                    prevCardsToPick = prevCardsToPick,
                    cardName = userCard.name
                )

                when (allowHit) {
                    is CardStates.Hit -> {
                        val updateResult = crusadeGameService.playCard(
                            userCard = userCard.copy(isPlayed = true)
                        )
                        when (updateResult) {
                            is Response.Error -> {
                                CardStates.Error(
                                    playerId = userCard.playerId,
                                    message = "${updateResult.message}, while hitting..."
                                )
                            }

                            is Response.Success -> {

                                val response = crusadeGameService.changeCardType(
                                    playerId = userCard.playerId,
                                    gameId = userCard.gameId,
                                    type = userCard.type
                                )

                                when (response) {
                                    is Response.Error -> {
                                        CardStates.Error(
                                            playerId = userCard.playerId,
                                            message = "${response.message}, while hitting..."
                                        )
                                    }

                                    is Response.Success -> {
                                        allowHit
                                    }
                                }
                            }
                        }

                    }

                    else -> allowHit
                }

            }

            Constants.CardNames.EIGHT -> {
                // play and prompt user define type
                val updateResult = crusadeGameService.playCard(
                    userCard = userCard.copy(isPlayed = true)
                )

                when (updateResult) {
                    is Response.Success -> {
                        CardStates.ChangeType(
                            playerId = userCard.playerId
                        )
                    }

                    is Response.Error -> {
                        CardStates.Error(
                            playerId = userCard.playerId,
                            message = "${updateResult.message} while playing eight"
                        )
                    }
                }
            }

            Constants.CardNames.JOKER -> {
                // can hit or change type
                // ask user define type
                val updateResult = crusadeGameService.playCard(
                    userCard = userCard.copy(isPlayed = true)
                )
                when (updateResult) {
                    is Response.Success -> {

                        when (val response = crusadeGameService.changeCardType(
                            playerId = userCard.playerId,
                            gameId = userCard.gameId,
                            type = ""
                        )) {
                            is Response.Error -> {

                                CardStates.Error(
                                    playerId = userCard.playerId,
                                    message = "${response.message} while asked to define card action..."
                                )
                            }

                            is Response.Success -> {
                                CardStates.DefineCardAction(
                                    playerId = userCard.playerId,
                                    gameCardId = userCard.gameCardId
                                )
                            }
                        }
                    }

                    is Response.Error -> {
                        CardStates.Error(
                            playerId = userCard.playerId,
                            message = "${updateResult.message} while asked to define card action..."
                        )
                    }
                }
            }

            Constants.CardNames.SEVEN -> {
                val updateResult = crusadeGameService.playCard(
                    userCard = userCard.copy(isPlayed = true)
                )
                when (updateResult) {
                    is Response.Success -> {

                        when (val response = crusadeGameService.changeCardType(
                            playerId = userCard.playerId,
                            gameId = userCard.gameId,
                            type = userCard.type
                        )) {
                            is Response.Error -> {
                                CardStates.Error(
                                    playerId = userCard.playerId,
                                    message = "${response.message} while being given a chance to play another card..."
                                )
                            }

                            is Response.Success -> {
                                val nextPlayer = findNextPlayerToPlayAboveSeven(
                                    currPlayerId = userCard.playerId,
                                    gameId = userCard.gameId
                                )

                                if (userCard.playerId != nextPlayer) {
                                    CardStates.Play(
                                        playerId = nextPlayer
                                    )
                                } else {
                                    CardStates.Chance(
                                        playerId = nextPlayer
                                    )
                                }
                            }
                        }
                    }

                    is Response.Error -> {
                        CardStates.Error(
                            playerId = userCard.playerId,
                            message = "${updateResult.message} while being given a chance to play another card..."
                        )
                    }
                }
            }

            Constants.CardNames.JACK -> {
                Log.d(GamesHubLogTags.ONLINE_CRUSADE_CARDS_CONTROLLER, "Playing: $userCard")
                val updateResult = crusadeGameService.playCard(
                    userCard = userCard.copy(isPlayed = true)
                )
                when (updateResult) {
                    is Response.Success -> {

                        when (val response = crusadeGameService.changeCardType(
                            playerId = userCard.playerId,
                            gameId = userCard.gameId,
                            type = userCard.type
                        )) {
                            is Response.Error -> {
                                CardStates.Error(
                                    playerId = userCard.playerId,
                                    message = "${response.message} while being given a chance to play another card..."
                                )
                            }

                            is Response.Success -> {
                                Log.d(
                                    GamesHubLogTags.ONLINE_CRUSADE_CARDS_CONTROLLER,
                                    "Successfully Played card$"
                                )
                                val prevPlayer =
                                    findPreviousPlayerToPlayAboveJackAndChangePlayingDirection(
                                        currPlayerId = userCard.playerId,
                                        gameId = userCard.gameId
                                    ) ?: return CardStates.Error(
                                        playerId = userCard.playerId,
                                        message = "Couldn't find the other player that should play after playing Jack"
                                    )
                                Log.d(
                                    GamesHubLogTags.ONLINE_CRUSADE_CARDS_CONTROLLER,
                                    "next playerId: $prevPlayer"
                                )

                                if (userCard.playerId != prevPlayer) {
                                    CardStates.Play(
                                        playerId = prevPlayer
                                    )
                                } else {
                                    CardStates.Chance(
                                        playerId = prevPlayer
                                    )
                                }
                            }
                        }
                    }

                    is Response.Error -> {
                        CardStates.Error(
                            playerId = userCard.playerId,
                            message = "${updateResult.message} while being given a chance to play another card..."
                        )
                    }
                }
            }

            else -> {
                val updateResult = crusadeGameService.playCard(
                    userCard = userCard.copy(isPlayed = true)
                )
                when (updateResult) {

                    is Response.Success -> {

                        when (val response = crusadeGameService.changeCardType(
                            playerId = userCard.playerId,
                            gameId = userCard.gameId,
                            type = userCard.type
                        )) {
                            is Response.Error -> {
                                CardStates.Error(
                                    playerId = userCard.playerId,
                                    message = "${response.message} Error while playing a card..."
                                )
                            }

                            is Response.Success -> {
                                CardStates.Played(
                                    playerId = userCard.playerId
                                )
                            }
                        }
                    }

                    is Response.Error -> CardStates.Error(
                        playerId = userCard.playerId,
                        message = "Error while playing a card..."
                    )
                }
            }
        }
    }

    /**
     * if cardName is eight or joker -> true
     * if cardName is same as currentName -> true
     * if cardType is same as currentType -> true
     * if cardType is different to currentType -> false
     */
    fun shouldCardBePlayed(
        cardName: String,
        cardType: String,
        currentCardName: String,
        currentCardType: String
    ): Boolean {
        Log.d(GamesHubLogTags.ONLINE_CRUSADE_CARDS_CONTROLLER, "cardName: $cardName currentCardName: $currentCardName cardType: $cardType currentCardType: $currentCardType")
        if (cardName == Constants.CardNames.EIGHT || cardName == Constants.CardNames.JOKER) {
            return true
        }
        if (cardName == currentCardName) {
            return true
        }
        if (cardType == currentCardType) {
            return true
        }
        return false
    }

    /**
     * empty playerId or gameId or num of cards to pick is 0 -> Error
     * valid inputs -> Picked
     * if picked specified cards -> Picked a specified num
     *
     */
    suspend fun pickCardsFromLibrary(
        playerId: String,
        gameId: String,
        stageNumber: Int,
        numOfCardsToPick: Int,
        canPlay: Boolean = true
    ): CardStates {

        if (playerId.isEmpty()) {
            return CardStates.Error(
                playerId = playerId,
                message = "Error while picking cards, make sure the playerId is not empty"
            )
        }

        if (numOfCardsToPick == 0) {
            return CardStates.Error(
                playerId = playerId,
                message = "Error while picking cards, cannot pick 0 cards"
            )
        }

        if (gameId.isEmpty()) {
            return CardStates.Error(
                playerId = playerId,
                message = "Error while picking cards, gameId is must not be empty"
            )
        }

        val pickedCardsResponse = crusadeGameService.pickCards(
            playerId = playerId,
            gameId = gameId,
            stageNumber = stageNumber,
            numOfCardsToPick = numOfCardsToPick
        )

        return when (pickedCardsResponse) {
            is Response.Success -> {
                CardStates.Picked(
                    playerId = playerId,
                    canPlay = canPlay,
                    cardsPicked = pickedCardsResponse.data ?: 0
                )
            }

            is Response.Error -> {
                CardStates.Error(
                    playerId = playerId,
                    message = pickedCardsResponse.message.toString()
                )
            }
        }

    }

    suspend fun allowChangeType(
        playerId: String,
        type: String,
        gameId: String
    ): Response<Unit> {

        if (type.isEmpty()) {
            return Response.Error(
                "The correct type should be set, was unable to change card type"
            )
        }

        if (playerId.isEmpty()) {
            return Response.Error("The player should not be empty")
        }

        return crusadeGameService.changeCardType(
            playerId = playerId,
            gameId = gameId,
            type = type
        )
    }

    /**
     * Card already played, update the cards other user should pick
     * and provide the player id of the other user
     */
    suspend fun allowHit(
        gameId: String,
        playerId: String,
        prevCardsToPick: Int = 0,
        cardName: String,
    ): CardStates {

        if (cardName.isEmpty()) {
            return CardStates.Error(
                playerId = playerId,
                message = "Error while hitting, card name should not be empty"
            )
        }

        if (playerId.isEmpty()) {
            return CardStates.Error(
                playerId = playerId,
                message = "Error while hitting, playerId should not be empty"
            )
        }

        if (cardName != Constants.CardNames.TWO && cardName != Constants.CardNames.JOKER) {
            return CardStates.Error(
                playerId = playerId,
                message = "Error while hitting, card name should be two or joker"
            )
        }

        val idOfOtherPlayer = getIdOfTheNextPlayer(
            currentPlayer = playerId,
            gameId = gameId
        )

        if (idOfOtherPlayer.isEmpty()) {
            return CardStates.Error(
                playerId = playerId,
                message = "Error while hitting, couldn't find the other player to hit"
            )
        }

        val totalCardsToPick = when (cardName) {
            Constants.CardNames.TWO -> {
                2 + prevCardsToPick
            }

            Constants.CardNames.JOKER -> {
                4 + prevCardsToPick
            }

            else -> {
                prevCardsToPick
            }
        }

        return CardStates.Hit(
            playerId = idOfOtherPlayer,
            cardsToPick = totalCardsToPick
        )
    }

    suspend fun getIdOfTheNextPlayer(currentPlayer: String, gameId: String): String {
        val players = playerIds(gameId = gameId)
        Log.d(
            GamesHubLogTags.ONLINE_CRUSADE_CARDS_CONTROLLER,
            "getIdOfNextPlayer: players: $players"
        )

        if (players.size == 2) {
            return players.first { it != currentPlayer }
        }

        val indexOfNextPlayer = players.indexOf(currentPlayer)

        return when {
            indexOfNextPlayer == players.lastIndex -> {
                players.first()
            }

            indexOfNextPlayer < players.lastIndex -> {
                players[indexOfNextPlayer + 1]
            }

            else -> {
                ""
            }
        }
    }

}