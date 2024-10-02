package com.learningis4fun.gameshub.crusade_cards.start.presentation

data class CardsStartState(
    val completedGamesState: CompletedGamesState = CompletedGamesState(),
    val showOngoingGame: Boolean = false,
    val ongoingGameState: OngoingGameState = OngoingGameState()
)
