@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.learningis4fun.gameshub

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.learningis4fun.gameshub.commons.ui.routes.EditProfileRoute
import com.learningis4fun.gameshub.commons.ui.routes.PlayCardsScreenRoute
import com.learningis4fun.gameshub.commons.ui.routes.ProfileScreenRoute
import com.learningis4fun.gameshub.commons.ui.routes.StartScreenRoute
import com.learningis4fun.gameshub.commons.ui.routes.WinnerScreenRoute
import com.learningis4fun.gameshub.presentation.game_winner.playedGameWinnerComposable
import com.learningis4fun.gameshub.presentation.play_crusade_cards.playCardsScreenComposable
import com.learningis4fun.gameshub.presentation.profile.editProfileScreenComposable
import com.learningis4fun.gameshub.presentation.profile.profileScreenComposable
import com.learningis4fun.gameshub.presentation.start.cardsStartScreenComposable
import com.learningis4fun.gameshub.presentation.util.GamesHubTransitionScope


@Composable
fun NavigationRoot() {
    val navController = rememberNavController()

    SharedTransitionLayout {
        GamesHubTransitionScope(transitionScope = this) {
            NavHost(
                modifier = Modifier.background(Color.White),
                navController = navController,
                startDestination = StartScreenRoute
            ) {

                cardsStartScreenComposable(

                    onStartClick = { gameId ->
                        navController.navigate(PlayCardsScreenRoute(currentGameId = gameId))
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
                        navController.navigate(ProfileScreenRoute)
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

}