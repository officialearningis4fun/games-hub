package com.learningis4fun.gameshub.crusade_cards.feature_start.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Game
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.ui.routes.StartScreenRoute
import com.learningis4fun.gameshub.crusade_cards.feature_start.CardsStartViewModel
import com.learningis4fun.gameshub.crusade_cards.feature_start.domain.model.CompletedGame
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.relations.GameWithPlayers
import kotlinx.coroutines.ExperimentalCoroutinesApi


@OptIn(ExperimentalCoroutinesApi::class)
fun NavGraphBuilder.cardsStartScreenComposable(
    onStartClick: () -> Unit,
    onResumeClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onGameClick: (String) -> Unit,
) {
    composable<StartScreenRoute> {

        val cardsStartViewModel: CardsStartViewModel = hiltViewModel()

        val cardsStartState by cardsStartViewModel.cardsStartState.collectAsStateWithLifecycle()

        CardsStartScreen(
            modifier = Modifier.fillMaxSize(),
            cardsStartState = cardsStartState,
            onStartClick = onStartClick,
            onResumeClick = onResumeClick,
            onProfileClick = onProfileClick,
            onGameClick = onGameClick,
            onDeleteGame = { gameId ->
                cardsStartViewModel.onDeleteGame(gameId)
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsStartScreen(
    modifier: Modifier,
    cardsStartState: CardsStartState,
    onStartClick: () -> Unit = {},
    onResumeClick: (String) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onGameClick: (String) -> Unit = {},
    onDeleteGame: (String) -> Unit = {}
) {

    val cardHeight = 96.dp
    val backgroundColor = Color(0xffff8800)

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White,
                    titleContentColor = Color.White
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
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            CardsStartDataScreen(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues),
                completedGamesState = cardsStartState.completedGamesState,
                ongoingGamesState = cardsStartState.ongoingGamesState,
                showOngoingGame = cardsStartState.showOngoingGame,
                onStartClick = onStartClick,
                onResumeClick = onResumeClick,
                onGameClick = onGameClick,
                onDeleteGame = onDeleteGame
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
        CircularProgressIndicator()
    }

}

@Composable
fun CardsStartErrorScreen(
    modifier: Modifier = Modifier,
    error: String
) {

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(text = error)
    }

}

@Composable
fun CardsStartDataScreen(
    modifier: Modifier,
    completedGamesState: CompletedGamesState,
    ongoingGamesState: OngoingGamesState,
    showOngoingGame: Boolean,
    onStartClick: () -> Unit = {},
    onResumeClick: (String) -> Unit = {},
    onGameClick: (String) -> Unit = {},
    onDeleteGame: (String) -> Unit = {}
) {

    Box(
        modifier = Modifier
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
                    textColor = Color.Black
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
                    onStartClick = onStartClick
                )
            }
        }

    }

}

@Preview
@Composable
private fun CardsStartScreenLoadingPrev() {
    CardsStartScreen(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        cardsStartState = CardsStartState()
    )
}

@Preview
@Composable
private fun CardsStartScreenWithEmptyCompletedGamesPrev() {
    CardsStartScreen(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        cardsStartState = CardsStartState(
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
    )
}

@Preview
@Composable
private fun CardsStartScreenPrev() {
    CardsStartScreen(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        cardsStartState = CardsStartState(
            isLoading = false,
            completedGamesState = CompletedGamesState(
                completedGames = listOf(
                    CompletedGame(
                        game = Game(id = "", name = "Game 01", winnerId = "id"),
                        players = listOf(
                            Player("id", profileName = "Hlompho"),
                            Player("id2", profileName = "Android"),
                        )
                    ),
                    CompletedGame(
                        game = Game(id = "", name = "Game 01", winnerId = "id"),
                        players = listOf(
                            Player("id", profileName = "Hlompho"),
                            Player("id2", profileName = "Android"),
                        )
                    )
                )
            ),
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
    )
}

@Preview
@Composable
private fun CardsStartScreenWithOngoingGamesPrev() {
    CardsStartScreen(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        cardsStartState = CardsStartState(
            isLoading = false,
            showOngoingGame = true,
            completedGamesState = CompletedGamesState(
                completedGames = listOf(
                    CompletedGame(
                        game = Game(id = "", name = "Game 01", winnerId = "id"),
                        players = listOf(
                            Player("id", profileName = "Hlompho"),
                            Player("id2", profileName = "Android"),
                        )
                    ),
                    CompletedGame(
                        game = Game(id = "", name = "Game 01", winnerId = "id"),
                        players = listOf(
                            Player("id", profileName = "Hlompho"),
                            Player("id2", profileName = "Android"),
                        )
                    )
                )
            ),
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
    )
}