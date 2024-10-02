package com.learningis4fun.gameshub.crusade_cards.start.presentation

import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Card
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Game
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.relations.GameWithPlayers

data class OngoingGameState(
    val threeCards: List<Card> = emptyList(),
    val gameWithPlayers: GameWithPlayers = GameWithPlayers(
        game = Game(),
        players = emptyList()
    )
)
