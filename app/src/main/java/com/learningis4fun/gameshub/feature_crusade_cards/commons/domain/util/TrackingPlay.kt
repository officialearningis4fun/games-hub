package com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util

sealed class TrackingPlay{
    data class Error(val msg:String = "") : TrackingPlay()
    object HasWinner : TrackingPlay()
    object Playing : TrackingPlay()
    object ContinuePlaying : TrackingPlay()
}
