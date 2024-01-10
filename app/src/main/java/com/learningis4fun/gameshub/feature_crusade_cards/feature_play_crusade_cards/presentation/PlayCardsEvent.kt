package com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.presentation

import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.UserCard
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.JokerActions

sealed class PlayCardsEvent {
    data class PlayCard(val userCard: UserCard) : PlayCardsEvent()
    data class SetNewCardType(val type: String) : PlayCardsEvent()
    data class DefineJokerAction(val action: JokerActions) : PlayCardsEvent()
    object PickCardFromDustbin : PlayCardsEvent()
    object ClosePlayCardsScreen : PlayCardsEvent()
}
