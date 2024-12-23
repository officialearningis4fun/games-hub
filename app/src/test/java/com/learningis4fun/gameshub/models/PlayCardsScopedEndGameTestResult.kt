package com.learningis4fun.gameshub.models

import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Game
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.CardStates
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.TrackingPlay
import com.learningis4fun.gameshub.crusade_cards.presentation.play_crusade_cards.PlayCardsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
data class PlayCardsScopedEndGameTestResult(
    val numOfLeftCards: Int,
    val deletedAllGameCards: Boolean,
    val game: Game,
    val cardState: CardStates,
    val trackingPlay: TrackingPlay,
    val playCardsEvents: PlayCardsViewModel.PlayCardsScreenEvents
)
