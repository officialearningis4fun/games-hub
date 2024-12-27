package com.learningis4fun.gameshub.commons.ui.routes

import kotlinx.serialization.Serializable

@Serializable
data object CoreNavGraphRoute

@Serializable
data object StartScreenRoute

@Serializable
data class PlayCardsScreenRoute(val currentGameId: String)

@Serializable
data class WinnerScreenRoute(val wonGameId: String, val winnerImageUrl: String, val playerId: String)