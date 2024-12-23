package com.learningis4fun.gameshub.commons.ui.routes

import kotlinx.serialization.Serializable

@Serializable
data object StartScreenRoute

@Serializable
data class PlayCardsScreenRoute(val currentGameId: String)

@Serializable
data object ProfileScreenRoute

@Serializable
data object EditProfileRoute

@Serializable
data class WinnerScreenRoute(val wonGameId: String, val winnerImageUrl: String, val playerId: String)


