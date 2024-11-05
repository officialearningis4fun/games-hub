@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.learningis4fun.gameshub.crusade_cards.feature_game_winner.presentation

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.UiMode
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.toUIDuration
import com.learningis4fun.gameshub.crusade_cards.commons.ui.components.create_new_game_tools.CreateNewGameToolsEvents
import com.learningis4fun.gameshub.crusade_cards.commons.ui.components.create_new_game_tools.CreateNewGameToolsState
import com.learningis4fun.gameshub.crusade_cards.commons.ui.components.create_new_game_tools.CreateNewGameToolsViewModel
import com.learningis4fun.gameshub.crusade_cards.commons.ui.routes.WinnerScreenRoute
import com.learningis4fun.gameshub.crusade_cards.commons.ui.theme.GamesHubAppTheme
import com.learningis4fun.gameshub.util.ScaleTransitionDirection
import com.learningis4fun.gameshub.util.scaleIntoContainer

fun NavGraphBuilder.playedGameWinnerComposable(
    sharedTransitionScope: SharedTransitionScope,
    onStartGame: () -> Unit,
    onCloseClick: () -> Unit,
    onStartNextStage: () -> Unit
) {

    composable<WinnerScreenRoute>(
        enterTransition = {
            scaleIntoContainer()
        },
        popEnterTransition = {
            scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS)
        }
    ) { backStateEntry ->

        val winnerImageRes =
            backStateEntry.toRoute<WinnerScreenRoute>().winnerImageUrl.toIntOrNull()
                ?: R.drawable.game_test_profile

        val viewModel: PlayedGameWinnerViewModel = hiltViewModel()

        val playedGameState by viewModel.playedGameState.collectAsStateWithLifecycle()

        val createNewGameToolsViewModel: CreateNewGameToolsViewModel = hiltViewModel()
        val createNewGameToolsState by createNewGameToolsViewModel.createNewGameToolsState.collectAsStateWithLifecycle()

        PlayedGameScreen(
            modifier = Modifier
                .fillMaxSize(),
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = this,
            playedGameWinnerOrNextStage = playedGameState,
            createNewGameToolsState = createNewGameToolsState,
            winnerImageRes = winnerImageRes,
            onStartGame = onStartGame,
            onStartNextStage = onStartNextStage,
            onCloseClick = onCloseClick,
            onEvent = viewModel::onEvent,
            onCreateEvent = createNewGameToolsViewModel::onEvent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun PlayedGameScreen(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    playedGameWinnerOrNextStage: PlayedGameWinnerOrNextStage,
    createNewGameToolsState: CreateNewGameToolsState = CreateNewGameToolsState(),
    winnerImageRes: Int = R.drawable.avatar_boy1,
    onCloseClick: () -> Unit = {},
    onStartGame: () -> Unit = {},
    onStartNextStage: () -> Unit = {},
    onEvent: (PlayedGameWinnerEvents) -> Unit = {},
    onCreateEvent: (CreateNewGameToolsEvents) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                title = {
                    Text(text = stringResource(id = R.string.screen_winner_title))
                },
                actions = {
                    IconButton(onClick = onCloseClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null
                        )
                    }
                },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            PlayedGameWinnerSection(
                imageRes = winnerImageRes,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope
            )

            when (playedGameWinnerOrNextStage) {
                PlayedGameWinnerOrNextStage.Loading -> {
                    CircularProgressIndicator()
                }

                is PlayedGameWinnerOrNextStage.PlayedGameNextStageState -> {
                    PlayedGameNextStage(
                        playedGameNextStageState = playedGameWinnerOrNextStage,
                        onStartGame = onStartNextStage,
                        onEvent = onEvent
                    )
                }

                is PlayedGameWinnerOrNextStage.PlayedGameWinnerState -> {
                    PlayedGameWinner(
                        playedGameWinnerState = playedGameWinnerOrNextStage,
                        createNewGameToolsState = createNewGameToolsState,
                        onEvent = onEvent,
                        onStartGame = onStartGame,
                        onCreateEvent = onCreateEvent
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayedGameWinnerSection(
    modifier: Modifier = Modifier,
    imageRes: Int,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {

    with(sharedTransitionScope) {
        Image(
            modifier = modifier
                .size(128.dp)
                .clip(CircleShape)
                .sharedElement(
                    sharedTransitionScope.rememberSharedContentState(key = "image/$imageRes"),
                    animatedVisibilityScope = animatedContentScope
                ),
            contentScale = ContentScale.Crop,
            painter = painterResource(id = imageRes),
            contentDescription = null
        )
    }
}

@Preview
@Composable
private fun PlayedGameNextStageScreenPrev() {
    GamesHubAppTheme() {
        SharedTransitionLayout {
            NavHost(
                navController = rememberNavController(),
                startDestination = "start"
            ) {
                composable(route = "start") {
                    PlayedGameScreen(
                        modifier = Modifier
                            .fillMaxSize(),
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this,
                        playedGameWinnerOrNextStage = playedGameNextStageState,
                        createNewGameToolsState = CreateNewGameToolsState(),
                        onCloseClick = {}
                    )
                }
            }
        }
    }
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PlayedGameNextStageScreenDarkPrev() {
    GamesHubAppTheme() {
        SharedTransitionLayout {
            NavHost(
                navController = rememberNavController(),
                startDestination = "start"
            ) {
                composable(route = "start") {
                    PlayedGameScreen(
                        modifier = Modifier
                            .fillMaxSize(),
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this,
                        playedGameWinnerOrNextStage = playedGameNextStageState,
                        createNewGameToolsState = CreateNewGameToolsState(),
                        onCloseClick = {}
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PlayedGameWinnerPrev() {
    GamesHubAppTheme() {
        SharedTransitionLayout {
            NavHost(
                navController = rememberNavController(),
                startDestination = "start"
            ) {
                composable(route = "start") {
                    PlayedGameScreen(
                        modifier = Modifier
                            .fillMaxSize(),
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this,
                        playedGameWinnerOrNextStage = playedGameWinnerState,
                        createNewGameToolsState = CreateNewGameToolsState()
                    )
                }
            }
        }
    }
}



@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PlayedGameWinnerDarkPrev() {
    GamesHubAppTheme() {
        SharedTransitionLayout {
            NavHost(
                navController = rememberNavController(),
                startDestination = "start"
            ) {
                composable(route = "start") {
                    PlayedGameScreen(
                        modifier = Modifier
                            .fillMaxSize(),
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this,
                        playedGameWinnerOrNextStage = playedGameWinnerState,
                        createNewGameToolsState = CreateNewGameToolsState()
                    )
                }
            }
        }
    }
}

val playedGameWinnerState = PlayedGameWinnerOrNextStage.PlayedGameWinnerState(
    loading = false,
    gameDuration = (8L).toUIDuration(),
    otherPlayers = listOf(
        Player(
            "id",
            profileName = "Hlompho",
            imageUrl = R.drawable.avatar_boy1.toString()
        ),
        Player(
            "id2",
            profileName = "Android",
            imageUrl = R.drawable.ai_platform.toString()
        ),
    ),
    winnerNames = "Hlompho",
    winnerImageUrl = R.drawable.avatar_boy1.toString()
)

val playedGameNextStageState = PlayedGameWinnerOrNextStage.PlayedGameNextStageState(
    loading = false,
    gameDuration = (80000L).toUIDuration(),
    eliminatedPlayers = listOf(
        Player(
            "id",
            profileName = "Hlompho",
            imageUrl = R.drawable.avatar_boy1.toString()
        ),
    ),
    nextStagePlayers = listOf(
        Player(
            "id2",
            profileName = "Hlompho",
            imageUrl = R.drawable.avatar_boy1.toString()
        ),
    ),
    winnerNames = "Hlompho",
    winnerImageUrl = R.drawable.avatar_boy1.toString()
)