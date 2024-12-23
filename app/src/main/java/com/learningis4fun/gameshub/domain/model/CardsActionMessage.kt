package com.learningis4fun.gameshub.domain.model

data class CardsActionMessage(
    val message : String,
    val animate : Boolean = false
)
