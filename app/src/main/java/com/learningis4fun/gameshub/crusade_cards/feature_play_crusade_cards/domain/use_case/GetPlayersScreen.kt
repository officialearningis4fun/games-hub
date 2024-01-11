package com.learningis4fun.gameshub.crusade_cards.feature_play_crusade_cards.domain.use_case

import com.learningis4fun.gameshub.crusade_cards.feature_play_crusade_cards.domain.model.PlayerScreenInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetPlayersScreen {

    operator fun invoke(): Flow<PlayerScreenInfo> = flow {  }
}