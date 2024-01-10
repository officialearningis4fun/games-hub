package com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.repository

import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.ActiveGame
import kotlinx.coroutines.flow.Flow

interface GameSocketService {

    fun getActiveGame(): Flow<ActiveGame>
}