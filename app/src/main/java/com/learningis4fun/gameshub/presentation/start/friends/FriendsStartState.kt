package com.learningis4fun.gameshub.presentation.start.friends

import com.learningis4fun.gameshub.presentation.start.CompletedGamesState
import com.learningis4fun.gameshub.presentation.start.OnGoingGame

data class FriendsStartState(
    val completedGamesState: CompletedGamesState = CompletedGamesState(),
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed interface OngoingGamesOrCreateNewGameState {
    data object Loading : OngoingGamesOrCreateNewGameState
    data object Nothing : OngoingGamesOrCreateNewGameState
    data object CreateNewGameToolsState : OngoingGamesOrCreateNewGameState
    data class OngoingGamesState(
        val games: List<OnGoingGame> = emptyList(),
    ) : OngoingGamesOrCreateNewGameState
}
