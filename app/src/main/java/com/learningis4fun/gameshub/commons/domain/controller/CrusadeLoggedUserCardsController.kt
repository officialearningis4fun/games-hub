package com.learningis4fun.gameshub.commons.domain.controller

import com.learningis4fun.gameshub.api.use_cases.HandleCardStateUseCase
import com.learningis4fun.gameshub.commons.domain.repository.GameCardRepository
import com.learningis4fun.gameshub.commons.domain.util.CardStates
import com.learningis4fun.gameshub.commons.domain.util.CardStatesDefaultMsg
import com.learningis4fun.gameshub.commons.domain.util.JokerActions
import com.learningis4fun.gameshub.commons.domain.util.Response
import com.learningis4fun.gameshub.domain.model.UserCard
import com.learningis4fun.gameshub.util.getDataOrThrowException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
class CrusadeLoggedUserCardsController @Inject constructor(
    private val handleCardStateUseCase: HandleCardStateUseCase,
    private val gameCardRepository: GameCardRepository,
    private val multiplayerCrusadeCardsController: MultiplayerCrusadeCardsController
) {

    suspend fun userRequestToAllowOthersPlay(playerId: String) {
        val cardState = CardStates.NoAvailableCard(
            playerId = playerId
        )
        handleCardStateUseCase.saveCardStateToPreferences(cardState)
    }

    suspend fun userRequestToDefineJokerAction(
        playerId: String,
        action: JokerActions,
        gameCardId: String
    ) {
        val cardState = when (action) {
            JokerActions.ChangeType -> {
                CardStates.ChangeType(playerId = playerId)
            }

            JokerActions.Hit -> {
                when (val userCardResponse = gameCardRepository.getGameCardWithCard(gameCardId)) {
                    is Response.Success -> {
                        multiplayerCrusadeCardsController.requestPlayerHit(
                            playerId = playerId,
                            userCard = userCardResponse.getDataOrThrowException()
                        )
                    }

                    is Response.Error -> {
                        CardStates.Error(
                            playerId = playerId,
                            message = "${userCardResponse.message}, gameCardId: $gameCardId"
                        )
                    }
                }

            }
        }
        handleCardStateUseCase.saveCardStateToPreferences(cardState)
    }

    suspend fun userRequestToPlay(
        userCard: UserCard,
        canPick: Boolean
    ): Response<Unit> {
        val cardState = multiplayerCrusadeCardsController.requestUserPlay(
            card = userCard,
            canPick = canPick
        )
        handleCardStateUseCase.saveCardStateToPreferences(cardState)

        return when (cardState) {
            is CardStates.Error -> {
                Response.Error(cardState.message)
            }

            is CardStates.Play -> {
                if (cardState.message == CardStatesDefaultMsg.PlayError) {
                    Response.Error(CardStatesDefaultMsg.PlayError)
                } else {
                    Response.Success(Unit)
                }
            }

            else -> Response.Success(Unit)
        }
    }

    suspend fun userRequestToPick(
        playerId: String,
        cardsToPick: Int,
        canPlay: Boolean
    ) {
        val cardState = CardStates.Pick(
            playerId = playerId,
            numOfCardsToPick = cardsToPick,
            canPlay = canPlay
        )
        handleCardStateUseCase.saveCardStateToPreferences(cardState)
    }

    suspend fun userRequestToHitBack(
        playerId: String,
        cardsToPick: Int,
        userCard: UserCard
    ): Response<Unit> {
        val cardState = multiplayerCrusadeCardsController.requestPlayerHitBack(
            playerId = playerId,
            cardsToPick = cardsToPick,
            userCard = userCard
        )
        handleCardStateUseCase.saveCardStateToPreferences(cardState)

        return if (cardState is CardStates.Error) {
            Response.Error(cardState.message)
        } else if (cardState is CardStates.Hit) {
            if (cardState.playerId == playerId) {
                // error
                Response.Error(cardState.message)
            } else {
                Response.Success(Unit)
            }
        } else Response.Success(Unit)
    }

    suspend fun userRequestToChangeCardType(
        playerId: String,
        type: String
    ) {
        val cardState = multiplayerCrusadeCardsController.requestPlayerChangeType(
            playerId = playerId,
            type = type
        )
        handleCardStateUseCase.saveCardStateToPreferences(cardState)
    }

    suspend fun userRequestToPickCards(
        currentCardState: CardStates,
        playerId: String
    ): Response<Unit> {

        return when (currentCardState) {

            is CardStates.Play -> {
                if (currentCardState.playerId == playerId && currentCardState.canPick) {
                    userRequestToPick(
                        playerId = currentCardState.playerId,
                        cardsToPick = 1,
                        canPlay = true
                    )
                }
                Response.Success(Unit)
            }

            is CardStates.Hit -> {
                if (currentCardState.playerId == playerId) {
                    userRequestToPick(
                        playerId = currentCardState.playerId,
                        cardsToPick = currentCardState.cardsToPick,
                        canPlay = false
                    )
                }
                Response.Success(Unit)
            }

            else -> {
                Response.Success(Unit)
            }
        }
    }

    suspend fun userRequestToChangeCardType(
        type: String,
        currentCardState: CardStates,
        playerId: String
    ): Response<Unit> {
        return when (currentCardState) {
            is CardStates.ChangeType -> {
                if (currentCardState.playerId == playerId) {
                    userRequestToChangeCardType(
                        playerId = currentCardState.playerId,
                        type = type
                    )
                    Response.Success(Unit)
                } else {
                    Response.Error(
                        message = "Unexpected behaviour, user has to define joker action"
                    )
                }
            }

            else -> {
                Response.Error(
                    message = "Unexpected behaviour, user has to define joker action"
                )
            }
        }
    }

}