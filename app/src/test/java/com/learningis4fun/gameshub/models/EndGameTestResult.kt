package com.learningis4fun.gameshub.models

import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.CardStates
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.GamePlayerActions
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class EndGameTestResult @ExperimentalCoroutinesApi constructor(
    val gameCardsSize : Int,
    val gameIdEmpty : Boolean,
    val gameState : String,
    val cardState: CardStates,
    val gamePlayersSize : Int,
    val playCardsEvents: GamePlayerActions = GamePlayerActions.Default
)
