package com.learningis4fun.gameshub.domain.model

data class UserCardScrollInfo(
    val shouldScroll : Boolean = false,
    val scrollPosition : Int = 0,
    val timeDifference : Long = 0L,
    val message : String = ""
)
