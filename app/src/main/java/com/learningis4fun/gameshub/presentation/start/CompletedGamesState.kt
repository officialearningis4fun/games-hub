package com.learningis4fun.gameshub.presentation.start

data class CompletedGamesState(
    val completedGames: List<com.learningis4fun.gameshub.domain.model.CompletedGame> = emptyList(),
    val userId: String = "",
    val isLoading: Boolean = true
) {
    val gamesWon = completedGames.count { it.game.winnerId == userId }
    val totalGames = completedGames.size

}
