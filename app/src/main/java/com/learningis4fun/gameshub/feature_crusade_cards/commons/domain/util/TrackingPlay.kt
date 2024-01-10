package com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util

import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.Player

sealed class TrackingPlay{
    data class Error(val msg:String = "") : TrackingPlay()
    data class HasWinner(val player: Player) : TrackingPlay()
    object Playing : TrackingPlay()
    object ContinuePlaying : TrackingPlay()
}
