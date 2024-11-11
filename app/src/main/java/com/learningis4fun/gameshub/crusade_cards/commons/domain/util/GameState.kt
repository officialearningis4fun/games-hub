package com.learningis4fun.gameshub.crusade_cards.commons.domain.util

sealed class GameState(val name: String) {
    data object NotStarted : GameState("not-started")
    data object Started : GameState("started")
    data object Paused : GameState("paused")
    data object Completed : GameState("completed")
}

fun toState(name: String) =
    when (name) {
        GameState.NotStarted.name -> GameState.NotStarted
        GameState.Started.name -> GameState.Started
        GameState.Paused.name -> GameState.Paused
        GameState.Completed.name -> GameState.Completed
        else -> GameState.NotStarted
    }