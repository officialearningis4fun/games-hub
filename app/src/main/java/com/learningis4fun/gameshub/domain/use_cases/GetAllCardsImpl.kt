package com.learningis4fun.gameshub.domain.use_cases

import com.learningis4fun.gameshub.data.data_source.remote.GamesHubAPI
import com.learningis4fun.gameshub.commons.domain.model.Card
import com.learningis4fun.gameshub.commons.domain.util.Response
import javax.inject.Inject

class GetAllCardsImpl @Inject constructor(
    private val gamesHubAPI: GamesHubAPI
) : GetAllCards {

    override suspend fun getAllCards(): Response<List<Card>> {
        return try {
            val getAllCards = gamesHubAPI.getAllCards().map { it.toCard() }
            Response.Success(data = getAllCards)
        } catch (e: Exception) {
            Response.Error(
                message = e.message.toString()
            )
        }
    }
}