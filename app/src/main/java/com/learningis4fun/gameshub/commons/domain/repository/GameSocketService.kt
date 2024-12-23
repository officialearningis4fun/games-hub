package com.learningis4fun.gameshub.commons.domain.repository

import com.learningis4fun.gameshub.commons.domain.model.ActiveGame
import kotlinx.coroutines.flow.Flow

interface GameSocketService {

    fun getActiveGame(): Flow<ActiveGame>
}