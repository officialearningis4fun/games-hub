package com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.use_case

import com.learningis4fun.gameshub.feature_crusade_cards.commons.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.repository.DefaultCardRepository
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.controller.CrusadeComputerCardsController
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.controller.MultiplayerCrusadeCardsController
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.repository.UserRepository
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.CardStates
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.CardStatesDefaultMsg
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.CardStateEntity
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.GamePlayerActions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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

    val user = userRepository.getPlayerSignInDetails()
    val androidUser = userRepository.getAndroidPlayerSignInDetails()

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

    suspend fun handleCardState(state: CardStates): GamePlayerActions? {

        var currPlayerId = ""
        var message = ""

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

                if (state.playerId == androidUser.first().uniqueId) {
                    val action = defaultCardRepository.jokerCardActionByComputer()
                    cardState = if (action == Constants.CardJokerActions.changeType) {
                        CardStates.ChangeType(
                            playerId = state.playerId
                        )
                    } else {
                        CardStates.Hit(
                            playerId = user.first().uniqueId,
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
                if (state.playerId == androidUser.first().uniqueId) {
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
                    val playerId = if (state.playerId == androidUser.first().uniqueId) {
                        user.first().uniqueId
                    } else {
                        androidUser.first().uniqueId
                    }

                    CardStates.Play(
                        playerId = playerId
                    )
                }

                currPlayerId = state.playerId
                message = state.message
            }
            is CardStates.Play -> {
                if (state.playerId == androidUser.first().uniqueId) {
                    val card = computerCardsController.playCardForComputer(
                        playerId = state.playerId
                    )
                    cardState = card?.let {
                        cardsController.requestUserPlay(
                            card = card,
                            canPick = state.canPick
                        )
                    } ?: if(state.canPick){
                        CardStates.Pick(
                            playerId = state.playerId,
                        )
                    } else {
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
                cardState = if (state.playerId == androidUser.first().uniqueId) {
                    CardStates.Play(
                        playerId = user.first().uniqueId
                    )
                } else {
                    CardStates.Play(
                        playerId = androidUser.first().uniqueId
                    )
                }

                currPlayerId = state.playerId
                message = state.message
            }
            is CardStates.ChangeType -> {
                if (state.playerId == androidUser.first().uniqueId) {
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
                cardState = if (state.playerId == androidUser.first().uniqueId) {
                    CardStates.Play(
                        playerId = user.first().uniqueId
                    )
                } else {
                    CardStates.Play(
                        playerId = androidUser.first().uniqueId
                    )
                }

                currPlayerId = state.playerId
                message = state.message
            }
            is CardStates.NoAvailableCard -> {

                cardState = if (state.playerId == androidUser.first().uniqueId) {
                    CardStates.Play(
                        playerId = user.first().uniqueId
                    )
                } else {
                    CardStates.Play(
                        playerId = androidUser.first().uniqueId,
                    )
                }

                currPlayerId = state.playerId
                message = state.message
            }
            is CardStates.Start -> {
                // computer always start the game for now
                cardState = CardStates.Play(
                    playerId = androidUser.first().uniqueId,
                    canPick = false
                )

            }
            is CardStates.Error -> {
                // send errors through -> TODO()
            }
        }
        cardState?.let {
            saveCardStateToPreferences(it)
        }

        when (state) {
            is CardStates.ChangeType,
            is CardStates.Play,
            is CardStates.Hit,
            is CardStates.DefineCardAction -> Unit
            else -> playCardState = GamePlayerActions.Default
        }

        if (currPlayerId.isNotEmpty() && currPlayerId == androidUser.first().uniqueId) {
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