package com.learningis4fun.gameshub.crusade_cards.feature_start.presentation

data class CardsStartState(
    val completedGamesState: CompletedGamesState = CompletedGamesState(),
    val showOngoingGame: Boolean = false,
    val ongoingGamesState: OngoingGamesState = OngoingGamesState(),
    val isLoading: Boolean = true
)
