package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Card
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.GameCard

data class GameCardWithCard(
    @Embedded val gameCard: GameCard,
    @Relation(
        parentColumn = "cardId",
        entityColumn = "cardId"
    )
    val card: Card
)
