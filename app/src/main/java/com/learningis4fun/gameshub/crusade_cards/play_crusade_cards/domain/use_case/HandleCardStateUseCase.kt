package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case

import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.crusade_cards.commons.domain.controller.CrusadeComputerCardsController
import com.learningis4fun.gameshub.crusade_cards.commons.domain.controller.MultiplayerCrusadeCardsController
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.DefaultCardRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.repository.UserRepository
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.CardStates
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.CardStatesDefaultMsg
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.GamePlayerActions
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.getPlayerId
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.CardStateEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.yield
import javax.inject.Inject

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
class HandleCardStateUseCase @Inject constructor(
    userRepository: UserRepository,
    private val preferenceManager: CrusadeCardsPreferencesManager,
    private val defaultCardRepository: DefaultCardRepository,
    private val cardsController: MultiplayerCrusadeCardsController,
    private val computerCardsController: CrusadeComputerCardsController
) {

    private val computerPlayers = listOf(
        userRepository.getDevice1PlayerSignInDetails(),
        userRepository.getDevice2PlayerSignInDetails(),
        userRepository.getDevice3PlayerSignInDetails()
    )

    /**
     * from start to Computer Play
     *
     * from com chance to play with the same playerId and canPlay-true
     *
     * from user chance to play with same playerId and canPlay-true
     *
     * from com define-card-action to hit or change-type to changed-type
     *
     * the user make this action -> to hit or change-type
     *
     * from com hit with valid card to hit other user or pick with invalid card
     *
     * the user make this action
     *
     * from com pick to picked
     *
     * from user pick to picked
     *
     * from com picked to either play with other playersId or play with it's playerId Id if canPlay
     *
     * from user picked to either play with other playersId or play with it's playerId Id if canPlay
     *
     * from com play with two card not seven, eight, jack, joker or two to played
     *
     *  * from com play with invalid card and canPick-true to pick
     *
     *  * from com play with invalid card to noAvailableCard
     *
     * from com play with two to hit
     *
     * from com play with joker to defineAction
     *
     * from com play with eight to change type
     *
     * from com play with seven or jack to chance
     *
     *
     * the user make action
     *
     * from com played to play with other playersId
     *
     * from user played to play with com's playerId
     *
     * from com change-type to changed type
     *
     * the user to make this action
     *
     * from com changed-type to play with other playersId
     *
     * from user changed-type to play with com's playerId
     *
     * from com start to play with same playerId
     */

    suspend fun handleCardState(state: CardStates, gameId: String): GamePlayerActions? {

        var currPlayerId = ""
        var message = ""

        val computerPlayersIds = computerPlayers.map { it.first().uniqueId }

        var cardState: CardStates? = null
        var playCardState: GamePlayerActions? = null
        when (state) {
            is CardStates.Chance -> {
                // can play
                cardState = CardStates.Play(
                    playerId = state.playerId
                )
                currPlayerId = state.playerId
                message = state.message
            }

            is CardStates.DefineCardAction -> {

                if (state.playerId in computerPlayersIds) {
                    val action = defaultCardRepository.jokerCardActionByComputer()
                    cardState = if (action == Constants.CardJokerActions.changeType) {
                        CardStates.ChangeType(
                            playerId = state.playerId
                        )
                    } else {
                        val nextPlayerId = cardsController.getIdOfTheNextPlayer(
                            state.playerId,
                            gameId = gameId
                        )
                        CardStates.Hit(
                            playerId = nextPlayerId,
                            cardsToPick = 4
                        )
                    }
                } else {
                    playCardState = GamePlayerActions.PromptPlayerDefineJokerCardAction(
                        playerId = state.playerId,
                        gameCardId = state.gameCardId
                    )
                }
                currPlayerId = state.playerId
                message = state.message
            }

            is CardStates.Hit -> {
                if (state.playerId in computerPlayersIds) {
                    val userCard = computerCardsController.cardForComputerToHit(
                        playerId = state.playerId
                    )
                    cardState = userCard?.let {
                        cardsController.requestPlayerHitBack(
                            playerId = state.playerId,
                            cardsToPick = state.cardsToPick,
                            userCard = userCard
                        )

                    } ?: CardStates.Pick(
                        playerId = state.playerId,
                        numOfCardsToPick = state.cardsToPick,
                        canPlay = false,
                    )

                } else {
                    playCardState = GamePlayerActions.PromptPlayerHitBackOrPick(
                        playerId = state.playerId,
                        cardsToPick = state.cardsToPick,
                        message = promptUserHitBackOrPickMessage(
                            cardsToPick = state.cardsToPick
                        )
                    )
                }

                currPlayerId = state.playerId
                message = state.message.toString()
            }

            is CardStates.Pick -> {
                cardState = cardsController.requestPlayerPick(
                    gameId = gameId,
                    playerId = state.playerId,
                    cardsToPick = state.numOfCardsToPick,
                    canPlay = state.canPlay
                )
                currPlayerId = state.playerId
                message = state.message
            }

            is CardStates.Picked -> {
                cardState = if (state.canPlay) {

                    CardStates.Play(
                        playerId = state.playerId,
                        canPick = false,
                        message = CardStatesDefaultMsg.PlayOnly
                    )
                } else {

                    CardStates.Play(
                        playerId = cardsController.getIdOfTheNextPlayer(
                            currentPlayer = state.playerId,
                            gameId = gameId
                        )
                    )
                }

                currPlayerId = state.playerId
                message = state.message
            }

            is CardStates.Play -> {
                if (state.playerId in computerPlayersIds) {
                    println("${state.playerId} is playing")

                    val card = computerCardsController.playCardForComputer(
                        playerId = state.playerId,
                        gameId = gameId
                    )
                    println("the available card is ${card?.card}")
                    cardState = card?.let {
                        println("${state.playerId} is playing the card")
                        cardsController.requestUserPlay(
                            card = card,
                            canPick = state.canPick
                        )
                    } ?: if (state.canPick) {
                        println("${state.playerId} is requesting to pick a card")
                        CardStates.Pick(
                            playerId = state.playerId,
                        )
                    } else {
                        println("${state.playerId} is doesn't have a card")
                        CardStates.NoAvailableCard(
                            playerId = state.playerId
                        )
                    }

                } else {
                    val msg = if (state.canPick) {
                        CardStatesDefaultMsg.PlayOrPick
                    } else {
                        CardStatesDefaultMsg.PlayOnly
                    }

                    playCardState = GamePlayerActions.PromptPlayerPlayOrPick(
                        playerId = state.playerId,
                        canPick = state.canPick,
                        message = msg
                    )
                }

                currPlayerId = state.playerId
                message = state.message
            }

            is CardStates.Played -> {

                cardState = CardStates.Play(
                    playerId = cardsController.getIdOfTheNextPlayer(
                        currentPlayer = state.playerId,
                        gameId = gameId
                    )
                )

                currPlayerId = state.playerId
                message = state.message
            }

            is CardStates.ChangeType -> {
                if (state.playerId in computerPlayersIds) {
                    val newCardType = computerCardsController.newCardType(state.playerId)
                    cardState = cardsController.requestPlayerChangeType(
                        playerId = state.playerId,
                        type = newCardType
                    )
                } else {
                    val currState = GamePlayerActions.PromptPlayerChangeCardType(
                        playerId = state.playerId
                    )
                    playCardState = currState
                }

                currPlayerId = state.playerId
                message = state.message
            }

            is CardStates.ChangedType -> {
                cardState = CardStates.Play(
                    playerId = cardsController.getIdOfTheNextPlayer(
                        currentPlayer = state.playerId,
                        gameId = gameId
                    )
                )

                currPlayerId = state.playerId
                message = state.message
            }

            is CardStates.NoAvailableCard -> {

                cardState = CardStates.Play(
                    playerId = cardsController.getIdOfTheNextPlayer(
                        currentPlayer = state.playerId,
                        gameId = gameId
                    )
                )

                currPlayerId = state.playerId
                message = state.message
            }

            is CardStates.Start -> {
                // computer always start the game for now
                cardState = CardStates.Play(
                    playerId = computerPlayersIds.first(),
                    canPick = false
                )

            }

            is CardStates.Error -> {
                println("There is an error while playing...")
            }
        }
        cardState?.let {
            yield()
            saveCardStateToPreferences(it)
        }

        when (state) {
            is CardStates.ChangeType,
            is CardStates.Play,
            is CardStates.Hit,
            is CardStates.DefineCardAction -> Unit

            else -> playCardState = GamePlayerActions.Default
        }

        if (currPlayerId.isNotEmpty() && currPlayerId in computerPlayersIds) {
            playCardState = GamePlayerActions.ActionMessages(message)
        }

        return playCardState
    }

    suspend fun saveCardStateToPreferences(state: CardStates) {
        val stateEntity = when (state) {
            is CardStates.Chance -> {
                CardStateEntity(
                    cardStateName = state.name,
                    playerId = state.playerId,
                    message = state.message
                )
            }

            is CardStates.ChangeType -> {
                CardStateEntity(
                    cardStateName = state.name,
                    playerId = state.playerId,
                    message = state.message
                )
            }

            is CardStates.ChangedType -> {
                CardStateEntity(
                    cardStateName = state.name,
                    playerId = state.playerId,
                    message = state.message
                )
            }

            is CardStates.DefineCardAction -> {
                CardStateEntity(
                    cardStateName = state.name,
                    playerId = state.playerId,
                    gameCardId = state.gameCardId,
                    message = state.message
                )
            }

            is CardStates.Error -> {
                CardStateEntity(
                    cardStateName = state.name,
                    message = state.message
                )
            }

            is CardStates.Hit -> {
                CardStateEntity(
                    cardStateName = state.name,
                    playerId = state.playerId,
                    cardsToPick = state.cardsToPick,
                    message = state.message.toString()
                )
            }

            is CardStates.NoAvailableCard -> {
                CardStateEntity(
                    cardStateName = state.name,
                    playerId = state.playerId,
                    message = state.message
                )
            }

            is CardStates.Pick -> {
                CardStateEntity(
                    cardStateName = state.name,
                    playerId = state.playerId,
                    canPlay = state.canPlay,
                    cardsToPick = state.numOfCardsToPick,
                    message = state.message
                )
            }

            is CardStates.Picked -> {
                CardStateEntity(
                    cardStateName = state.name,
                    playerId = state.playerId,
                    canPlay = state.canPlay,
                    cardsPicked = state.cardsPicked,
                    message = state.message
                )
            }

            is CardStates.Play -> {
                CardStateEntity(
                    cardStateName = state.name,
                    playerId = state.playerId,
                    canPick = state.canPick,
                    message = state.message
                )
            }

            is CardStates.Played -> {
                CardStateEntity(
                    cardStateName = state.name,
                    playerId = state.playerId,
                    message = state.message
                )
            }

            CardStates.Start -> {
                CardStateEntity(
                    cardStateName = state.name
                )
            }
        }
        preferenceManager.updateRecentCardState(stateEntity)
    }

    fun promptUserHitBackOrPickMessage(cardsToPick: Int): String {
        return "${CardStatesDefaultMsg.HitBackOrPick} $cardsToPick cards"
    }
}