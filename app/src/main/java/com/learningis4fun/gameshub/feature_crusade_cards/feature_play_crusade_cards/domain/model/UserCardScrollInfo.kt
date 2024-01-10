package com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model

data class UserCardScrollInfo(
    val shouldScroll : Boolean = false,
    val scrollPosition : Int = 0,
    val timeDifference : Long = 0L,
    val message : String = ""
)
