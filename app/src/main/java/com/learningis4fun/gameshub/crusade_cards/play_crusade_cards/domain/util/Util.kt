package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.util

import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response

fun <T> Response<T>.getDataOrThrowException(): T {
    return when (this) {
        is Response.Error -> throw Exception(this.message)
        is Response.Success -> if(this.data == null) {
            throw Exception("Data not found")
        } else data
    }
}

object GamesHubLogTags{
    const val MULTIPLAYER_CRUSADE_CARDS_CONTROLLER = "MultiplayerController"
    const val GAME_REPOSITORY = "gameRepository"
    const val GAME_CARD_REPOSITORY = "gameCardRepository"
}