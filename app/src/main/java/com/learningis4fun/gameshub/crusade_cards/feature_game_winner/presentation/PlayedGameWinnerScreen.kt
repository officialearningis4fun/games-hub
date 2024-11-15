package com.learningis4fun.gameshub.crusade_cards.feature_game_winner.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Card
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Game
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.toUIDuration
import com.learningis4fun.gameshub.crusade_cards.commons.ui.components.create_new_game_tools.CreateGameSetupToolsSection
import com.learningis4fun.gameshub.crusade_cards.commons.ui.components.create_new_game_tools.CreateNewGameToolsEvents
import com.learningis4fun.gameshub.crusade_cards.commons.ui.components.create_new_game_tools.CreateNewGameToolsState
import com.learningis4fun.gameshub.crusade_cards.commons.ui.theme.GamesHubAppTheme
import com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.Heading
import com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.OnGoingGame
import com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.OngoingGamesOrNewGameState
import com.learningis4fun.gameshub.crusade_cards.feature_start.presentation.OngoingGamesSection
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.relations.GameWithPlayers
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.PlayerScreenInfo
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.UserCard
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.presentation.CardItem


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayedGameWinner(
    modifier: Modifier = Modifier,
    playedGameWinnerState: PlayedGameWinnerOrNextStage.PlayedGameWinnerState,
    createNewGameToolsState: CreateNewGameToolsState,
    onStartGame: () -> Unit = {},
    onResumeClick: (String) -> Unit = {},
    onEvent: (PlayedGameWinnerEvents) -> Unit = {},
    onCreateEvent: (CreateNewGameToolsEvents) -> Unit = {}
) {

    var onShowCardsState by remember {
        mutableStateOf(Pair("", false))
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {

            Text(
                text = playedGameWinnerState.winnerNames,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Black
                )
            )

            Text(
                text = " won within ",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = playedGameWinnerState.gameDuration,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Black
                )
            )

            Text(
                text = " against",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        PlayerCardsViewPopupContainer(
            modifier = Modifier,
            cardsOfPlayer = playedGameWinnerState.unPlayedCardsOfPlayer(onShowCardsState.first),
            onPopupDismissRequested = { onShowCardsState = Pair("", false) },
            isVisible = onShowCardsState.second && onShowCardsState.first.isNotEmpty()
        )

        Spacer(modifier = Modifier.height(16.dp))

        FlowRow(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            playedGameWinnerState.otherPlayersScreenInfo().forEach {
                val showCardsPopupView =
                    onShowCardsState.second && onShowCardsState.first == it.playerId
                PlayerItemWithPopup(
                    modifier = Modifier,
                    playerScreenInfo = it.copy(isCurrentlyPlaying = showCardsPopupView),
                    onClick = {
                        onShowCardsState = Pair(it.playerId, true)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (playedGameWinnerState.ongoingGamesOrNewGameState) {
            OngoingGamesOrNewGameState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                )
            }

            OngoingGamesOrNewGameState.NewGameState -> {
                CreateNewGameSection(
                    modifier = Modifier,
                    createNewGameToolsState = createNewGameToolsState,
                    onCreateEvent = onCreateEvent,
                    onStartGame = onStartGame
                )
            }

            is OngoingGamesOrNewGameState.OngoingGamesState -> {
                PlayedGameOngoingGamesSection(
                    modifier = Modifier,
                    ongoingGamesState = playedGameWinnerState.ongoingGamesOrNewGameState,
                    onDeleteGame = { gameId ->
                        onEvent(PlayedGameWinnerEvents.DeleteGame(gameId))
                    },
                    onResumeClick = onResumeClick
                )
            }
        }

    }

}

@Composable
fun PlayerCardsViewPopupContainer(
    modifier: Modifier = Modifier,
    cardsOfPlayer: List<Card>,
    onPopupDismissRequested: () -> Unit,
    isVisible: Boolean
) {
    Box(modifier) {
        Spacer(modifier = Modifier.height(0.dp))
        if (isVisible) {
            Popup(
                alignment = Alignment.BottomCenter,
                offset = IntOffset(0, 0),
                onDismissRequest = onPopupDismissRequested
            ) {

                Card(
                    modifier = Modifier,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        LazyRow(
                            Modifier
                                .height(144.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(cardsOfPlayer) {
                                CardItem(card = it, count = 0)
                            }

                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerItemWithPopup(
    modifier: Modifier,
    sizeOfPhoto: Dp = 64.dp,
    playerScreenInfo: PlayerScreenInfo,
    onClick: () -> Unit = {}
) {

    val image = playerScreenInfo.imageUrl?.toIntOrNull() ?: playerScreenInfo.defaultAvatar

    val isPlaying = playerScreenInfo.isCurrentlyPlaying

    Box {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Card(
                modifier = Modifier,
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Box(
                    modifier = Modifier
                ) {

                    Image(
                        modifier = Modifier
                            .size(sizeOfPhoto)
                            .clip(CircleShape)
                            .border(
                                width = 3.dp,
                                shape = CircleShape,
                                color = if (isPlaying) Color.White else Color.LightGray.copy(alpha = 0.8f)
                            )
                            .clickable { onClick() },
                        contentScale = ContentScale.Crop,
                        painter = painterResource(id = image),
                        contentDescription = null
                    )

                    if (playerScreenInfo.numOfCardsLeft.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Text(
                                modifier = Modifier
                                    .align(Alignment.Center),
                                text = playerScreenInfo.numOfCardsLeft,
                                color = MaterialTheme.colorScheme.onError,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Text(
                modifier = Modifier
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                text = playerScreenInfo.playerName,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
        }
    }

}


@Composable
private fun CreateNewGameSection(
    modifier: Modifier = Modifier,
    createNewGameToolsState: CreateNewGameToolsState,
    onCreateEvent: (CreateNewGameToolsEvents) -> Unit,
    onStartGame: () -> Unit
) {
    Column(modifier) {
        HorizontalDivider()

        CreateGameSetupToolsSection(
            modifier = Modifier
                .fillMaxWidth(),
            createGameButtonText = "Play another game",
            createNewGameToolsState = createNewGameToolsState,
            onEvent = onCreateEvent,
            onStartGame = onStartGame
        )

    }
}

@Composable
private fun PlayedGameOngoingGamesSection(
    modifier: Modifier = Modifier,
    ongoingGamesState: OngoingGamesOrNewGameState.OngoingGamesState,
    onResumeClick: (String) -> Unit = {},
    onDeleteGame: (String) -> Unit = {}
) {
    Column(modifier) {
        HorizontalDivider()

        Heading(
            text = "Ongoing games",
            modifier = Modifier.padding(16.dp),
            textColor = MaterialTheme.colorScheme.onBackground
        )

        OngoingGamesSection(
            ongoingGamesState = ongoingGamesState,
            onDeleteGame = onDeleteGame,
            onResumeClick = onResumeClick
        )
    }
}


@Preview
@Composable
private fun PlayedGameWinnerPrev() {
    GamesHubAppTheme(darkTheme = false) {
        PlayedGameWinner(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            playedGameWinnerState = PlayedGameWinnerOrNextStage.PlayedGameWinnerState(
                loading = false,
                gameDuration = (80000L).toUIDuration(),
                otherPlayers = listOf(
                    Player(
                        "uniqueId",
                        profileName = "Hlompho",
                        imageUrl = R.drawable.avatar_boy1.toString()
                    ),
                ),
                unPlayedCards = Constants.Cards.cards.take(4).mapIndexed { index, card ->
                    UserCard(
                        gameCardId = "$index",
                        playerId = Constants.Players.USER.uniqueId,
                        cardId = card.id,
                        name = card.name,
                        type = card.type,
                        gameId = "gameId",
                        isPlayed = false,
                        image = card.image,
                        playTime = 0L,
                        pickedTime = 0L,
                    )
                },
                winnerNames = "Hlompho",
                winnerImageUrl = R.drawable.avatar_boy1.toString(),
                ongoingGamesOrNewGameState = ongoingGamesState
            ),
            createNewGameToolsState = CreateNewGameToolsState()
        )
    }
}

@Preview
@Composable
private fun PlayedGameWinnerDarkPrev() {
    GamesHubAppTheme {
        PlayedGameWinner(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            playedGameWinnerState = PlayedGameWinnerOrNextStage.PlayedGameWinnerState(
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
                winnerImageUrl = R.drawable.avatar_boy1.toString(),
                ongoingGamesOrNewGameState = OngoingGamesOrNewGameState.NewGameState
            ),
            createNewGameToolsState = CreateNewGameToolsState()
        )
    }
}

val ongoingGamesState = OngoingGamesOrNewGameState.OngoingGamesState(
    listOf(
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
                        profileName = "Dev1",
                        imageUrl = R.drawable.avatar_boy2.toString()
                    ),
                    Player(
                        uniqueId = "id2",
                        profileName = "You",
                        imageUrl = R.drawable.ai_platform.toString()
                    )
                )
            )
        )
    )
)

