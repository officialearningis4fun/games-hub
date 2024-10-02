package com.learningis4fun.gameshub.crusade_cards.start.domain.model

import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Game
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player

data class CompletedGame(
    val duration: Long = 0L,
    val game: Game = Game(),
    val players: List<Player> = emptyList(),
) {

    val winner = players.find { it.uniqueId == game.winnerId }
    val otherPlayer = players.firstOrNull { it.uniqueId != game.winnerId }

}
