package com.learningis4fun.gameshub.api.use_cases

import android.util.Log
import com.learningis4fun.gameshub.commons.domain.util.CardStates
import com.learningis4fun.gameshub.commons.domain.util.CardStatesDefaultMsg
import com.learningis4fun.gameshub.commons.domain.util.GamePlayerActions
import com.learningis4fun.gameshub.commons.domain.util.getPlayerId
import com.learningis4fun.gameshub.util.GamesHubLogTags

fun CardStates.toGamePlayerAction(userId: String): GamePlayerActions? {
    return try {
        Log.d(GamesHubLogTags.UTIL, "cardState: $this")
        this.checkPlayerId(userId)

        when (val state = this) {
            CardStates.Start -> GamePlayerActions.ActionMessages("Hitting up the scene")
            is CardStates.ChangedType,
            is CardStates.Chance,
            is CardStates.NoAvailableCard,
            is CardStates.Played,
            is CardStates.Picked,
            is CardStates.Pick -> null

            is CardStates.ChangeType -> {
                GamePlayerActions.PromptPlayerChangeCardType(
                    playerId = state.playerId
                )
            }

            is CardStates.DefineCardAction -> {

                GamePlayerActions.PromptPlayerDefineJokerCardAction(
                    playerId = state.playerId, gameCardId = state.gameCardId
                )
            }

            is CardStates.Hit -> {
                GamePlayerActions.PromptPlayerHitBackOrPick(
                    playerId = state.playerId,
                    cardsToPick = state.cardsToPick,
                    message = promptUserHitBackOrPickMessage(
                        cardsToPick = state.cardsToPick
                    )
                )
            }

            is CardStates.Play -> {

                val msg = if (state.canPick) {
                    CardStatesDefaultMsg.PlayOrPick
                } else {
                    CardStatesDefaultMsg.PlayOnly
                }

                GamePlayerActions.PromptPlayerPlayOrPick(
                    playerId = state.playerId, canPick = state.canPick, message = msg
                )
            }

            is CardStates.Error -> {
                GamePlayerActions.Error(playerId = state.playerId, message = state.message)
            }

        }
    } catch (e: Exception) {
        null
    }
}

@Throws
fun CardStates.checkPlayerId(userId: String) {
    val playerId = this.getPlayerId()
    if (playerId == userId) Unit else throw Exception("PlayerId of CardState doesn't match the userId")
}

fun promptUserHitBackOrPickMessage(cardsToPick: Int): String {
    return "${CardStatesDefaultMsg.HitBackOrPick} $cardsToPick cards"
}