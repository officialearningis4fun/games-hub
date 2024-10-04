package com.learningis4fun.gameshub

import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.learningis4fun.gameshub.crusade_cards.commons.ui.routes.PlayCardsScreenRoute
import com.learningis4fun.gameshub.crusade_cards.commons.ui.routes.ProfileScreenRoute
import com.learningis4fun.gameshub.crusade_cards.commons.ui.routes.StartScreenRoute
import com.learningis4fun.gameshub.crusade_cards.feature_profile.presentation.profile.ProfileScreen
import com.learningis4fun.gameshub.crusade_cards.feature_profile.presentation.profile.ProfileViewModel
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.presentation.PlayCardsScreen
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.presentation.PlayCardsViewModel
import com.learningis4fun.gameshub.crusade_cards.start.CardsStartViewModel
import com.learningis4fun.gameshub.crusade_cards.start.presentation.CardsStartScreen
import com.learningis4fun.gameshub.util.ScaleTransitionDirection
import com.learningis4fun.gameshub.util.scaleIntoContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi


@OptIn(ExperimentalCoroutinesApi::class, ExperimentalStdlibApi::class)
@Composable
fun NavigationRoot(
    modifier: Modifier
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = StartScreenRoute) {

        composable<StartScreenRoute> {
            val activity = LocalContext.current

            val cardsStartViewModel =
                ViewModelProvider(activity as ComponentActivity)[CardsStartViewModel::class.java]

            val cardsStartState by cardsStartViewModel.cardsStartState.collectAsState()

            LaunchedEffect(key1 = true) {
                cardsStartViewModel.cardsStartScreenEvents.collect { event ->
                    when (event) {
                        CardsStartViewModel.CardsStartScreenEvents.NavigateToPlayCardsScreen -> {
                            //
                            println("Navigating to playCards")
                            navController.navigate(PlayCardsScreenRoute)
                        }

                        CardsStartViewModel.CardsStartScreenEvents.NavigateToAccountScreen -> {
                            navController.navigate(ProfileScreenRoute)
                        }

                        CardsStartViewModel.CardsStartScreenEvents.OpenChooseNumberOfCardsScreen -> TODO()
                        is CardsStartViewModel.CardsStartScreenEvents.ShowErrorMessage -> {
                            println(event.message)
                        }
                    }
                }
            }

            CardsStartScreen(
                modifier = modifier,
                cardsStartState = cardsStartState,
                onStartClick = {
                    cardsStartViewModel.onChallengeAIBtnClick()
                },
                onResumeClick = {
                    cardsStartViewModel.onResumeGameBtnClick()
                },
                onProfileClick = {
                    navController.navigate(ProfileScreenRoute)
                }
            )
        }

        composable<PlayCardsScreenRoute>(
            enterTransition = {
                scaleIntoContainer()
            },
            popEnterTransition = {
                scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS)
            }
        ) {
            val activity = LocalContext.current as ComponentActivity
            val playCardsViewModel: PlayCardsViewModel = hiltViewModel()

            val playCardsState by playCardsViewModel.playCardsState.collectAsState()
            val gameDuration by playCardsViewModel.gameDuration.collectAsState()

            val lifecycleOwner = LocalLifecycleOwner.current
            val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

            LaunchedEffect(lifecycleState) {
                // Do something with your state
                // You may want to use DisposableEffect or other alternatives
                // instead of LaunchedEffect
                when (lifecycleState) {
                    Lifecycle.State.DESTROYED -> {
                        playCardsViewModel.onAppPaused()
                    }

                    Lifecycle.State.INITIALIZED -> {}
                    Lifecycle.State.CREATED -> {}
                    Lifecycle.State.STARTED -> {}
                    Lifecycle.State.RESUMED -> {
                        playCardsViewModel.onAppResumed()
                    }
                }
            }

            activity.onBackPressedDispatcher
                .addCallback(lifecycleOwner, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        playCardsViewModel.onCloseNavigationIconClicked() {
                            navController.navigateUp()
                        }
                    }
                })

            PlayCardsScreen(
                playCardsState = playCardsState,
                gameDuration = gameDuration,
                onEvent = playCardsViewModel::onEvent,
                winner = {
                    navController.navigateUp()
                },
                onClose = {
                    playCardsViewModel.onCloseNavigationIconClicked {
                        navController.navigateUp()
                    }
                },
                onProfileClick = {
                    navController.navigate(ProfileScreenRoute)
                }
            )
        }

        composable<ProfileScreenRoute>(
            enterTransition = {
                scaleIntoContainer()
            },
            popEnterTransition = {
                scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS)
            }
        ) {

            val profileViewModel: ProfileViewModel = hiltViewModel()
            val userProfileInfo by profileViewModel.userProfileInfo.collectAsState()

            ProfileScreen(
                userProfileInfo = userProfileInfo,
                onBackClick = {
                    navController.navigateUp()
                },
                onSignOut = {
                    profileViewModel.signOut()
                }
            )
        }

    }

}