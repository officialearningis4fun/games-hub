package com.learningis4fun.gameshub.presentation.start

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.commons.domain.model.Game
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.CreateNewGameToolsEvents
import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.CreateNewGameToolsState
import com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools.CreateNewGameToolsViewModel
import com.learningis4fun.gameshub.commons.ui.components.ongoing_or_create_new_game.OngoingGamesOrCreateNewGameState
import com.learningis4fun.gameshub.commons.ui.components.ongoing_or_create_new_game.OngoingGamesOrCreateNewGameToolsSection
import com.learningis4fun.gameshub.commons.ui.components.ongoing_or_create_new_game.OngoingGamesOrCreateNewGameViewModel
import com.learningis4fun.gameshub.commons.ui.routes.StartScreenRoute
import com.learningis4fun.gameshub.presentation.util.GamesHubAnimatedContentScope
import com.learningis4fun.gameshub.presentation.util.GamesHubAppThemePrev
import kotlinx.coroutines.ExperimentalCoroutinesApi


@OptIn(ExperimentalCoroutinesApi::class)
fun NavGraphBuilder.cardsStartScreenComposable(
    onStartClick: (String) -> Unit,
    onResumeClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onGameClick: (Pair<Game, Player>) -> Unit = {},
) {
    composable<StartScreenRoute> { backStackEntry ->
        backStackEntry.destination.parent

        val cardsStartViewModel: CardsStartViewModel = hiltViewModel()
        val cardsStartState by cardsStartViewModel.cardsStartState.collectAsStateWithLifecycle()

        val ongoingGamesOrCreateNewGameViewModel: OngoingGamesOrCreateNewGameViewModel =
            hiltViewModel()
        val ongoingGamesOrCreateNewGameState by ongoingGamesOrCreateNewGameViewModel.ongoingGamesOrCreateNewGameState.collectAsStateWithLifecycle()

        ongoingGamesOrCreateNewGameViewModel.setGameId("")

        val createNewGameToolsViewModel = hiltViewModel<CreateNewGameToolsViewModel>()
        val createNewGameToolsState by createNewGameToolsViewModel.createNewGameToolsState.collectAsStateWithLifecycle()

        GamesHubAnimatedContentScope(
            animatedContentScope = this@composable
        ) {
            CardsStartScreen(
                modifier = Modifier
                    .fillMaxSize(),
                cardsStartState = cardsStartState,
                ongoingGamesOrCreateNewGameState = ongoingGamesOrCreateNewGameState,
                createNewGameToolsState = createNewGameToolsState,
                onStartClick = {
                    createNewGameToolsViewModel.setupNewGameOnStartClick { gameId ->
                        onStartClick(gameId)
                    }
                },
                onResumeClick = onResumeClick,
                onProfileClick = onProfileClick,
                onGameClick = onGameClick,
                onEvent = cardsStartViewModel::onEvent,
                onCreateGameEvent = createNewGameToolsViewModel::onEvent
            )
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsStartScreen(
    modifier: Modifier = Modifier,
    cardsStartState: CardsStartState,
    ongoingGamesOrCreateNewGameState: OngoingGamesOrCreateNewGameState = OngoingGamesOrCreateNewGameState.Loading,
    createNewGameToolsState: CreateNewGameToolsState = CreateNewGameToolsState(),
    onStartClick: () -> Unit = {},
    onResumeClick: (String) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onGameClick: (Pair<Game, Player>) -> Unit = {},
    onEvent: (CardsStartEvents) -> Unit = {},
    onCreateGameEvent: (CreateNewGameToolsEvents) -> Unit = {}
) {

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.shadow(
                    elevation = 5.dp,
                    spotColor = Color.LightGray
                ),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                title = {
                    Text(text = stringResource(id = R.string.app_name))
                },
                actions = {
                    IconButton(onClick = { onProfileClick() }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (cardsStartState.isLoading) {
            CardsStartLoadingScreen(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
            )
        } else {
            CardsStartDataScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues),
                completedGamesState = cardsStartState.completedGamesState,
                ongoingGamesOrCreateNewGameState = ongoingGamesOrCreateNewGameState,
                createNewGameToolsState = createNewGameToolsState,
                onStartClick = onStartClick,
                onResumeClick = onResumeClick,
                onGameClick = onGameClick,
                onEvent = onCreateGameEvent,
                onDeleteGame = {
                    onEvent(CardsStartEvents.DeleteGame(it))
                }
            )
        }
    }

}

@Composable
fun CardsStartLoadingScreen(
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp)
        )
    }

}

@Composable
fun CardsStartDataScreen(
    modifier: Modifier,
    ongoingGamesOrCreateNewGameState: OngoingGamesOrCreateNewGameState = OngoingGamesOrCreateNewGameState.Loading,
    completedGamesState: CompletedGamesState,
    createNewGameToolsState: CreateNewGameToolsState,
    onStartClick: () -> Unit = {},
    onResumeClick: (String) -> Unit = {},
    onGameClick: (Pair<Game, Player>) -> Unit = {},
    onDeleteGame: (String) -> Unit = {},
    onEvent: (CreateNewGameToolsEvents) -> Unit = {}
) {
    LazyColumn(modifier) {

        item {
            CompletedGamesSection(
                modifier = Modifier.fillMaxWidth(),
                completedGamesState = completedGamesState,
                onGameClick = onGameClick
            )
        }

        item {
            OngoingGamesOrCreateNewGameToolsSection(
                modifier = Modifier.fillMaxWidth(),
                ongoingGamesOrCreateNewGameState = ongoingGamesOrCreateNewGameState,
                createNewGameToolsState = createNewGameToolsState,
                onStartGame = onStartClick,
                onResumeClick = onResumeClick,
                onDeleteGame = onDeleteGame,
                onCreateEvent = onEvent,
            )
        }
    }

}

@Preview
@Composable
private fun CardsStartScreenLightPrev() {
    GamesHubAppThemePrev {
        CardsStartScreen(
            modifier = Modifier
                .fillMaxSize(),
            ongoingGamesOrCreateNewGameState = OngoingGamesOrCreateNewGameState.CreateNewGameToolsState,
            createNewGameToolsState = CreateNewGameToolsState(),
            cardsStartState = cardsStartState.copy(isLoading = false)
        )
    }
}

@Preview
@Composable
private fun CardsStartScreenWithOngoingGameLightPrev() {
    GamesHubAppThemePrev {
        CardsStartScreen(
            modifier = Modifier
                .fillMaxSize(),
            cardsStartState = cardsStartStateWithOngoingGame.copy(isLoading = false)
        )
    }
}

val cardsStartState = CardsStartState(
    isLoading = false,
    completedGamesState = CompletedGamesState(),
)

val cardsStartStateWithOngoingGame = CardsStartState(
    isLoading = false,
    completedGamesState = CompletedGamesState()
)