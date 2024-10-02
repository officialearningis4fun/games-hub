package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model

data class UserCardScrollInfo(
    val shouldScroll : Boolean = false,
    val scrollPosition : Int = 0,
    val timeDifference : Long = 0L,
    val message : String = ""
)
