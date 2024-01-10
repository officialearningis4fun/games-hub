package com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util

sealed class GameState(val name: String) {
    object NotStarted : GameState("not-started")
    object Started : GameState("started")
    object Paused : GameState("paused")
    object Completed : GameState("completed")

}

fun toState(name: String) =
    when (name) {
        GameState.NotStarted.name -> GameState.NotStarted
        GameState.Started.name -> GameState.Started
        GameState.Paused.name -> GameState.Paused
        GameState.Completed.name -> GameState.Completed
        else -> GameState.NotStarted
    }
