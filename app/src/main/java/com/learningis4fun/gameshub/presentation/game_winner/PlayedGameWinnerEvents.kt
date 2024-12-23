package com.learningis4fun.gameshub.presentation.game_winner

sealed interface PlayedGameWinnerEvents {
    data object IncreaseNumberOfCards : PlayedGameWinnerEvents
    data object DecreaseNumberOfCards : PlayedGameWinnerEvents
    data class DeleteGame(val gameId: String): PlayedGameWinnerEvents
    data class PlayerProfileClick(val playerId: String): PlayedGameWinnerEvents
}