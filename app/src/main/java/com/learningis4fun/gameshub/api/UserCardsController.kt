package com.learningis4fun.gameshub.api

import android.util.Log
import com.learningis4fun.gameshub.commons.domain.util.CardStates
import com.learningis4fun.gameshub.commons.domain.util.CardStatesDefaultMsg
import com.learningis4fun.gameshub.commons.domain.util.JokerActions
import com.learningis4fun.gameshub.commons.domain.util.Response
import com.learningis4fun.gameshub.domain.model.UserCard
import com.learningis4fun.gameshub.util.GamesHubLogTags
import com.learningis4fun.gameshub.util.getDataOrThrowException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
class UserCardsController @Inject constructor(
    private val handleCardStateUseCase: HandleCardStateUseCase,
    private val cruseCardsController: CrusadeCardsController,
    private val crusadeGameService: CrusadeGameService
) {

    suspend fun userRequestToAllowOthersPlay(gameId: String, playerId: String) {
        val cardState = CardStates.NoAvailableCard(
            playerId = playerId
        )
        handleCardStateUseCase.saveCardStateToPreferences(gameId = gameId, state = cardState)
    }

    suspend fun userRequestToDefineJokerAction(
        gameId: String,
        playerId: String,
        action: JokerActions,
        gameCardId: String
    ) {
        val cardState = when (action) {
            JokerActions.ChangeType -> {
                CardStates.ChangeType(playerId = playerId)
            }

            JokerActions.Hit -> {
                when (val userCardResponse = crusadeGameService.getUserCard(
                    gameId = gameId,
                    gameCardId = gameCardId
                )) {
                    is Response.Success -> {
                        cruseCardsController.requestPlayerHit(
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
        handleCardStateUseCase.saveCardStateToPreferences(gameId = gameId, state =  cardState)
    }

    suspend fun userRequestToPlay(
        userCard: UserCard,
        canPick: Boolean,
        currentCardType: String,
        currentCardName: String
    ): Response<Unit> {
        val cardState = cruseCardsController.requestUserPlay(
            card = userCard,
            currentCardName = currentCardName,
            currentCardType = currentCardType,
            canPick = canPick
        )
        handleCardStateUseCase.saveCardStateToPreferences(gameId = userCard.gameId, state = cardState)

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
        gameId: String,
        playerId: String,
        cardsToPick: Int,
        canPlay: Boolean,
    ) {
        val cardState = CardStates.Pick(
            playerId = playerId,
            numOfCardsToPick = cardsToPick,
            canPlay = canPlay
        )
        Log.d(GamesHubLogTags.USER_CRUSADE_CARDS_CONTROLLER, "pick request approved - gameId: $gameId cardState: $cardState")
        handleCardStateUseCase.saveCardStateToPreferences(gameId = gameId,state = cardState)
    }

    suspend fun userRequestToHitBack(
        playerId: String,
        cardsToPick: Int,
        userCard: UserCard
    ): Response<Unit> {
        val cardState = cruseCardsController.requestPlayerHitBack(
            playerId = playerId,
            cardsToPick = cardsToPick,
            userCard = userCard
        )
        handleCardStateUseCase.saveCardStateToPreferences(gameId = userCard.gameId, state = cardState)

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
        type: String,
        gameId: String
    ) {
        val cardState = cruseCardsController.requestPlayerChangeType(
            playerId = playerId,
            gameId = gameId,
            type = type
        )
        handleCardStateUseCase.saveCardStateToPreferences(gameId = gameId, state = cardState)
    }

    suspend fun userRequestToPickCards(
        currentCardState: CardStates,
        playerId: String,
        gameId: String
    ): Response<Unit> {

        Log.d(GamesHubLogTags.USER_CRUSADE_CARDS_CONTROLLER, "pick request- currentCardState: $currentCardState")

        return when (currentCardState) {

            is CardStates.Play -> {
                if (currentCardState.playerId == playerId && currentCardState.canPick) {
                    userRequestToPick(
                        gameId = gameId,
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
                        gameId = gameId,
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
        playerId: String,
        gameId: String
    ): Response<Unit> {
        return when (currentCardState) {
            is CardStates.ChangeType -> {
                if (currentCardState.playerId == playerId) {
                    userRequestToChangeCardType(
                        playerId = currentCardState.playerId,
                        gameId = gameId,
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