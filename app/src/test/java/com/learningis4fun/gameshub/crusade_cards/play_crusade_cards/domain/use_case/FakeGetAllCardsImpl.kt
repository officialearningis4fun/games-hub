package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case

import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Card
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response
import com.learningis4fun.gameshub.crusade_cards.start.domain.use_cases.GetAllCards

class FakeGetAllCardsImpl(

): GetAllCards {
    override suspend fun getAllCards(): Response<List<Card>> {

        return Response.Success(data = Constants.Cards.cards, message = "No message")
    }


}