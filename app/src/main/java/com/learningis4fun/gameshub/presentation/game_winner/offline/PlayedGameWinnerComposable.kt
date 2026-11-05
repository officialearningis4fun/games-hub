package com.learningis4fun.gameshub.presentation.game_winner.offline

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.commons.ui.routes.WinnerScreenRoute
import com.learningis4fun.gameshub.presentation.game_winner.PlayedGameScreen
import com.learningis4fun.gameshub.presentation.util.GamesHubAnimatedContentScope
import com.learningis4fun.gameshub.util.ScaleTransitionDirection
import com.learningis4fun.gameshub.util.scaleIntoContainer

fun NavGraphBuilder.playedGameWinnerComposable(
    onCloseClick: () -> Unit,
    onStartNextStage: (String) -> Unit
) {

    composable<WinnerScreenRoute>(
        enterTransition = {
            scaleIntoContainer()
        },
        popEnterTransition = {
            scaleIntoContainer(direction = ScaleTransitionDirection.INWARDS)
        }
    ) { backStateEntry ->

        val wonGameId = backStateEntry.toRoute<WinnerScreenRoute>().wonGameId

        val playerId = backStateEntry.toRoute<WinnerScreenRoute>().playerId

        val winnerImageRes =
            backStateEntry.toRoute<WinnerScreenRoute>().winnerImageUrl.toIntOrNull()
                ?: R.drawable.game_test_profile

        val viewModel: PlayedGameWinnerViewModel = hiltViewModel()
        val playedGameState by viewModel.playedGameState.collectAsStateWithLifecycle()

        GamesHubAnimatedContentScope(
            animatedContentScope = this@composable
        ) {
            PlayedGameScreen(
                modifier = Modifier
                    .fillMaxSize(),
                playedGameWinnerOrNextStage = playedGameState,
                winnerImageRes = winnerImageRes,
                wonGameId = wonGameId,
                playerId = playerId,
                onStartNextStage = { onStartNextStage(wonGameId) },
                onCloseClick = onCloseClick,
            )
        }

    }
}