package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.use_case

import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.CardOfGame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetUserCards {

   operator fun invoke(): Flow<List<CardOfGame>>{
        return flow {  }
    }
}