package com.learningis4fun.gameshub.presentation.start

data class CardsStartState(
    val completedGamesState: CompletedGamesState = CompletedGamesState(),
    val isLoading: Boolean = true,
)
