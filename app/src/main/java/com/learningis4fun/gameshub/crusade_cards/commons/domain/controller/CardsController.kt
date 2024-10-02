package com.learningis4fun.gameshub.crusade_cards.commons.domain.controller

import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.CardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.GameRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.UserRepository
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.UserCard
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.mappers.toGameCard
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.DefaultCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.CardStates
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.CardStatesDefaultMsg
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@ExperimentalCoroutinesApi
class CardsController @Inject constructor(
    private val repository: DefaultCardRepository,
    private val gameRepository: GameRepository,
    private val cardRepository: CardRepository,
    private val gameCardRepository: GameCardRepository,
    private val userRepository: UserRepository,
    private val crusadeComputerCardsController: CrusadeComputerCardsController
) {

    suspend fun getCurrentGameId(): String = gameRepository.getCurrentGameId().first()

    suspend fun getCurrentCardName(): String = cardRepository.getCurrCardName()

    suspend fun getCurrentCardType(): String = cardRepository.getCurrentCardType()

    suspend fun getUserId() = userRepository.getUserId()
    suspend fun getAndroidUserId() = userRepository.getAndroidUserId()

    /**
     * No tests present
     */
    suspend fun requestOtherUsersPlay(
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

    suspend fun requestComputerChangeType(
        playerId: String
    ): CardStates {

        val type = repository.cardTypeForComputerToChangeTo()

        val result = allowChangeType(playerId = playerId, type = type)

        return when (result) {
            is Response.Error -> CardStates.Error(message = result.message)
            is Response.Success -> CardStates.ChangedType(
                playerId = playerId,
                message = result.message
            )
        }
    }

    suspend fun requestOtherPlayersChangeType(
        playerId: String,
        type: String
    ): CardStates {

        val result = allowChangeType(playerId = playerId, type = type)

        return when (result) {
            is Response.Error -> CardStates.Error(message = result.message)
            is Response.Success -> CardStates.ChangedType(
                playerId = playerId,
                message = result.message
            )
        }
    }

    suspend fun requestComputerToPick(
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

    suspend fun requestOtherPlayersPick(
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

    suspend fun requestComputerHitBack(
        playerId: String,
        cardsToPick: Int
    ): CardStates {
        val currentGameId = getCurrentGameId()
        val card = repository.cardForComputerToHitBack(gameId = currentGameId, playerId = playerId)
        return requestHitBackOrPick(playerId = playerId, userCard = card, cardsToPick = cardsToPick)
    }

    suspend fun requestOtherPlayersHitBack(
        playerId: String,
        cardsToPick: Int,
        userCard: UserCard?
    ): CardStates {
        return requestHitBackOrPick(
            playerId = playerId,
            userCard = userCard,
            cardsToPick = cardsToPick
        )
    }

    suspend fun requestOtherPlayersHit(
        playerId: String,
        userCard: UserCard
    ): CardStates {
        return requestHitOfAlreadyPlayedCard(
            playerId = playerId,
            userCardName = userCard.name
        )
    }

    /**
     * if card for computer is null and canPick, request computer to pick
     * if card for computer is null and canPick is false, computer has noCardAvailable
     * else decision is made by requestPlay function
     */
    suspend fun requestComputerPlay(
        playerId: String,
        canPick: Boolean
    ): CardStates {

        val gameId = getCurrentGameId()

        val card = crusadeComputerCardsController.playCardForComputer(
            playerId = playerId
        )

        return if (card == null) {
            //pick a card
            if (canPick) {
                CardStates.Pick(
                    playerId = playerId,
                    numOfCardsToPick = 1,
                    canPlay = true
                )
            } else {
                CardStates.NoAvailableCard(
                    playerId = playerId
                )
            }

        } else {
            val currentCardName = getCurrentCardName()
            val currentCardType = getCurrentCardType()

            val cardState = requestPlay(
                playerId = playerId,
                gameId = gameId,
                currentCardType = currentCardType,
                currentCardName = currentCardName,
                userCard = card,
                canPick = canPick
            )

            if (cardState is CardStates.Error) {

                if (canPick) {
                    CardStates.Pick(
                        playerId = playerId,
                        numOfCardsToPick = 1,
                        canPlay = true
                    )
                } else {
                    CardStates.NoAvailableCard(
                        playerId = playerId
                    )
                }

            } else {
                cardState
            }
        }


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
                    // same user requested to play if not computer

                    if (playerId == getAndroidUserId()) {
                        if (canPick) {
                            CardStates.Pick(
                                playerId = playerId,
                                canPlay = true,
                                numOfCardsToPick = 1
                            )
                        } else {
                            CardStates.NoAvailableCard(
                                playerId = playerId
                            )
                        }

                    } else CardStates.Play(
                        playerId = playerId,
                        canPick = canPick,
                        message = CardStatesDefaultMsg.PlayError
                    )
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
        userCard: UserCard?,
        playerId: String?,
        cardsToPick: Int
    ): CardStates {

        val id = playerId ?: userCard?.playerId
        ?: return CardStates.Error("provide either user card or playerId")

        if (userCard == null) {
            return CardStates.Pick(
                playerId = id,
                canPlay = false,
                numOfCardsToPick = cardsToPick
            )
        }

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
                    playerId = playerId!!,
                    cardsToPick = cardsToPick
                )
            }
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
            Constants.CardNames.SEVEN, Constants.CardNames.JACK -> {
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
                                CardStates.Chance(
                                    playerId = userCard.playerId
                                )
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
            return CardStates.Error("Error while picking cards, gameId is not empty")
        }

        val pickedCardsResponse = repository.pickCardsFromLibrary(
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

        val userId = userRepository.getUserId()
        val androidUserId = userRepository.getAndroidUserId()

        if (playerId.isEmpty() || cardName.isEmpty()) {
            return CardStates.Error("Error while hitting, card name and playerId should not empty")
        }

        if (cardName != Constants.CardNames.TWO && cardName != Constants.CardNames.JOKER) {
            return CardStates.Error("Error while hitting, card name should be two or joker")
        }

        val idOfOtherPlayer = if (playerId == androidUserId) {
            userId
        } else {
           androidUserId
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


}