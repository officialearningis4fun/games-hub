package com.learningis4fun.gameshub.models

data class CreateGameAndAssignCardsTestResult(
    val numOfGameCards : Int,
    val gameIdNotEmpty : Boolean,
    val numOfUserCards: Int
)
