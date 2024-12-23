package com.learningis4fun.gameshub.domain.model

import com.learningis4fun.gameshub.commons.domain.model.Game
import com.learningis4fun.gameshub.commons.domain.model.Player

data class CompletedGame(
    val duration: Long = 0L,
    val game: Game = Game(),
    val players: List<Player> = emptyList(),
) {

    val winner = players.find { it.uniqueId == game.winnerId }
    val otherPlayers = players.filterNot { it.uniqueId == game.winnerId }

}
