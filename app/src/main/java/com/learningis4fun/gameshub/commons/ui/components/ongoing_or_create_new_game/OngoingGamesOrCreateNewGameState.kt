package com.learningis4fun.gameshub.commons.ui.components.ongoing_or_create_new_game

import com.learningis4fun.gameshub.presentation.start.OnGoingGame

sealed interface OngoingGamesOrCreateNewGameState {
    data object Loading : OngoingGamesOrCreateNewGameState
    data object Nothing : OngoingGamesOrCreateNewGameState
    data object CreateNewGameToolsState : OngoingGamesOrCreateNewGameState
    data class OngoingGamesState(
        val games: List<OnGoingGame> = emptyList(),
    ) : OngoingGamesOrCreateNewGameState
}
