package com.learningis4fun.gameshub.models

import com.learningis4fun.gameshub.crusade_cards.presentation.start.CardsStartViewModel

data class CardsStartChallengeAIBtnClickTestResult(
    val screenEvent: CardsStartViewModel.CardsStartScreenEvents,
    val isGameIdEmpty: Boolean,
    val hasAssigedCards: Boolean
)
