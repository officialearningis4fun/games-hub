package com.learningis4fun.gameshub

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.learningis4fun.gameshub.crusade_cards.commons.ui.routes.EditProfileRoute
import com.learningis4fun.gameshub.crusade_cards.commons.ui.routes.PlayCardsScreenRoute
import com.learningis4fun.gameshub.crusade_cards.commons.ui.routes.ProfileScreenRoute
import com.learningis4fun.gameshub.crusade_cards.commons.ui.routes.StartScreenRoute
import com.learningis4fun.gameshub.crusade_cards.commons.ui.routes.WinnerScreenRoute
import com.learningis4fun.gameshub.crusade_cards.feature_game_winner.presentation.playedGameWinnerComposable
import com.learningis4fun.gameshub.crusade_cards.feature_profile.presentation.profile.editProfileScreenComposable
import com.learningis4fun.gameshub.crusade_cards.feature_profile.presentation.profile.profileScreenComposable
import com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.cardsStartScreenComposable
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.presentation.playCardsScreenComposable


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NavigationRoot() {
    val navController = rememberNavController()

    SharedTransitionLayout {
        NavHost(
            modifier = Modifier.background(Color.White),
            navController = navController,
            startDestination = StartScreenRoute
        ) {

            cardsStartScreenComposable(
                sharedTransitionScope = this@SharedTransitionLayout,
                onStartClick = {
                    navController.navigate(PlayCardsScreenRoute(currentGameId = ""))
                },
                onResumeClick = { gameId ->
                    navController.navigate(PlayCardsScreenRoute(currentGameId = gameId))
                },
                onProfileClick = {
                    navController.navigate(ProfileScreenRoute)
                },
                onGameClick = { (game, player) ->
                    navController.navigate(
                        WinnerScreenRoute(
                            wonGameId = game.id,
                            winnerImageUrl = player.imageUrl
                        )
                    )
                },
            )

            playedGameWinnerComposable(
                sharedTransitionScope = this@SharedTransitionLayout,
                onStartGame = {
                    navController.navigate(PlayCardsScreenRoute()) {
                        popUpTo(StartScreenRoute)
                    }
                },
                onCloseClick = {
                    navController.navigateUp()
                },
                onStartNextStage = {
                    navController.navigate(PlayCardsScreenRoute()) {
                        popUpTo(StartScreenRoute)
                    }
                }
            )

            playCardsScreenComposable(
                onBackClick = {
                    navController.navigateUp()
                },
                onProfileClick = {
                    navController.navigate(ProfileScreenRoute)
                },
                navigateToWinner = { route ->
                    navController.navigate(
                        WinnerScreenRoute(
                            wonGameId = route.wonGameId,
                            winnerImageUrl = route.winnerImageUrl
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
                },
                onEditProfile = {
                    navController.navigate(EditProfileRoute)
                }
            )

            editProfileScreenComposable(
                onBackClick = {
                    navController.navigateUp()
                }
            )

        }
    }


}