package com.learningis4fun.gameshub.api

import android.util.Log
import com.learningis4fun.gameshub.api.use_cases.promptUserHitBackOrPickMessage
import com.learningis4fun.gameshub.commons.domain.model.GameStage
import com.learningis4fun.gameshub.commons.domain.util.CardStates
import com.learningis4fun.gameshub.commons.domain.util.CardStatesDefaultMsg
import com.learningis4fun.gameshub.commons.domain.util.GamePlayerActions
import com.learningis4fun.gameshub.data.data_source.remote.GoogleAuthUiClient
import com.learningis4fun.gameshub.domain.model.CardStateEntity
import com.learningis4fun.gameshub.util.GamesHubLogTags
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.yield
import javax.inject.Inject

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
class HandleCardStateUseCase @Inject constructor(
    private val crusadeGameService: CrusadeGameService,
    private val cardsController: CrusadeCardsController,
    private val getLastGameStageWinnerIdOrFirstPlayerId: GetLastGameStageWinnerIdOrFirstPlayerId,
    private val googleAuthUiClient: GoogleAuthUiClient
) {

    suspend fun signedInPlayerId(): String? {
        return googleAuthUiClient.signedInUser.first()?.id
    }

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
     * from com picked to either play with other playersId or play with it's playerId if player can Play
     *
     * from user picked to either play with other playersId or play with it's playerId  if user can Play
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

    suspend fun handleCardState(
        state: CardStates,
        gameId: String,
        gameStages: List<GameStage>,
        winnerId: String?
    ): GamePlayerActions? {

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

                when (state.playerId) {
                    signedInPlayerId() -> {
                        playCardState = GamePlayerActions.PromptPlayerDefineJokerCardAction(
                            playerId = state.playerId, gameCardId = state.gameCardId
                        )
                    }

                    else -> {}
                }

                currPlayerId = state.playerId
                message = state.message
            }

            is CardStates.Hit -> {
                when (state.playerId) {

                    signedInPlayerId() -> playCardState =
                        GamePlayerActions.PromptPlayerHitBackOrPick(
                            playerId = state.playerId,
                            cardsToPick = state.cardsToPick,
                            message = promptUserHitBackOrPickMessage(
                                cardsToPick = state.cardsToPick
                            )
                        )

                    else -> {}
                }

                currPlayerId = state.playerId
                message = state.message
            }

            is CardStates.Pick -> {
                val stageNumber = gameStages.maxByOrNull { it.stageNumber }!!.stageNumber
                cardState = cardsController.requestPlayerPick(
                    stageNumber = stageNumber,
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

                    signedInPlayerId() -> {
                        val msg = if (state.canPick) {
                            CardStatesDefaultMsg.PlayOrPick
                        } else {
                            CardStatesDefaultMsg.PlayOnly
                        }

                        playCardState = GamePlayerActions.PromptPlayerPlayOrPick(
                            playerId = state.playerId, canPick = state.canPick, message = msg
                        )
                    }

                    else -> {}
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
                Log.d(GamesHubLogTags.MULTIPLAYER_CRUSADE_CARDS_CONTROLLER, "state: $cardState")

                currPlayerId = state.playerId
                message = state.message
            }

            is CardStates.ChangeType -> {
                when (state.playerId) {
                    signedInPlayerId() -> {
                        val currState = GamePlayerActions.PromptPlayerChangeCardType(
                            playerId = state.playerId
                        )
                        playCardState = currState
                    }

                    else -> {}
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

                val playerIdToStartTheGame = getLastGameStageWinnerIdOrFirstPlayerId(
                    gameId = gameId,
                    gameStages = gameStages,
                    winnerId = winnerId
                )

                cardState = playerIdToStartTheGame?.let { playerId ->
                    CardStates.Play(
                        playerId = playerId, canPick = false
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
            saveCardStateToPreferences(gameId = gameId, state = it)
        }

        // update playCardState
        when (state) {
            is CardStates.ChangeType, is CardStates.Play, is CardStates.Hit, is CardStates.DefineCardAction -> Unit

            else -> playCardState = GamePlayerActions.Default
        }

        if (currPlayerId.isNotEmpty()) {
            playCardState = GamePlayerActions.ActionMessages(message)
        }

        return playCardState
    }

    suspend fun saveCardStateToPreferences(state: CardStates, gameId: String) {
        val stateEntity = when (state) {
            is CardStates.Chance -> {
                CardStateEntity(
                    cardStateName = state.name, playerId = state.playerId, message = state.message
                )
            }

            is CardStates.ChangeType -> {
                CardStateEntity(
                    cardStateName = state.name, playerId = state.playerId, message = state.message
                )
            }

            is CardStates.ChangedType -> {
                CardStateEntity(
                    cardStateName = state.name, playerId = state.playerId, message = state.message
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
                    cardStateName = state.name, message = state.message
                )
            }

            is CardStates.Hit -> {
                CardStateEntity(
                    cardStateName = state.name,
                    playerId = state.playerId,
                    cardsToPick = state.cardsToPick,
                    message = state.message
                )
            }

            is CardStates.NoAvailableCard -> {
                CardStateEntity(
                    cardStateName = state.name, playerId = state.playerId, message = state.message
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
                    cardStateName = state.name, playerId = state.playerId, message = state.message
                )
            }

            CardStates.Start -> {
                CardStateEntity(
                    cardStateName = state.name
                )
            }
        }
        crusadeGameService.updateCardState(gameId = gameId, stateEntity)
    }
}