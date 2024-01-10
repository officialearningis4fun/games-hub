package com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.use_case

import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.repository.GameSocketService
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.repository.UserRepository
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.CardStates
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.CardStatesDefaultMsg
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.GamePlayerActions
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.promptUserHitBackOrPickMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetGamePlayerAction @Inject constructor(
    private val gameSocketService: GameSocketService,
    private val userRepository: UserRepository
) {

    private fun saveAndReturnCardState(): Flow<CardStates> {
        return flow { }
    }

    operator fun invoke(): Flow<Response<GamePlayerActions>> =
        saveAndReturnCardState().map { cardStates ->

            val playerId = userRepository.getUserId()

            var gamePlayerAction: GamePlayerActions? = null
            when (cardStates) {
                is CardStates.Chance -> {
                    // not expected
                }
                is CardStates.DefineCardAction -> {

                    if (cardStates.playerId == playerId) {
                        gamePlayerAction = GamePlayerActions.PromptPlayerDefineJokerCardAction(
                            playerId = cardStates.playerId,
                            gameCardId = cardStates.gameCardId
                        )
                    } else {
                        gamePlayerAction = GamePlayerActions.ActionMessages("Defining joker action")
                    }
                }
                is CardStates.Hit -> {

                    gamePlayerAction = if (cardStates.playerId == playerId) {
                        GamePlayerActions.PromptPlayerHitBackOrPick(
                            playerId = cardStates.playerId,
                            cardsToPick = cardStates.cardsToPick,
                            message = promptUserHitBackOrPickMessage(
                                cardsToPick = cardStates.cardsToPick
                            )
                        )
                    } else {
                        GamePlayerActions.ActionMessages("Someone is hit")
                    }
                }
                is CardStates.Pick -> {
                    //Note expected
                }
                is CardStates.Picked -> {
                    //Note expected
                }
                is CardStates.Play -> {
                    gamePlayerAction = if (cardStates.playerId == playerId) {
                        val msg = if (cardStates.canPick) {
                            CardStatesDefaultMsg.PlayOrPick
                        } else {
                            CardStatesDefaultMsg.PlayOnly
                        }

                        GamePlayerActions.PromptPlayerPlayOrPick(
                            playerId = cardStates.playerId,
                            canPick = cardStates.canPick,
                            message = msg
                        )
                    } else {
                        GamePlayerActions.ActionMessages("Someone is playing")
                    }
                }
                is CardStates.Played -> {
                    //Note expected
                }
                is CardStates.ChangeType -> {
                    gamePlayerAction = if (cardStates.playerId == playerId) {
                        GamePlayerActions.PromptPlayerChangeCardType(
                            playerId = cardStates.playerId
                        )
                    } else {
                        GamePlayerActions.ActionMessages("Someone is asked to change type")
                    }
                }
                is CardStates.ChangedType -> {
                    // not expected
                }
                is CardStates.NoAvailableCard -> {
                    // not expected
                }
                is CardStates.Start -> {
                    // not expected
                }
                is CardStates.Error -> {
                    // send errors through -> TODO()
                }
            }
            gamePlayerAction?.let {
                Response.Success(
                   data = it,
                    message = ""
                )
            }?: Response.Error(
                data = GamePlayerActions.Default,
                message = ""
            )
        }
}