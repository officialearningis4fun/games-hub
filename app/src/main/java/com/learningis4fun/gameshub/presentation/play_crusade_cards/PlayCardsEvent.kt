package com.learningis4fun.gameshub.presentation.play_crusade_cards

import com.learningis4fun.gameshub.commons.domain.util.JokerActions
import com.learningis4fun.gameshub.domain.model.UserCard

sealed interface PlayCardsEvent {
    data class PlayCard(val userCard: UserCard) : PlayCardsEvent
    data class PlayedCard(val userCard: UserCard?): PlayCardsEvent
    data class SetNewCardType(val type: String) : PlayCardsEvent
    data class DefineJokerAction(val action: JokerActions) : PlayCardsEvent
    data object PickCardFromDustbin : PlayCardsEvent
    data object ClosePlayCardsScreen : PlayCardsEvent
    data object ScrollToItemPositionConsumed : PlayCardsEvent
    data object AllowOthersPlay : PlayCardsEvent
    data class ErrorButtonClick(val errorAction: PlayCardsErrorAction) : PlayCardsEvent
    data object DismissErrorClick : PlayCardsEvent
}
