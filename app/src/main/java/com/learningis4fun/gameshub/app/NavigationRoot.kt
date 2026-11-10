@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.learningis4fun.gameshub.app

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.navigation
import com.learningis4fun.gameshub.app.nav.BottonNavRoute
import com.learningis4fun.gameshub.commons.ui.routes.AddParticipantsRoute
import com.learningis4fun.gameshub.commons.ui.routes.CoreNavGraph
import com.learningis4fun.gameshub.commons.ui.routes.CreateGameGraph
import com.learningis4fun.gameshub.commons.ui.routes.CreateOnlineGameRoute
import com.learningis4fun.gameshub.commons.ui.routes.EditProfileRoute
import com.learningis4fun.gameshub.commons.ui.routes.OnlineWinnerScreenRoute
import com.learningis4fun.gameshub.commons.ui.routes.PlayCardsOnlineScreenRoute
import com.learningis4fun.gameshub.commons.ui.routes.PlayCardsScreenRoute
import com.learningis4fun.gameshub.commons.ui.routes.StartScreenRoute
import com.learningis4fun.gameshub.commons.ui.routes.WinnerScreenRoute
import com.learningis4fun.gameshub.presentation.create_game.online.addParticipantsScreenComposable
import com.learningis4fun.gameshub.presentation.create_game.online.createOnlineGameScreenComposable
import com.learningis4fun.gameshub.presentation.game_winner.offline.playedGameWinnerComposable
import com.learningis4fun.gameshub.presentation.game_winner.online.playedOnlineGameWinnerComposable
import com.learningis4fun.gameshub.presentation.play_crusade_cards.offline.playCardsScreenComposable
import com.learningis4fun.gameshub.presentation.play_crusade_cards.online.playCardsOnlineScreenComposable
import com.learningis4fun.gameshub.presentation.profile.editProfileScreenComposable
import com.learningis4fun.gameshub.presentation.profile.profileScreenComposable
import com.learningis4fun.gameshub.presentation.start.cardsStartScreenComposable
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
                startDestination = CoreNavGraph,
            ) {
                navigation<CoreNavGraph>(startDestination = BottonNavRoute.HomeScreenRoute) {

                    cardsStartScreenComposable(
                        onStartClick = { gameId ->
                            navController.navigate(PlayCardsScreenRoute(currentGameId = gameId))
                        },
                        onResumeClick = { gameId ->
                            navController.navigate(PlayCardsScreenRoute(currentGameId = gameId))
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
                        navController = navController,
                        onResumeClick = { gameId ->
                            navController.navigate(PlayCardsOnlineScreenRoute(currentGameId = gameId))
                        },
                        onCreateClick = {
                            navController.navigate(CreateGameGraph)
                        },
                        onGameClick = { (game, player) ->
                            navController.navigate(
                                OnlineWinnerScreenRoute(
                                    wonGameId = game.id,
                                    winnerImageUrl = player.imageUrl,
                                    playerId = player.uniqueId
                                )
                            )
                        },
                    )

                    profileScreenComposable(
                        onEditProfile = {
                            navController.navigate(EditProfileRoute)
                        },
                        signInRequest = signInRequest
                    )
                }

                navigation<CreateGameGraph>(startDestination = CreateOnlineGameRoute){
                    createOnlineGameScreenComposable(
                        navController = navController,
                        onClose = {
                            navController.navigateUp()
                        },
                        onAddParticipants = {
                            navController.navigate(AddParticipantsRoute)
                        },
                        onGameCreated = {gameId ->
                            navController.navigate(
                                PlayCardsOnlineScreenRoute(
                                    currentGameId = gameId
                                )
                            ) {
                                popUpTo<BottonNavRoute.FriendsScreenRoute> { inclusive = false }
                            }
                        }
                    )

                    addParticipantsScreenComposable(
                        navController = navController,
                        onNavBackClick = {
                            navController.navigateUp()
                        }
                    )
                }

                editProfileScreenComposable(
                    onBackClick = {
                        navController.navigateUp()
                    },
                )
                playCardsScreenComposable(
                    onBackClick = {
                        navController.navigateUp()
                    },
                    navigateToWinner = { route ->
                        navController.navigate(
                            WinnerScreenRoute(
                                wonGameId = route.wonGameId,
                                winnerImageUrl = route.winnerImageUrl,
                                playerId = route.playerId
                            )
                        ) {
                            popUpTo<BottonNavRoute.HomeScreenRoute> {
                                inclusive = false
                            }
                        }
                    }
                )

                playCardsOnlineScreenComposable(
                    onBackClick = {
                        navController.navigateUp()
                    },
                    navigateToWinner = { route ->
                        navController.navigate(
                            OnlineWinnerScreenRoute(
                                wonGameId = route.wonGameId,
                                winnerImageUrl = route.winnerImageUrl,
                                playerId = route.playerId
                            )
                        ) {
                            popUpTo<BottonNavRoute.FriendsScreenRoute> {
                                inclusive = false
                            }
                        }
                    }
                )

                playedGameWinnerComposable(
                    onCloseClick = {
                        navController.navigateUp()
                    },
                    onStartNextStage = { gameId ->
                        navController.navigate(PlayCardsScreenRoute(gameId)) {
                            popUpTo(BottonNavRoute.HomeScreenRoute)
                        }
                    }
                )

                playedOnlineGameWinnerComposable(
                    onCloseClick = {
                        navController.navigateUp()
                    },
                    onStartNextStage = { gameId ->
                        navController.navigate(PlayCardsOnlineScreenRoute(gameId)) {
                            popUpTo(BottonNavRoute.FriendsScreenRoute)
                        }
                    }
                )
            }
        }
    }

}