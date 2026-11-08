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

fun <T> Response<T>.getNullableDataOrThrowException(): T? {
    return when (this) {
        is Response.Error -> throw Exception(this.message)
        is Response.Success -> data
    }
}

object GamesHubLogTags {
    const val UTIL = "util"
    const val MULTIPLAYER_CRUSADE_CARDS_CONTROLLER = "MultiplayerController"
    const val ONLINE_CRUSADE_CARDS_CONTROLLER = "OnlineController"
    const val USER_CRUSADE_CARDS_CONTROLLER = "UserController"
    const val GAME_REPOSITORY = "gameRepository"
    const val GAME_CARD_REPOSITORY = "gameCardRepository"
    const val HANDLE_CARD_STATE_USE_CASE = "handleCardStateUseCase"
    const val PLAY_CARDS_VIEW_MODEL = "playCardsViewModel"
    const val PLAY_ONLINE_GAME_WINNER_VIEW_MODEL = "playOnlineGameWinnerVM"
    const val GET_LATEST_UN_PLAYED_CARDS = "getLatestUnPlayedCards"
    const val GET_LATEST_GAME_STAGE_PLAYER = "latestGameStagePlayer"
    const val CREATE_NEW_GAME_VIEWMODEL = "createNewGameViewModel"
    const val OPENAI_CARDS_CONTROLLER = "openaiCardsController"
    const val CRUSADE_PREFERENCES = "preferences"
}

object FirebaseLog {
    object LogName {
        const val OPEN_AI = "open_ai"
        const val NEW_GAME_WITH_OPENAI_CREATED = "new_game_with_open_ai_created"
        const val NEW_OFFLINE_GAME_CREATED = "new_offline_game_created"
    }

    object LogTag {
        const val SESSION_ID = "session_id"
    }
}