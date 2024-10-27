package com.learningis4fun.gameshub.crusade_cards.feature_start.presentation

import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants

data class CardsStartState(
    val completedGamesState: CompletedGamesState = CompletedGamesState(),
    val showOngoingGame: Boolean = false,
    val ongoingGamesState: OngoingGamesState = OngoingGamesState(),
    val isLoading: Boolean = true,
    val numberOfCardsPerPlayer: Int = Constants.NumberOfCardsOptions.DEFAULT_CARDS_PER_PLAYER
)
