package com.learningis4fun.gameshub.data.data_source.remote

import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.data.data_source.remote.dto.CardDto

interface GamesHubAPI {


    suspend fun signUpWithGoogle(player: Player)


    suspend fun getAllCards(): List<CardDto>


}