package com.learningis4fun.gameshub.crusade_cards.feature_game_winner.presentation

sealed interface PlayedGameWinnerEvents {
    data object IncreaseNumberOfCards : PlayedGameWinnerEvents
    data object DecreaseNumberOfCards : PlayedGameWinnerEvents
    data class DeleteGame(val gameId: String): PlayedGameWinnerEvents
}