package com.learningis4fun.gameshub.api.use_cases

import com.learningis4fun.gameshub.commons.domain.model.CardOfGame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetUserCards {

    operator fun invoke(): Flow<List<CardOfGame>> {
        return flow { }
    }
}