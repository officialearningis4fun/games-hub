package com.learningis4fun.gameshub.domain.use_cases

import android.util.Log
import com.learningis4fun.gameshub.commons.domain.controller.CrusadeComputerCardsController
import com.learningis4fun.gameshub.commons.domain.controller.MultiplayerCrusadeCardsController
import com.learningis4fun.gameshub.commons.domain.util.CardStates
import com.learningis4fun.gameshub.commons.domain.util.CardStatesDefaultMsg
import com.learningis4fun.gameshub.commons.domain.util.Constants
import com.learningis4fun.gameshub.commons.domain.util.GamePlayerActions
import com.learningis4fun.gameshub.commons.domain.util.Response
import com.learningis4fun.gameshub.data.data_source.CrusadeCardsPreferencesManager
import com.learningis4fun.gameshub.di.CrusadeCardsDataModule
import com.learningis4fun.gameshub.util.GamesHubLogTags
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.yield
import javax.inject.Inject

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
class HandleCardStateUseCase @Inject constructor(
    private val preferenceManager: CrusadeCardsPreferencesManager,
    private val cardsController: MultiplayerCrusadeCardsController,
    @CrusadeCardsDataModule.LocalCrusadeCardController private val computerCardsController: CrusadeComputerCardsController,
    @CrusadeCardsDataModule.OpenAICrusadeCardController private val openAICrusadeCardsController: CrusadeComputerCardsController,
    private val getLastGameStageWinnerOrFirstPlayer: GetLastGameStageWinnerOrFirstPlayer,
) {

    private val computerPlayers = listOf(
        preferenceManager.device1,
        preferenceManager.device2,
        preferenceManager.device3,
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

                when (state.playerId) {

                    preferenceManager.openAI.first().uniqueId -> {
                        val action = openAICrusadeCardsController.jokerCardAction(
                            gameId = gameId, playerId = state.playerId
                        )
                        cardState = if (action == Constants.CardJokerActions.changeType) {
                            CardStates.ChangeType(
                                playerId = state.playerId
                            )
                        } else {
                            val nextPlayerId = cardsController.getIdOfTheNextPlayer(
                                state.playerId, gameId = gameId
                            )
                            CardStates.Hit(
                                playerId = nextPlayerId, cardsToPick = 4
                            )
                        }
                    }

                    in computerPlayersIds -> {
                        val action = computerCardsController.jokerCardAction(
                            gameId = gameId, playerId = state.playerId
                        )
                        cardState = if (action == Constants.CardJokerActions.changeType) {
                            CardStates.ChangeType(
                                playerId = state.playerId
                            )
                        } else {
                            val nextPlayerId = cardsController.getIdOfTheNextPlayer(
                                state.playerId, gameId = gameId
                            )
                            CardStates.Hit(
                                playerId = nextPlayerId, cardsToPick = 4
                            )
                        }
                    }

                    else -> {
                        playCardState = GamePlayerActions.PromptPlayerDefineJokerCardAction(
                            playerId = state.playerId, gameCardId = state.gameCardId
                        )
                    }
                }

                currPlayerId = state.playerId
                message = state.message
            }

            is CardStates.Hit -> {
                when (state.playerId) {

                    preferenceManager.openAI.first().uniqueId -> {
                        val userCard = openAICrusadeCardsController.cardForComputerToHit(
                            playerId = state.playerId,
                            gameId = gameId,
                            cardsToPick = state.cardsToPick
                        )
                        cardState = if (userCard != null) {
                            cardsController.requestPlayerHitBack(
                                playerId = state.playerId,
                                cardsToPick = state.cardsToPick,
                                userCard = userCard
                            )
                        } else CardStates.Pick(
                            playerId = state.playerId,
                            numOfCardsToPick = state.cardsToPick,
                            canPlay = false,
                        )
                    }

                    in computerPlayersIds -> {
                        val userCard = computerCardsController.cardForComputerToHit(
                            playerId = state.playerId,
                            gameId = gameId,
                            cardsToPick = state.cardsToPick
                        )
                        cardState = if (userCard != null) {
                            cardsController.requestPlayerHitBack(
                                playerId = state.playerId,
                                cardsToPick = state.cardsToPick,
                                userCard = userCard
                            )
                        } else CardStates.Pick(
                            playerId = state.playerId,
                            numOfCardsToPick = state.cardsToPick,
                            canPlay = false,
                        )
                    }

                    else -> {
                        playCardState = GamePlayerActions.PromptPlayerHitBackOrPick(
                            playerId = state.playerId,
                            cardsToPick = state.cardsToPick,
                            message = promptUserHitBackOrPickMessage(
                                cardsToPick = state.cardsToPick
                            )
                        )
                    }
                }

                currPlayerId = state.playerId
                message = state.message
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
                            currentPlayer = state.playerId, gameId = gameId
                        )
                    )
                }

                currPlayerId = state.playerId
                message = state.message
            }

            is CardStates.Play -> {
                when (state.playerId) {

                    preferenceManager.openAI.first().uniqueId -> {
                        val card = openAICrusadeCardsController.playCardForComputer(
                            playerId = state.playerId, gameId = gameId
                        )

                        when (card) {
                            is Response.Error -> {
                                cardState = CardStates.Error(
                                    playerId = state.playerId,
                                    canPick = state.canPick,
                                    message = card.message.toString()
                                )
                            }

                            is Response.Success -> {
                                cardState = if (card.data != null) {
                                    cardsController.requestUserPlay(
                                        card = card.data, canPick = state.canPick
                                    )
                                } else if (state.canPick) {
                                    CardStates.Pick(
                                        playerId = state.playerId,
                                    )
                                } else {
                                    CardStates.NoAvailableCard(
                                        playerId = state.playerId
                                    )
                                }
                            }
                        }

                    }

                    in computerPlayersIds -> {
                        val card = computerCardsController.playCardForComputer(
                            playerId = state.playerId, gameId = gameId
                        )

                        when (card) {
                            is Response.Error -> {
                                cardState = CardStates.Error(
                                    playerId = state.playerId,
                                    canPick = state.canPick,
                                    message = card.message.toString()
                                )
                            }

                            is Response.Success -> {
                                cardState = if (card.data != null) {
                                    cardsController.requestUserPlay(
                                        card = card.data, canPick = state.canPick
                                    )
                                } else if (state.canPick) {
                                    CardStates.Pick(
                                        playerId = state.playerId,
                                    )
                                } else {
                                    CardStates.NoAvailableCard(
                                        playerId = state.playerId
                                    )
                                }
                            }
                        }


                    }

                    else -> {
                        val msg = if (state.canPick) {
                            CardStatesDefaultMsg.PlayOrPick
                        } else {
                            CardStatesDefaultMsg.PlayOnly
                        }

                        playCardState = GamePlayerActions.PromptPlayerPlayOrPick(
                            playerId = state.playerId, canPick = state.canPick, message = msg
                        )
                    }
                }

                currPlayerId = state.playerId
                message = state.message
            }

            is CardStates.Played -> {

                cardState = CardStates.Play(
                    playerId = cardsController.getIdOfTheNextPlayer(
                        currentPlayer = state.playerId, gameId = gameId
                    )
                )

                currPlayerId = state.playerId
                message = state.message
            }

            is CardStates.ChangeType -> {
                when (state.playerId) {

                    preferenceManager.openAI.first().uniqueId -> {
                        val newCardType = openAICrusadeCardsController.newCardType(
                            playerId = state.playerId, gameId = gameId
                        )
                        cardState = cardsController.requestPlayerChangeType(
                            playerId = state.playerId, type = newCardType
                        )
                    }

                    in computerPlayersIds -> {
                        val newCardType = computerCardsController.newCardType(
                            playerId = state.playerId, gameId = gameId
                        )
                        cardState = cardsController.requestPlayerChangeType(
                            playerId = state.playerId, type = newCardType
                        )
                    }

                    else -> {
                        val currState = GamePlayerActions.PromptPlayerChangeCardType(
                            playerId = state.playerId
                        )
                        playCardState = currState
                    }
                }

                currPlayerId = state.playerId
                message = state.message
            }

            is CardStates.ChangedType -> {
                cardState = CardStates.Play(
                    playerId = cardsController.getIdOfTheNextPlayer(
                        currentPlayer = state.playerId, gameId = gameId
                    )
                )

                currPlayerId = state.playerId
                message = state.message
            }

            is CardStates.NoAvailableCard -> {

                cardState = CardStates.Play(
                    playerId = cardsController.getIdOfTheNextPlayer(
                        currentPlayer = state.playerId, gameId = gameId
                    )
                )

                currPlayerId = state.playerId
                message = state.message
            }

            is CardStates.Start -> {

                val playerToStartTheGame = getLastGameStageWinnerOrFirstPlayer(gameId = gameId)

                cardState = playerToStartTheGame?.let { player ->
                    CardStates.Play(
                        playerId = player.uniqueId, canPick = false
                    )
                } ?: CardStates.Error(
                    playerId = "",
                    message = "Could not find an appropriate player to start the game"
                )
            }

            is CardStates.Error -> {
                Log.e(
                    GamesHubLogTags.HANDLE_CARD_STATE_USE_CASE,
                    "There is an error while ${state.playerId} playing..."
                )
            }
        }

        cardState?.let {
            yield()
            saveCardStateToPreferences(it)
        }

        // update playCardState
        when (state) {
            is CardStates.ChangeType, is CardStates.Play, is CardStates.Hit, is CardStates.DefineCardAction -> Unit

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
                com.learningis4fun.gameshub.domain.model.CardStateEntity(
                    cardStateName = state.name, playerId = state.playerId, message = state.message
                )
            }

            is CardStates.ChangeType -> {
                com.learningis4fun.gameshub.domain.model.CardStateEntity(
                    cardStateName = state.name, playerId = state.playerId, message = state.message
                )
            }

            is CardStates.ChangedType -> {
                com.learningis4fun.gameshub.domain.model.CardStateEntity(
                    cardStateName = state.name, playerId = state.playerId, message = state.message
                )
            }

            is CardStates.DefineCardAction -> {
                com.learningis4fun.gameshub.domain.model.CardStateEntity(
                    cardStateName = state.name,
                    playerId = state.playerId,
                    gameCardId = state.gameCardId,
                    message = state.message
                )
            }

            is CardStates.Error -> {
                com.learningis4fun.gameshub.domain.model.CardStateEntity(
                    cardStateName = state.name, message = state.message
                )
            }

            is CardStates.Hit -> {
                com.learningis4fun.gameshub.domain.model.CardStateEntity(
                    cardStateName = state.name,
                    playerId = state.playerId,
                    cardsToPick = state.cardsToPick,
                    message = state.message
                )
            }

            is CardStates.NoAvailableCard -> {
                com.learningis4fun.gameshub.domain.model.CardStateEntity(
                    cardStateName = state.name, playerId = state.playerId, message = state.message
                )
            }

            is CardStates.Pick -> {
                com.learningis4fun.gameshub.domain.model.CardStateEntity(
                    cardStateName = state.name,
                    playerId = state.playerId,
                    canPlay = state.canPlay,
                    cardsToPick = state.numOfCardsToPick,
                    message = state.message
                )
            }

            is CardStates.Picked -> {
                com.learningis4fun.gameshub.domain.model.CardStateEntity(
                    cardStateName = state.name,
                    playerId = state.playerId,
                    canPlay = state.canPlay,
                    cardsPicked = state.cardsPicked,
                    message = state.message
                )
            }

            is CardStates.Play -> {
                com.learningis4fun.gameshub.domain.model.CardStateEntity(
                    cardStateName = state.name,
                    playerId = state.playerId,
                    canPick = state.canPick,
                    message = state.message
                )
            }

            is CardStates.Played -> {
                com.learningis4fun.gameshub.domain.model.CardStateEntity(
                    cardStateName = state.name, playerId = state.playerId, message = state.message
                )
            }

            CardStates.Start -> {
                com.learningis4fun.gameshub.domain.model.CardStateEntity(
                    cardStateName = state.name
                )
            }
        }
        preferenceManager.updateRecentCardState(stateEntity)
    }
}