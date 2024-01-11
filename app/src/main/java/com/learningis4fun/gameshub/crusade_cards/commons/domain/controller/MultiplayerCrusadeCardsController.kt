package com.learningis4fun.gameshub.crusade_cards.commons.domain.controller

import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.DefaultCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.CardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.UserRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.CardStates
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.CardStatesDefaultMsg
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.crusade_cards.feature_play_crusade_cards.domain.mappers.toGameCard
import com.learningis4fun.gameshub.crusade_cards.feature_play_crusade_cards.domain.model.UserCard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
class MultiplayerCrusadeCardsController @Inject constructor(
    private val gameRepository: GameRepository,
    private val cardRepository: CardRepository,
    private val userRepository: UserRepository,
    private val gameCardRepository: GameCardRepository,
    private val repository: DefaultCardRepository
) {

    suspend fun getCurrentGameId(): String = gameRepository.getCurrentGameId().first()

    suspend fun getCurrentCardName(): String = cardRepository.getCurrCardName()

    suspend fun getCurrentCardType(): String = cardRepository.getCurrentCardType()

    /**
     * No tests present
     */
    suspend fun requestUserPlay(
        card: UserCard,
        canPick: Boolean
    ): CardStates {
        val gameId = getCurrentGameId()

        val currentCardName = getCurrentCardName()
        val currentCardType = getCurrentCardType()

        return requestPlay(
            playerId = card.playerId,
            gameId = gameId,
            currentCardType = currentCardType,
            currentCardName = currentCardName,
            userCard = card,
            canPick = canPick
        )
    }

    suspend fun requestPlayerChangeType(
        playerId: String,
        type: String
    ): CardStates {

        val result = allowChangeType(playerId = playerId, type = type)

        return when (result) {
            is Response.Error -> CardStates.Error(message = result.message)
            is Response.Success -> CardStates.ChangedType(
                playerId = playerId
            )
        }
    }

    suspend fun requestPlayerPick(
        playerId: String,
        cardsToPick: Int,
        canPlay: Boolean
    ): CardStates {
        val gameId = getCurrentGameId()
        return pickCardsFromLibrary(
            playerId = playerId,
            gameId = gameId,
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

        if (playerId.isEmpty()) {
            return CardStates.Error("Make sure, the playerId, gameId or card is not empty")
        }

        if (gameId.isEmpty()) {
            return CardStates.Error("Make sure, the gameId is not empty")
        }

        if (userCard == UserCard()) {
            return CardStates.Error("Make sure, the card is not empty")
        }

        return when {
            /**
             * if current card type is empty -> play card
             * if current card name & card type are empty -> play card
             */
            currentCardType.isEmpty() || (currentCardName.isEmpty() && currentCardType.isEmpty()) -> {
                playCard(
                    userCard = userCard
                )
            }
            // else -> check if can play
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
                    // same user requested to play with error message of previous card
                    if(playerId == userRepository.getAndroidUserId()){
                        if(canPick){
                            CardStates.Pick(
                                playerId = playerId
                            )
                        } else {
                            CardStates.NoAvailableCard(
                                playerId = playerId
                            )
                        }
                    } else {
                        CardStates.Play(
                            playerId = playerId,
                            canPick = canPick,
                            message = CardStatesDefaultMsg.PlayError
                        )
                    }
                }
            }
        }
    }

    suspend fun requestHitOfAlreadyPlayedCard(
        userCardName: String,
        playerId: String
    ): CardStates {
        return allowHit(playerId = playerId, prevCardsToPick = 0, cardName = userCardName)
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
            playerId = userCard.playerId,
            prevCardsToPick = cardsToPick,
            cardName = userCard.name
        )

        return when (allowHit) {
            is CardStates.Hit -> {

                return when (val updateResult =
                    gameCardRepository.playGameCard(userCard.toGameCard())) {
                    is Response.Success -> {
                        val newCardType = if (userCard.name == Constants.CardNames.JOKER) ""
                        else userCard.type

                        val response = repository.changeCurrentCardType(newCardType)

                        when (response) {
                            is Response.Error -> {
                                CardStates.Error("${response.message} while hitting back")
                            }
                            is Response.Success -> {
                                allowHit
                            }
                        }
                    }
                    is Response.Error -> {
                        CardStates.Error(updateResult.message)
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

    fun howManyUsersToSkip(players: List<String>): Int {
        val size = players.size
        return when (size) {
            in 3..5 -> 2
            in 6..7 -> 3
            in 8..10 -> 4
            else -> 1
        }
    }

    suspend fun playerIds(gameId: String): List<String> {
        return gameRepository.getGameWithPlayers(gameId = gameId)
            .first().players.map { it.uniqueId }
    }

    @ExperimentalStdlibApi
    suspend fun findNextPlayerToPlayAboveSeven(currPlayerId: String, gameId: String): String? {
        val players = playerIds(gameId = gameId)

        return if (players.size == 2) {
            currPlayerId
        } else {
            val howManyToSkip = howManyUsersToSkip(players = players)
            val indexOfCurrPlayer = players.indexOf(currPlayerId)

            if (indexOfCurrPlayer + howManyToSkip > players.lastIndex) {
                null
            } else {
                players[indexOfCurrPlayer + howManyToSkip]
            }
        }
        return currPlayerId
    }

    suspend fun findPreviousPlayerToPlayAboveJack(currPlayerId: String, gameId: String): String? {
        return try {
            val players = playerIds(gameId = gameId)

            if (players.size == 2) {
                currPlayerId
            } else {
                // 1, 2, 3, 4, 5, 6, 7: get the prev player is and rotate the play direction
                val indexOfCurrPlayer = players.indexOf(currPlayerId)

                if (indexOfCurrPlayer <= players.lastIndex && indexOfCurrPlayer > 0) {
                    players[indexOfCurrPlayer - 1]
                } else {
                    players[players.lastIndex]
                }
            }

        } catch (exception: Exception) {
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
                    playerId = userCard.playerId,
                    prevCardsToPick = prevCardsToPick,
                    cardName = userCard.name
                )

                when (allowHit) {
                    is CardStates.Hit -> {
                        val updateResult = gameCardRepository.playGameCard(
                            gameCard = userCard.toGameCard().copy(isPlayed = true)
                        )
                        when (updateResult) {
                            is Response.Error -> {
                                CardStates.Error("${updateResult.message}, while hitting...")
                            }
                            is Response.Success -> {

                                val response = repository.changeCurrentCardType(userCard.type)

                                when (response) {
                                    is Response.Error -> {
                                        CardStates.Error("${response.message}, while hitting...")
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
                val updateResult = gameCardRepository.playGameCard(
                    gameCard = userCard.toGameCard().copy(isPlayed = true)
                )

                when (updateResult) {
                    is Response.Success -> {
                        CardStates.ChangeType(
                            playerId = userCard.playerId
                        )
                    }
                    is Response.Error -> {
                        CardStates.Error("${updateResult.message} while playing eight")
                    }
                }
            }
            Constants.CardNames.JOKER -> {
                // can hit or change type
                // ask user define type
                val updateResult = gameCardRepository.playGameCard(
                    gameCard = userCard.toGameCard().copy(isPlayed = true)
                )
                when (updateResult) {
                    is Response.Success -> {

                        val response = repository.changeCurrentCardType("")

                        when (response) {
                            is Response.Error -> {

                                CardStates.Error("${response.message} while asked to define card action...")
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
                        CardStates.Error("${updateResult.message} while asked to define card action...")
                    }
                }
            }
            Constants.CardNames.SEVEN -> {
                val updateResult = gameCardRepository.playGameCard(
                    gameCard = userCard.toGameCard().copy(isPlayed = true)
                )
                when (updateResult) {
                    is Response.Success -> {

                        val response = repository.changeCurrentCardType(userCard.type)

                        when (response) {
                            is Response.Error -> {
                                CardStates.Error("${response.message} while being given a chance to play another card...")
                            }
                            is Response.Success -> {
                                val nextPlayer = findNextPlayerToPlayAboveSeven(
                                        currPlayerId = userCard.playerId,
                                        gameId = userCard.gameId
                                    ) ?: return CardStates.Error(
                                        message = "Couldn't find the other player that should play after playing Seven")

                                if(userCard.playerId != nextPlayer){
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
                        CardStates.Error("${updateResult.message} while being given a chance to play another card...")
                    }
                }
            }
            Constants.CardNames.JACK -> {
                val updateResult = gameCardRepository.playGameCard(
                    gameCard = userCard.toGameCard().copy(isPlayed = true)
                )
                when (updateResult) {
                    is Response.Success -> {

                        val response = repository.changeCurrentCardType(userCard.type)

                        when (response) {
                            is Response.Error -> {
                                CardStates.Error("${response.message} while being given a chance to play another card...")
                            }
                            is Response.Success -> {
                                val prevPlayer = findPreviousPlayerToPlayAboveJack(
                                        currPlayerId = userCard.playerId,
                                        gameId = userCard.gameId
                                    ) ?: return CardStates.Error(
                                    message = "Couldn't find the other player that should play after playing Jack")

                                if(userCard.playerId != prevPlayer){
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
                        CardStates.Error("${updateResult.message} while being given a chance to play another card...")
                    }
                }
            }
            else -> {
                val updateResult = gameCardRepository.playGameCard(
                    gameCard = userCard.toGameCard().copy(isPlayed = true)
                )
                when (updateResult) {

                    is Response.Success -> {

                        val response = repository.changeCurrentCardType(userCard.type)

                        when (response) {
                            is Response.Error -> {
                                CardStates.Error("${response.message} Error while playing a card...")
                            }
                            is Response.Success -> {
                                CardStates.Played(
                                    playerId = userCard.playerId
                                )
                            }
                        }
                    }
                    is Response.Error -> CardStates.Error("Error while playing a card...")
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
        numOfCardsToPick: Int,
        canPlay: Boolean = true
    ): CardStates {

        if (playerId.isEmpty()) {
            return CardStates.Error("Error while picking cards, make sure the playerId is not empty")
        }

        if (numOfCardsToPick == 0) {
            return CardStates.Error("Error while picking cards, cannot pick 0 cards")
        }

        if (gameId.isEmpty()) {
            return CardStates.Error("Error while picking cards, gameId is must not be empty")
        }

        val pickedCardsResponse = gameCardRepository.pickCardsFromLibrary(
            playerId = playerId,
            gameId = gameId,
            numOfCardsToPick = numOfCardsToPick
        )

        return when (pickedCardsResponse) {
            is Response.Success -> {
                CardStates.Picked(
                    playerId = playerId,
                    canPlay = canPlay,
                    cardsPicked = pickedCardsResponse.data
                )
            }
            is Response.Error -> {
                CardStates.Error(pickedCardsResponse.message)
            }
        }

    }

    suspend fun allowChangeType(
        playerId: String,
        type: String
    ): Response<String> {

        if (type.isEmpty()) {
            return Response.Error(
                type,
                "The correct type should be set, was unable to change card type"
            )
        }

        if (playerId.isEmpty()) {
            return Response.Error(type, "The player should not be empty")
        }

        return repository.changeCurrentCardType(type = type)
    }

    /**
     * Card already played, update the cards other user should pick
     * and provide the player id of the other user
     */
    suspend fun allowHit(
        playerId: String,
        prevCardsToPick: Int = 0,
        cardName: String,
    ): CardStates {

        if (cardName.isEmpty()) {
            return CardStates.Error("Error while hitting, card name should not empty")
        }

        if (playerId.isEmpty()) {
            return CardStates.Error("Error while hitting, playerId should not empty")
        }

        if (cardName != Constants.CardNames.TWO && cardName != Constants.CardNames.JOKER) {
            return CardStates.Error(
                message = "Error while hitting, card name should be two or joker"
            )
        }

        val idOfOtherPlayer = getIdOfTheNextPlayerToHit(
            playerRequestingToHit = playerId,
            gameId = getCurrentGameId()
        )

        if (idOfOtherPlayer.isNullOrEmpty()) {
            return CardStates.Error("Error while hitting, couldn't find the other player to hit")
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

    suspend fun getIdOfTheNextPlayerToHit(playerRequestingToHit: String, gameId: String): String? {
        val players = playerIds(gameId = gameId)

        if (players.size == 2) {
            return players.first { it != playerRequestingToHit }
        }

        val indexOfPlayerToHit = players.indexOf(playerRequestingToHit)

        if (indexOfPlayerToHit == -1) {
            return null
        }

        return when {
            indexOfPlayerToHit == players.lastIndex -> {
                players.first()
            }
            indexOfPlayerToHit < players.lastIndex -> {
                players[indexOfPlayerToHit + 1]
            }
            else -> {
                null
            }
        }
    }


}