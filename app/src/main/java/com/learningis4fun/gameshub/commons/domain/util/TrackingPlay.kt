package com.learningis4fun.gameshub.commons.domain.util

import com.learningis4fun.gameshub.commons.domain.model.Player

sealed class TrackingPlay{
    data class Error(val msg:String = "") : TrackingPlay()
    data class HasWinner(val player: Player, val gameId: String) : TrackingPlay()
    data object ContinuePlaying : TrackingPlay()
}

