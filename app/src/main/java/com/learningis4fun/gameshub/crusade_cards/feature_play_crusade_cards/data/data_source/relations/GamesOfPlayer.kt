package com.learningis4fun.gameshub.crusade_cards.feature_play_crusade_cards.data.data_source.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Game
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.GameUserCrossRef
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player

data class GamesOfPlayer(
    @Embedded
    val player: Player,
    @Relation(
        parentColumn = "uniqueId",
        entityColumn = "gameId",
        associateBy = Junction(GameUserCrossRef::class)
    )
    val games: List<Game>
)
