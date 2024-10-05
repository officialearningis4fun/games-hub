package com.learningis4fun.gameshub.crusade_cards.commons.ui.routes

import kotlinx.serialization.Serializable

@Serializable
data object StartScreenRoute

@Serializable
data class PlayCardsScreenRoute(val currentGameId: String = "")

@Serializable
data object ProfileScreenRoute

@Serializable
data class WinnerScreenRoute(val wonGameId: String)