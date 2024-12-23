package com.learningis4fun.gameshub.data.data_source.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.learningis4fun.gameshub.commons.domain.model.Game
import com.learningis4fun.gameshub.commons.domain.model.GameUserCrossRef
import com.learningis4fun.gameshub.commons.domain.model.Player

data class GameWithPlayers(
    @Embedded
    val game: Game,
    @Relation(
        parentColumn = "gameId",
        entityColumn = "uniqueId",
        associateBy = Junction(GameUserCrossRef::class)
    )
    val players: List<Player>
) {
    fun toCompletedGame() = com.learningis4fun.gameshub.domain.model.CompletedGame(
        duration = game.gameDuration,
        game = game,
        players = players
    )
}
