package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.mappers

import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.relations.GameCardWithCard
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.GameCard
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.UserCard

fun UserCard.toGameCard() = GameCard(
    id = this.gameCardId,
    gameId = this.gameId,
    cardId = this.cardId,
    playerId = this.playerId,
    isPlayed = this.isPlayed,
    playTime = this.playTime,
    pickedTime = this.pickedTime
)

fun GameCardWithCard.toUserCard() = UserCard(
    gameCardId = this.gameCard.id,
    playerId = this.gameCard.playerId,
    cardId = this.card.id,
    name = this.card.name,
    type = this.card.type,
    image = this.card.image,
    gameId = this.gameCard.gameId,
    isPlayed = this.gameCard.isPlayed,
    playTime = this.gameCard.playTime,
    pickedTime = this.gameCard.pickedTime
)