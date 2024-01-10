package com.learningis4fun.gameshub.feature_crusade_cards.feature_start.domain.use_cases

import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.Card
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.Response

interface GetAllCards {

    suspend fun getAllCards(): Response<List<Card>>
}