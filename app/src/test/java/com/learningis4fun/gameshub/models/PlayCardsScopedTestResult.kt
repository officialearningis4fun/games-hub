package com.learningis4fun.gameshub.models

import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.CardStates
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.TrackingPlay

data class PlayCardsScopedTestResult(
    val tracking : TrackingPlay,
    val cardState : CardStates,
    val numOfPlayedCardsPerUser : Int = 1
)
