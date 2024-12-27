package com.learningis4fun.gameshub.app

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.navigation
import com.learningis4fun.gameshub.commons.ui.routes.CoreNavGraphRoute
import com.learningis4fun.gameshub.commons.ui.routes.PlayCardsScreenRoute
import com.learningis4fun.gameshub.commons.ui.routes.ProfileGraph
import com.learningis4fun.gameshub.commons.ui.routes.StartScreenRoute
import com.learningis4fun.gameshub.commons.ui.routes.WinnerScreenRoute
import com.learningis4fun.gameshub.presentation.game_winner.playedGameWinnerComposable
import com.learningis4fun.gameshub.presentation.play_crusade_cards.playCardsScreenComposable
import com.learningis4fun.gameshub.presentation.start.cardsStartScreenComposable

fun NavGraphBuilder.coreGraph(
    navController: NavController
) {

    navigation<CoreNavGraphRoute>(startDestination = StartScreenRoute) {

        cardsStartScreenComposable(
            onStartClick = { gameId ->
                navController.navigate(PlayCardsScreenRoute(currentGameId = gameId))
            },
            onResumeClick = { gameId ->
                navController.navigate(PlayCardsScreenRoute(currentGameId = gameId))
            },
            onProfileClick = {
                navController.navigate(ProfileGraph)
            },
            onGameClick = { (game, player) ->
                navController.navigate(
                    WinnerScreenRoute(
                        wonGameId = game.id,
                        winnerImageUrl = player.imageUrl,
                        playerId = player.uniqueId
                    )
                )
            },
        )

        playedGameWinnerComposable(
            onStartGame = { gameId ->
                navController.navigate(PlayCardsScreenRoute(gameId)) {
                    popUpTo(StartScreenRoute)
                }
            },
            onResumeClick = { gameId ->
                navController.navigate(PlayCardsScreenRoute(currentGameId = gameId))
            },
            onCloseClick = {
                navController.navigateUp()
            },
            onStartNextStage = { gameId ->
                navController.navigate(PlayCardsScreenRoute(gameId)) {
                    popUpTo(StartScreenRoute)
                }
            }
        )

        playCardsScreenComposable(
            onBackClick = {
                navController.navigateUp()
            },
            onProfileClick = {
                navController.navigate(ProfileGraph)
            },
            navigateToWinner = { route ->
                navController.navigate(
                    WinnerScreenRoute(
                        wonGameId = route.wonGameId,
                        winnerImageUrl = route.winnerImageUrl,
                        playerId = route.playerId
                    )
                ) {
                    popUpTo<StartScreenRoute> {
                        inclusive = false
                    }
                }
            }
        )
    }
}