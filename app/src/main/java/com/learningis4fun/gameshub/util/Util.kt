package com.learningis4fun.gameshub.util

import com.learningis4fun.gameshub.commons.domain.util.Response

fun <T> Response<T>.getDataOrThrowException(): T {
    return when (this) {
        is Response.Error -> throw Exception(this.message)
        is Response.Success -> if (this.data == null) {
            throw Exception("Data not found")
        } else data
    }
}

object GamesHubLogTags {
    const val UTIL = "util"
    const val MULTIPLAYER_CRUSADE_CARDS_CONTROLLER = "MultiplayerController"
    const val GAME_REPOSITORY = "gameRepository"
    const val GAME_CARD_REPOSITORY = "gameCardRepository"
    const val HANDLE_CARD_STATE_USE_CASE = "handleCardStateUseCase"
    const val PLAY_CARDS_VIEW_MODEL = "playCardsViewModel"
    const val GET_LATEST_UN_PLAYED_CARDS = "getLatestUnPlayedCards"
    const val GET_LATEST_GAME_STAGE_PLAYER = "latestGameStagePlayer"
    const val CREATE_NEW_GAME_VIEWMODEL = "createNewGameViewModel"
    const val GEMINI_CARDS_CONTROLLER = "geminiCardsController"
}