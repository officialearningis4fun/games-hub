@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.learningis4fun.gameshub.app

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.learningis4fun.gameshub.app.nav.BottonNavRoute
import com.learningis4fun.gameshub.commons.ui.routes.AddParticipantsRoute
import com.learningis4fun.gameshub.commons.ui.routes.CoreNavGraphRoute
import com.learningis4fun.gameshub.commons.ui.routes.EditProfileRoute
import com.learningis4fun.gameshub.commons.ui.routes.PlayCardsScreenRoute
import com.learningis4fun.gameshub.commons.ui.routes.ProfileGraph
import com.learningis4fun.gameshub.commons.ui.routes.StartScreenRoute
import com.learningis4fun.gameshub.commons.ui.routes.WinnerScreenRoute
import com.learningis4fun.gameshub.presentation.game_winner.playedGameWinnerComposable
import com.learningis4fun.gameshub.presentation.play_crusade_cards.playCardsScreenComposable
import com.learningis4fun.gameshub.presentation.profile.editProfileScreenComposable
import com.learningis4fun.gameshub.presentation.profile.profileScreenComposable
import com.learningis4fun.gameshub.presentation.start.cardsStartScreenComposable
import com.learningis4fun.gameshub.presentation.start.friends.addParticipantsScreenComposable
import com.learningis4fun.gameshub.presentation.start.friends.friendsScreenComposable
import com.learningis4fun.gameshub.presentation.util.GamesHubTransitionScope


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NavigationRoot(
    modifier: Modifier,
    navController: NavHostController,
    signInRequest: () -> Unit
) {

    SharedTransitionLayout {
        GamesHubTransitionScope(transitionScope = this) {
            NavHost(
                modifier = modifier,
                navController = navController,
                startDestination = CoreNavGraphRoute
            ) {
                navigation<CoreNavGraphRoute>(startDestination = BottonNavRoute.HomeScreenRoute) {
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
                    friendsScreenComposable(
                        onResumeClick = { gameId ->
                            navController.navigate(PlayCardsScreenRoute(currentGameId = gameId))
                        },
                        onProfileClick = {
                            navController.navigate(ProfileGraph)
                        },
                        onAddParticipants = {
                            navController.navigate(AddParticipantsRoute)
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
                    profileScreenComposable(
                        onBackClick = {
                            navController.navigateUp()
                        },
                        onEditProfile = {
                            navController.navigate(EditProfileRoute)
                        },
                        signInRequest = signInRequest
                    )
                }

                addParticipantsScreenComposable(
                    onNavBackClick = {
                        navController.navigateUp()
                    }
                )

                editProfileScreenComposable(
                    onBackClick = {
                        navController.navigateUp()
                    },
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
            }
        }
    }

}