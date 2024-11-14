package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Card
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.GameCard
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.UserCard

data class GameCardWithCard(
    @Embedded val gameCard: GameCard,
    @Relation(
        parentColumn = "cardId",
        entityColumn = "cardId"
    )
    val card: Card
) {
    fun toUserCard() = UserCard(
        gameCardId = gameCard.id,
        playerId = gameCard.playerId,
        cardId = card.id,
        name = card.name,
        type = card.type,
        image = card.image,
        stageNumber = gameCard.stageNumber,
        gameId = gameCard.gameId,
        isPlayed = gameCard.isPlayed,
        playTime = gameCard.playTime,
        pickedTime = gameCard.pickedTime
    )
}
