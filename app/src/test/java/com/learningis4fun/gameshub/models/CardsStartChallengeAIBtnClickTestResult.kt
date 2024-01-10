package com.learningis4fun.gameshub.models

import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.CardStates
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.TrackingPlay
import com.learningis4fun.gameshub.feature_crusade_cards.feature_start.CardsStartViewModel

data class CardsStartChallengeAIBtnClickTestResult(
    val screenEvent: CardsStartViewModel.CardsStartScreenEvents,
    val isGameIdEmpty: Boolean,
    val hasAssigedCards: Boolean
)
