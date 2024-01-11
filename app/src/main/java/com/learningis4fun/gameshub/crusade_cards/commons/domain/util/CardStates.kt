package com.learningis4fun.gameshub.crusade_cards.commons.domain.util

import com.learningis4fun.gameshub.crusade_cards.feature_play_crusade_cards.domain.model.CardStateEntity

sealed class CardStates(val name: String) {

    object Start : CardStates("start")
    data class Play(val playerId: String, val canPick: Boolean = true, val message: String = CardStatesDefaultMsg.Play) :
        CardStates("play")

    data class Played(val playerId: String, val message: String = CardStatesDefaultMsg.Played) : CardStates("played")
    data class Pick(
        val playerId: String,
        val numOfCardsToPick: Int = 1,
        val canPlay: Boolean = true,
        val message: String = CardStatesDefaultMsg.Pick
    ) : CardStates("pick")

    data class Picked(
        val playerId: String,
        val canPlay: Boolean = true,
        val cardsPicked: Int,
        val message: String = CardStatesDefaultMsg.Picked
    ) : CardStates("picked")

    data class Hit(val playerId: String, val message: String? = CardStatesDefaultMsg.Hit, val cardsToPick: Int) :
        CardStates("hit")

    data class ChangeType(val playerId: String, val message: String = CardStatesDefaultMsg.ChangeType) : CardStates("change-type")
    data class ChangedType(val playerId: String, val message: String = CardStatesDefaultMsg.ChangedType) : CardStates("changed-type")
    data class DefineCardAction(
        val playerId: String,
        val gameCardId: String = "",
        val message: String = CardStatesDefaultMsg.DefineCardAction
    ) : CardStates("define-card-action")

    data class Chance(val playerId: String, val message: String = CardStatesDefaultMsg.Chance) : CardStates("chance")
    data class NoAvailableCard(val playerId: String, val message: String = CardStatesDefaultMsg.NoAvailableCard) :
        CardStates("no-available-card")

    data class Error(val message: String) : CardStates("error")
}

fun toCardState(state: CardStateEntity): CardStates = when (state.cardStateName) {
    CardStates.Start.name -> CardStates.Start
    "play" -> CardStates.Play(
        playerId = state.playerId,
        canPick = state.canPick,
        message = state.message
    )
    "played" -> CardStates.Played(
        playerId = state.playerId, message = state.message
    )
    "pick" -> CardStates.Pick(
        playerId = state.playerId,
        numOfCardsToPick = state.cardsToPick,
        canPlay = state.canPlay,
        message = state.message
    )
    "picked" -> CardStates.Picked(
        playerId = state.playerId,
        canPlay = state.canPlay,
        cardsPicked = state.cardsPicked,
        message = state.message
    )
    "hit" -> CardStates.Hit(
        playerId = state.playerId,
        cardsToPick = state.cardsToPick,
        message = state.message
    )
    "change-type" -> CardStates.ChangeType(playerId = state.playerId, message = state.message)
    "changed-type" -> CardStates.ChangedType(playerId = state.playerId, message = state.message)
    "define-card-action" -> CardStates.DefineCardAction(
        playerId = state.playerId,
        gameCardId = state.gameCardId,
        message = state.message
    )
    "chance" -> CardStates.Chance(playerId = state.playerId, message = state.message)
    "no-available-card" -> CardStates.NoAvailableCard(
        playerId = state.playerId,
        message = state.message
    )
    "error" -> CardStates.Error(message = state.message)
    else -> CardStates.Start
}

data class CardStateAndContent(
    val state: String,
    val contents: String
)

object CardStatesDefaultMsg{
    const val Play = "Requested to play"
    const val Played = "Successfully played"
    const val PlayOnly = "Requested to play or allow others play if you have no card"
    const val PlayOrPick = "Requested to play or pick"
    const val Pick = "Requested to pick"
    const val Picked = "Successfully picked"
    const val HitBackOrPick = "Requested to hit back or pick"
    const val HitError = "Unable to hit, play another card or pick"
    const val Hit = "Successfully hit"
    const val ChangeType = "Requested to change type"
    const val ChangedType = "Successfully changed type"
    const val DefineCardAction = "Requested to define card action"
    const val Chance = "Play another card"
    const val PlayError = "Play another card or pick"
    const val NoAvailableCard = "Have no card to play"
}



