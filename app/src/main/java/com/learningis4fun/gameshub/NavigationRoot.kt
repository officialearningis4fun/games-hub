package com.learningis4fun.gameshub

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.learningis4fun.gameshub.crusade_cards.commons.ui.routes.PlayCardsScreenRoute
import com.learningis4fun.gameshub.crusade_cards.commons.ui.routes.ProfileScreenRoute
import com.learningis4fun.gameshub.crusade_cards.commons.ui.routes.StartScreenRoute
import com.learningis4fun.gameshub.crusade_cards.commons.ui.routes.WinnerScreenRoute
import com.learningis4fun.gameshub.crusade_cards.feature_game_winner.presentation.playedGameWinnerComposable
import com.learningis4fun.gameshub.crusade_cards.feature_profile.presentation.profile.profileScreenComposable
import com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.cardsStartScreenComposable
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.presentation.playCardsScreenComposable


@Composable
fun NavigationRoot() {
    val navController = rememberNavController()


    NavHost(navController = navController, startDestination = StartScreenRoute) {

        cardsStartScreenComposable(
            onStartClick = {
                navController.navigate(PlayCardsScreenRoute(currentGameId = ""))
            },
            onResumeClick = { gameId ->
                navController.navigate(PlayCardsScreenRoute(currentGameId = gameId))
            },
            onProfileClick = {
                navController.navigate(ProfileScreenRoute)
            },
            onGameClick = { gameId ->
                navController.navigate(WinnerScreenRoute(gameId))
            },
        )

        playCardsScreenComposable(
            onBackClick = {
                navController.navigateUp()
            },
            onProfileClick = {
                navController.navigate(ProfileScreenRoute)
            },
            navigateToWinner = { wonGameId ->
                navController.navigate(
                    WinnerScreenRoute(
                        wonGameId = wonGameId
                    )
                ) {
                    popUpTo<StartScreenRoute> {
                        inclusive = false
                    }
                }
            }
        )

        profileScreenComposable(
            onBackClick = {
                navController.navigateUp()
            }
        )

        playedGameWinnerComposable(
            onStartGame = {
                navController.navigate(PlayCardsScreenRoute(currentGameId = "")) {
                    popUpTo(StartScreenRoute)
                }
            },
            onCloseClick = {
                navController.navigateUp()
            }
        )

    }

}