package com.learningis4fun.gameshub.api.use_cases

import com.learningis4fun.gameshub.commons.domain.model.Card
import com.learningis4fun.gameshub.commons.domain.util.Response

interface GetAllCards {

    suspend fun getAllCards(): Response<List<Card>>
}