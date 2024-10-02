package com.learningis4fun.gameshub.crusade_cards.start.presentation

import com.learningis4fun.gameshub.crusade_cards.start.domain.model.CompletedGame

data class CompletedGamesState(
    val completedGames: List<CompletedGame> = emptyList(),
    val userId: String = ""
) {
    val gamesWon = completedGames.count { it.game.winnerId == userId }
    val totalGames = completedGames.size

}
