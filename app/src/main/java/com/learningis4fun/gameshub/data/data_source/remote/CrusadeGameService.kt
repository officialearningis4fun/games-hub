package com.learningis4fun.gameshub.data.data_source.remote

import com.learningis4fun.gameshub.data.data_source.remote.dto.CardOfGameDto
import com.learningis4fun.gameshub.commons.domain.util.Response

interface CrusadeGameService {

    suspend fun playCard(playerId: String, cardOfGameDto: CardOfGameDto): Response<Unit>
    suspend fun pickCards(playerId: String, gameId: String): Response<Unit>
    suspend fun allowOthersPlay(playerId: String, gameId: String): Response<Unit>
    suspend fun defineJokerAction(playerId: String, gameId: String, action: String): Response<Unit>
    suspend fun changeCardType(playerId: String, gameId: String, type: String): Response<Unit>

    companion object {
        const val BASE_URL = "http://192.168.43.237:8080"
    }

    sealed class EndPoints(val url: String) {
        object PlayCard : EndPoints("")
        object PickCards : EndPoints("")
        object AllowOthersPlay : EndPoints("")
        object DefineJokerAction : EndPoints("")
        object ChangeCardType : EndPoints("")
    }
}