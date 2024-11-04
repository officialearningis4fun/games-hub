package com.learningis4fun.gameshub.crusade_cards.feature_game_winner.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
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
    ) {

        val viewModel: PlayedGameWinnerViewModel = hiltViewModel()

        val playedGameState by viewModel.playedGameState.collectAsStateWithLifecycle()

        val createNewGameToolsViewModel: CreateNewGameToolsViewModel = hiltViewModel()
        val createNewGameToolsState by createNewGameToolsViewModel.createNewGameToolsState.collectAsStateWithLifecycle()

        PlayedGameScreen(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            playedGameWinnerOrNextStage = playedGameState,
            createNewGameToolsState = createNewGameToolsState,
            onStartGame = onStartGame,
            onStartNextStage = onStartNextStage,
            onCloseClick = onCloseClick,
            onEvent = viewModel::onEvent,
            onCreateEvent = createNewGameToolsViewModel::onEvent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayedGameScreen(
    modifier: Modifier = Modifier,
    playedGameWinnerOrNextStage: PlayedGameWinnerOrNextStage,
    createNewGameToolsState: CreateNewGameToolsState = CreateNewGameToolsState(),
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

@Preview
@Composable
private fun PlayedGameNextStageScreenPrev() {
    GamesHubAppTheme(darkTheme = false) {
        PlayedGameScreen(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            playedGameWinnerOrNextStage = PlayedGameWinnerOrNextStage.PlayedGameNextStageState(
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
            ),
            createNewGameToolsState = CreateNewGameToolsState(),
            onCloseClick = {}
        )
    }
}

@Preview
@Composable
private fun PlayedGameNextStageScreenDarkPrev() {
    GamesHubAppTheme(darkTheme = true) {
        PlayedGameScreen(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            playedGameWinnerOrNextStage = PlayedGameWinnerOrNextStage.PlayedGameNextStageState(
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
            ),
            createNewGameToolsState = CreateNewGameToolsState(),
            onCloseClick = {}
        )
    }
}

@Preview
@Composable
private fun PlayedGameWinnerPrev() {
    GamesHubAppTheme(darkTheme = false) {
        PlayedGameScreen(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            playedGameWinnerOrNextStage = PlayedGameWinnerOrNextStage.PlayedGameWinnerState(
                loading = false,
                gameDuration = (80000L).toUIDuration(),
                otherPlayers = listOf(
                    Player(
                        "id",
                        profileName = "Hlompho",
                        imageUrl = R.drawable.avatar_boy1.toString()
                    ),
                ),
                winnerNames = "Hlompho",
                winnerImageUrl = R.drawable.avatar_boy1.toString()
            ),
            createNewGameToolsState = CreateNewGameToolsState()
        )
    }
}

@Preview
@Composable
private fun PlayedGameWinnerDarkPrev() {
    GamesHubAppTheme(darkTheme = true) {
        PlayedGameScreen(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            playedGameWinnerOrNextStage = PlayedGameWinnerOrNextStage.PlayedGameWinnerState(
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
            ),
            createNewGameToolsState = CreateNewGameToolsState()
        )
    }
}