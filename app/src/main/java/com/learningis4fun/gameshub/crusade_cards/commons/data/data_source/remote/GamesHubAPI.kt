package com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.remote

import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.remote.dto.CardDto
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface GamesHubAPI {

    @POST("/signup/google")
    suspend fun signUpWithGoogle(@Body player: Player)

    @GET("/crusade/cards")
    suspend fun getAllCards(): List<CardDto>


}