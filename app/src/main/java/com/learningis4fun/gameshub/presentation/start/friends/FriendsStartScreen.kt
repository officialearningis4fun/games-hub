package com.learningis4fun.gameshub.presentation.start.friends

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.app.nav.BottonNavRoute
import com.learningis4fun.gameshub.commons.domain.model.Game
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.ui.routes.CoreNavGraph
import com.learningis4fun.gameshub.presentation.start.CompletedGamesSection
import com.learningis4fun.gameshub.presentation.start.CompletedGamesState
import com.learningis4fun.gameshub.presentation.start.OngoingGamesSection
import com.learningis4fun.gameshub.presentation.util.GamesHubAnimatedContentScope
import com.learningis4fun.gameshub.presentation.util.GamesHubAppThemePrev
import com.learningis4fun.gameshub.util.ScaleTransitionDirection
import com.learningis4fun.gameshub.util.scaleIntoContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi


@OptIn(ExperimentalCoroutinesApi::class)
fun NavGraphBuilder.friendsScreenComposable(
    navController: NavHostController,
    onResumeClick: (String) -> Unit,
    onGameClick: (Pair<Game, Player>) -> Unit = {},
    onCreateClick: () -> Unit = {},
) {
    composable<BottonNavRoute.FriendsScreenRoute>(
        popEnterTransition = {
            scaleIntoContainer(direction = ScaleTransitionDirection.INWARDS)
        }) { backStackEntry ->
        backStackEntry.destination.parent

        val parentEntry = remember(backStackEntry) {
            navController.getBackStackEntry(CoreNavGraph)
        }

        val friendsStartViewModel: FriendsStartViewModel = hiltViewModel(parentEntry)

        val friendsStartState by friendsStartViewModel.friendsStartState.collectAsStateWithLifecycle()
        val ongoingGamesOrCreateNewGameState by friendsStartViewModel.ongoingGamesOrCreateNewGameState.collectAsStateWithLifecycle()

        GamesHubAnimatedContentScope(
            animatedContentScope = this@composable
        ) {
            FriendsStartScreen(
                modifier = Modifier.fillMaxSize(),
                friendsStartState = friendsStartState,
                ongoingGamesOrCreateNewGameState = ongoingGamesOrCreateNewGameState,
                onCreateClick = onCreateClick,
                onResumeClick = onResumeClick,
                onGameClick = onGameClick,
                onEvent = friendsStartViewModel::onEvent,
            )
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FriendsStartScreen(
    modifier: Modifier = Modifier,
    friendsStartState: FriendsStartState,
    ongoingGamesOrCreateNewGameState: OngoingGamesOrCreateNewGameState = OngoingGamesOrCreateNewGameState.Loading,
    onCreateClick: () -> Unit = {},
    onResumeClick: (String) -> Unit = {},
    onGameClick: (Pair<Game, Player>) -> Unit = {},
    onEvent: (FriendsStartEvents) -> Unit = {},
) {

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.shadow(
                    elevation = 5.dp, spotColor = Color.LightGray
                ), colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ), title = {
                    Text(text = stringResource(id = R.string.app_name))
                })
        })
    { paddingValues ->
        if (friendsStartState.isLoading) {
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
                completedGamesState = friendsStartState.completedGamesState,
                ongoingGamesOrCreateNewGameState = ongoingGamesOrCreateNewGameState,
                onResumeClick = onResumeClick,
                onGameClick = onGameClick,
                onCreateClick = onCreateClick,
                onDeleteGame = {
                    onEvent(FriendsStartEvents.DeleteGame(it))
                })
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
    onCreateClick: () -> Unit,
    onResumeClick: (String) -> Unit = {},
    onGameClick: (Pair<Game, Player>) -> Unit = {},
    onDeleteGame: (String) -> Unit = {},
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
                onCreateClick = onCreateClick,
                onResumeClick = onResumeClick,
                onDeleteGame = onDeleteGame,
            )
        }

    }

}

@Composable
private fun OngoingGamesOrCreateNewGameToolsSection(
    modifier: Modifier = Modifier,
    ongoingGamesOrCreateNewGameState: OngoingGamesOrCreateNewGameState,
    onCreateClick: () -> Unit,
    onResumeClick: (String) -> Unit = {},
    onDeleteGame: (String) -> Unit = {},
) {
    Box(modifier = modifier) {
        when (ongoingGamesOrCreateNewGameState) {
            OngoingGamesOrCreateNewGameState.CreateNewGameToolsState -> {
                Column {
                    HorizontalDivider()
                    CreateNewGameSection(
                        modifier = Modifier,
                        onCreateClick = onCreateClick,
                    )
                }
            }

            OngoingGamesOrCreateNewGameState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopCenter)
                )
            }

            OngoingGamesOrCreateNewGameState.Nothing -> {}
            is OngoingGamesOrCreateNewGameState.OngoingGamesState -> {
                OngoingGamesSection(
                    modifier = Modifier,
                    games = ongoingGamesOrCreateNewGameState.games,
                    onDeleteGame = { gameId ->
                        onDeleteGame(gameId)
                    },
                    onResumeClick = onResumeClick
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class)
@Composable
private fun CreateNewGameSection(
    modifier: Modifier = Modifier,
    onCreateClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            onClick = {
                onCreateClick()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(
                imageVector = Icons.Default.Edit, contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Create game".uppercase())
        }
    }

}

@Preview
@Composable
private fun CardsStartScreenLightPrev() {
    GamesHubAppThemePrev {
        FriendsStartScreen(
            modifier = Modifier.fillMaxSize(),
            ongoingGamesOrCreateNewGameState = OngoingGamesOrCreateNewGameState.CreateNewGameToolsState,
            friendsStartState = FriendsStartState(isLoading = false)
        )
    }
}

@Preview
@Composable
private fun CardsStartScreenWithOngoingGameLightPrev() {
    GamesHubAppThemePrev {
        FriendsStartScreen(
            modifier = Modifier.fillMaxSize(),
            friendsStartState = FriendsStartState(isLoading = false),
        )
    }
}