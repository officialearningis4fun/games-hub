package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Game
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.GameUserCrossRef

data class GameWithPlayers(
    @Embedded
    val game: Game,
    @Relation(
        parentColumn = "gameId",
        entityColumn = "uniqueId",
        associateBy = Junction(GameUserCrossRef::class)
    )
    val players: List<Player>
)
