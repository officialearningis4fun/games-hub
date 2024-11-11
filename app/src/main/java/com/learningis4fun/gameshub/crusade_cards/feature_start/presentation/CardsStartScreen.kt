package com.learningis4fun.gameshub.crusade_cards.feature_start.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Game
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.ui.components.create_new_game_tools.CreateNewGameToolsEvents
import com.learningis4fun.gameshub.crusade_cards.commons.ui.components.create_new_game_tools.CreateNewGameToolsState
import com.learningis4fun.gameshub.crusade_cards.commons.ui.components.create_new_game_tools.CreateNewGameToolsViewModel
import com.learningis4fun.gameshub.crusade_cards.commons.ui.routes.StartScreenRoute
import com.learningis4fun.gameshub.crusade_cards.commons.ui.theme.GamesHubAppTheme
import com.learningis4fun.gameshub.crusade_cards.feature_start.CardsStartEvents
import com.learningis4fun.gameshub.crusade_cards.feature_start.CardsStartViewModel
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.relations.GameWithPlayers
import kotlinx.coroutines.ExperimentalCoroutinesApi


@OptIn(ExperimentalCoroutinesApi::class)
fun NavGraphBuilder.cardsStartScreenComposable(
    onStartClick: (String) -> Unit,
    onResumeClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onGameClick: (Pair<Game, Player>) -> Unit = {},
) {
    composable<StartScreenRoute> {

        val cardsStartViewModel: CardsStartViewModel = hiltViewModel()
        val cardsStartState by cardsStartViewModel.cardsStartState.collectAsStateWithLifecycle()

        val createNewGameToolsViewModel = hiltViewModel<CreateNewGameToolsViewModel>()
        val createNewGameToolsState by createNewGameToolsViewModel.createNewGameToolsState.collectAsStateWithLifecycle()

        CardsStartScreen(
            modifier = Modifier
                .fillMaxSize(),
            cardsStartState = cardsStartState,
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsStartScreen(
    modifier: Modifier = Modifier,
    cardsStartState: CardsStartState,
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
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues),
                completedGamesState = cardsStartState.completedGamesState,
                ongoingGamesState = cardsStartState.ongoingGamesState,
                showOngoingGame = cardsStartState.showOngoingGame,
                onStartClick = onStartClick,
                onResumeClick = onResumeClick,
                onGameClick = onGameClick,
                onDeleteGame = {
                    onEvent(CardsStartEvents.DeleteGame(it))
                },
                createNewGameToolsState = createNewGameToolsState,
                onEvent = onCreateGameEvent
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
    completedGamesState: CompletedGamesState,
    ongoingGamesState: OngoingGamesState,
    createNewGameToolsState: CreateNewGameToolsState,
    showOngoingGame: Boolean,
    onStartClick: () -> Unit = {},
    onResumeClick: (String) -> Unit = {},
    onGameClick: (Pair<Game, Player>) -> Unit = {},
    onDeleteGame: (String) -> Unit = {},
    onEvent: (CreateNewGameToolsEvents) -> Unit = {}
) {

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        Column(modifier) {

            CompletedGamesSection(
                completedGamesState = completedGamesState,
                onGameClick = onGameClick
            )

            HorizontalDivider()

            if (showOngoingGame) {
                Heading(
                    text = "Ongoing games",
                    modifier = Modifier.padding(16.dp),
                    textColor = MaterialTheme.colorScheme.onBackground
                )

                OngoingGamesSection(
                    modifier = Modifier.fillMaxWidth(),
                    ongoingGamesState = ongoingGamesState,
                    onResumeClick = onResumeClick,
                    onDeleteGame = onDeleteGame

                )
            } else {
                CreateGameSection(
                    modifier = Modifier.fillMaxWidth(),
                    onStartClick = onStartClick,
                    createNewGameToolsState = createNewGameToolsState,
                    onEvent = onEvent

                )
            }
        }

    }

}

@Composable
private fun PrevRootLayout(
    content: @Composable () -> Unit
) {
    GamesHubAppTheme {
        content()
    }
}

@Preview
@Composable
private fun CardsStartScreenLightPrev() {
    PrevRootLayout {
        CardsStartScreen(
            modifier = Modifier
                .fillMaxSize(),
            cardsStartState = cardsStartState.copy(isLoading = false)
        )
    }
}

val cardsStartState = CardsStartState(
    isLoading = false,
    completedGamesState = CompletedGamesState(),
    ongoingGamesState = OngoingGamesState(
        games = listOf(
            OnGoingGame(

                threeCards = Constants
                    .Cards
                    .cards
                    .take(3).mapIndexed { index, card ->
                        card.copy(
                            id = "$index"
                        )
                    },
                gameWithPlayers = GameWithPlayers(
                    game = Game(),
                    players = listOf(
                        Player(
                            uniqueId = "id1",
                            profileName = "Android"
                        ),
                        Player(
                            uniqueId = "id2",
                            profileName = "Hlompho"
                        )
                    )
                )
            )
        )
    )
)