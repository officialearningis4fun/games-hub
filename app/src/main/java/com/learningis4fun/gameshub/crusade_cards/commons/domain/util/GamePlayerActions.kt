package com.learningis4fun.gameshub.crusade_cards.commons.domain.util

sealed class GamePlayerActions(val name: String) {

    data object Default : GamePlayerActions("default")

    data class ActionMessages(val message: String) : GamePlayerActions("messages")

    data class PromptPlayerDefineJokerCardAction(
        val playerId: String,
        val gameCardId: String,
        val message: String = CardStatesDefaultMsg.DefineCardAction
    ) : GamePlayerActions("define-joker-actions")

    data class PromptPlayerHitBackOrPick(
        val playerId: String,
        val cardsToPick: Int,
        val message: String = CardStatesDefaultMsg.HitBackOrPick
    ) : GamePlayerActions("hit-back-or-pick")

    data class PromptPlayerPlayOrPick(
        val playerId: String,
        val canPick: Boolean,
        val message: String = CardStatesDefaultMsg.PlayOrPick
    ) : GamePlayerActions("play-or-pick")

    data class PromptPlayerChangeCardType(
        val playerId: String,
        val message: String = CardStatesDefaultMsg.ChangeType
    ) : GamePlayerActions("change-card-type")
}

fun GamePlayerActions.toDebugMsg(): String {
    return when(val action = this){
        is GamePlayerActions.ActionMessages -> "${action.name}: ${action.message}"
        GamePlayerActions.Default -> action.name
        is GamePlayerActions.PromptPlayerChangeCardType -> "${action.name}: ${action.message}"
        is GamePlayerActions.PromptPlayerDefineJokerCardAction -> "${action.name}: ${action.message}"
        is GamePlayerActions.PromptPlayerHitBackOrPick -> "${action.name}: ${action.message}"
        is GamePlayerActions.PromptPlayerPlayOrPick -> "${action.name}: ${action.message}"
    }
}

fun promptUserHitBackOrPickMessage(cardsToPick: Int): String {
    return "${CardStatesDefaultMsg.HitBackOrPick} $cardsToPick cards"
}
