package com.learningis4fun.gameshub.data.data_source.remote

import io.ktor.client.HttpClient

class CrusadeGameServiceImpl(
    val client: HttpClient
) {

    /*override suspend fun playCard(playerId: String, cardOfGameDto: CardOfGameDto): Response<Unit> {
        return try {
            Response.Success(data = Unit)
        }catch (e: Exception){
            Response.Error(
                data = Unit,
                message = e.message.toString()
            )
        }
    }

    override suspend fun pickCards(playerId: String, gameId: String): Response<Unit> {
        return try {
            Response.Success(data = Unit, message = "")
        }catch (e: Exception){
            Response.Error(
                data = Unit,
                message = e.message.toString()
            )
        }
    }

    override suspend fun allowOthersPlay(playerId: String, gameId: String): Response<Unit> {
        return try {
            Response.Success(data = Unit, message = "")
        }catch (e: Exception){
            Response.Error(
                data = Unit,
                message = e.message.toString()
            )
        }
    }

    override suspend fun defineJokerAction(
        playerId: String,
        gameId: String,
        action: String
    ): Response<Unit> {
        return try {
            Response.Success(data = Unit, message = "")
        }catch (e: Exception){
            Response.Error(
                data = Unit,
                message = e.message.toString()
            )
        }
    }

    override suspend fun changeCardType(
        playerId: String,
        gameId: String,
        type: String
    ): Response<Unit> {
        return try {
            Response.Success(data = Unit, message = "")
        }catch (e: Exception){
            Response.Error(
                data = Unit,
                message = e.message.toString()
            )
        }
    }*/
}