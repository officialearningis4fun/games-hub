package com.learningis4fun.gameshub.crusade_cards.start.domain.use_cases

import com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.remote.GamesHubAPI
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Card
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import javax.inject.Inject

class GetAllCardsImpl @Inject constructor(
    private val gamesHubAPI: GamesHubAPI
) : GetAllCards{

    override suspend fun getAllCards(): Response<List<Card>> {
        return try {
            val getAllCards = gamesHubAPI.getAllCards().map { it.toCard() }
            Response.Success(data = getAllCards, message = "")
        } catch (e: Exception) {
            Response.Error(
                data = emptyList(),
                message = e.message.toString()
            )
        }
    }
}