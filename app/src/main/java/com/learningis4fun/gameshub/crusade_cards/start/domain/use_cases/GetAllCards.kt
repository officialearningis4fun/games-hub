package com.learningis4fun.gameshub.crusade_cards.start.domain.use_cases

import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Card
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Response

interface GetAllCards {

    suspend fun getAllCards(): Response<List<Card>>
}