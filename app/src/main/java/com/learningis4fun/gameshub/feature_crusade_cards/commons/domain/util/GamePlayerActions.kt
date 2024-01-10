package com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model

import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.CardStatesDefaultMsg
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.util.TypeConverters

sealed class GamePlayerActions(val name: String) {

    object Default : GamePlayerActions("default")

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

sealed class GamePlayerActionsNames(val name: String) {
    object Default : GamePlayerActionsNames("default")
    object ActionMessages : GamePlayerActionsNames("messages")
    object PromptPlayerDefineJokerCardAction : GamePlayerActionsNames("define-joker-actions")
    object PromptPlayerHitBackOrPick : GamePlayerActionsNames("hit-back-or-pick")
    object PromptPlayerPlayOrPick : GamePlayerActionsNames("play-or-pick")
    object PromptPlayerChangeCardType : GamePlayerActionsNames("change-card-type")
}

fun toGamePlayerActions(typeConverters: TypeConverters, action: String, jsonContexts: String): GamePlayerActions? {

    return when (action) {
        GamePlayerActionsNames.Default.name -> GamePlayerActions.Default
        GamePlayerActionsNames.ActionMessages.name -> typeConverters.toActionMessages(jsonContexts)
        GamePlayerActionsNames.PromptPlayerDefineJokerCardAction.name -> typeConverters.toPromptPlayerDefineJokerAction(
            jsonContexts
        )
        GamePlayerActionsNames.PromptPlayerHitBackOrPick.name -> typeConverters.toPromptPlayerHitBack(jsonContexts)
        GamePlayerActionsNames.PromptPlayerPlayOrPick.name -> typeConverters.toPromptPlayerPlayOrPick(jsonContexts)
        GamePlayerActionsNames.PromptPlayerChangeCardType.name -> typeConverters.toPromptPlayerChangeCardType(jsonContexts)
        else -> GamePlayerActions.Default
    }
}


fun promptUserHitBackOrPickMessage(cardsToPick: Int): String {
    return "${CardStatesDefaultMsg.HitBackOrPick} $cardsToPick cards"
}
