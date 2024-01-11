package com.learningis4fun.gameshub.crusade_cards.feature_play_crusade_cards.domain.use_case

data class PlayCardsUseCases(
    val getGameDuration: GetGameDuration,
    val getUserCards: GetUserCards,
    val getGamePlayerAction: GetGamePlayerAction,
    val getPlayersScreen: GetPlayersScreen
)
