package com.learningis4fun.gameshub.presentation.start

data class CardsStartState(
    val completedGamesState: CompletedGamesState = CompletedGamesState(),
    val isLoading: Boolean = true,
    val ongoingGamesOrNewGameState: OngoingGamesOrNewGameState = OngoingGamesOrNewGameState.Loading
)

sealed interface OngoingGamesOrNewGameState {

    data object Loading : OngoingGamesOrNewGameState

    data class OngoingGamesState(
        val games: List<OnGoingGame> = emptyList(),
        val isLoading: Boolean = true
    ) : OngoingGamesOrNewGameState

    data object NewGameState : OngoingGamesOrNewGameState
}
