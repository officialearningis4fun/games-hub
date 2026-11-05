package com.learningis4fun.gameshub.presentation.play_crusade_cards.online

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.learningis4fun.gameshub.commons.ui.routes.PlayCardsOnlineScreenRoute
import com.learningis4fun.gameshub.commons.ui.routes.WinnerScreenRoute
import com.learningis4fun.gameshub.presentation.play_crusade_cards.Heading
import com.learningis4fun.gameshub.presentation.play_crusade_cards.PlayCardsScreen
import com.learningis4fun.gameshub.presentation.play_crusade_cards.TimerSection
import com.learningis4fun.gameshub.presentation.util.GamesHubAnimatedContentScope
import com.learningis4fun.gameshub.util.ScaleTransitionDirection
import com.learningis4fun.gameshub.util.scaleIntoContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalStdlibApi::class, ExperimentalCoroutinesApi::class)
fun NavGraphBuilder.playCardsOnlineScreenComposable(
    onBackClick: () -> Unit,
    navigateToWinner: (WinnerScreenRoute) -> Unit
) {
    composable<PlayCardsOnlineScreenRoute>(
        enterTransition = {
            scaleIntoContainer()
        },
        popEnterTransition = {
            scaleIntoContainer(direction = ScaleTransitionDirection.INWARDS)
        }
    ) {

        val playCardsViewModel: PlayCardsOnlineViewModel = hiltViewModel()

        val playCardsState by playCardsViewModel.playCardsState.collectAsState()
        val gameDuration by playCardsViewModel.gameDuration.collectAsStateWithLifecycle()

        val animatingCardAndStage by playCardsViewModel.animatingCardAndStage.collectAsStateWithLifecycle()

        BackHandler(
            onBack = {
                playCardsViewModel.onCloseNavigationIconClicked {
                    onBackClick()
                }
            }
        )

        GamesHubAnimatedContentScope(
            animatedContentScope = this@composable
        ) {
            PlayCardsScreen(
                playCardsState = playCardsState,
                animatingCardAndStage = animatingCardAndStage,
                onEvent = playCardsViewModel::onEvent,
                timerSectionContent = {
                    TimerSection(
                        textColor = Color.Black,
                        duration = gameDuration,
                        direction = playCardsState.playingDirection
                    )
                },
                headingTimerContent = {
                    Heading(
                        text = gameDuration,
                        textColor = Color.Black
                    )
                },
                winner = { (player, wonGameId) ->
                    navigateToWinner(
                        WinnerScreenRoute(
                            wonGameId = wonGameId,
                            winnerImageUrl = player.imageUrl,
                            playerId = player.uniqueId
                        )
                    )
                },
                onClose = {
                    playCardsViewModel.onCloseNavigationIconClicked {
                        onBackClick()
                    }
                },
                onPauseGame = {
                    playCardsViewModel.onAppPaused()
                },
                onResumeGame = {
                    playCardsViewModel.onAppResumed()
                }
            )

        }
    }
}