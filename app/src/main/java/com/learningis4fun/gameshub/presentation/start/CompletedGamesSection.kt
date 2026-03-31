package com.learningis4fun.gameshub.presentation.start

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.commons.domain.model.Game
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.domain.util.toUIDuration
import com.learningis4fun.gameshub.commons.ui.theme.GamesHubAppTheme
import com.learningis4fun.gameshub.presentation.util.GamesHubAppThemePrev
import com.learningis4fun.gameshub.presentation.util.LocalAnimatedContentScope
import com.learningis4fun.gameshub.presentation.util.LocalCustomSharedTransitionScope
import com.learningis4fun.gameshub.presentation.util.sharedImageElementKey

@Composable
fun CompletedGamesSection(
    modifier: Modifier = Modifier,
    completedGamesState: CompletedGamesState,
    onGameClick: (Pair<Game, Player>) -> Unit = {}
) {

    val completedGames = completedGamesState.completedGames

    Column(
        modifier.background(MaterialTheme.colorScheme.background)
    ) {
        if (completedGamesState.isLoading) {
            CompletedGamesLoadingScreen(
                modifier = Modifier.fillMaxWidth()
            )
        } else if (completedGames.isEmpty()) {
            EmptyListScreen()
        } else {
            CompletedGamesDataSection(
                modifier = Modifier.fillMaxWidth(),
                completedGamesState = completedGamesState,
                onGameClick = onGameClick
            )
        }

    }
}

@Composable
fun CompletedGamesDataSection(
    modifier: Modifier = Modifier,
    completedGamesState: CompletedGamesState,
    onGameClick: (Pair<Game, Player>) -> Unit = {}
) {

    val completedGames = completedGamesState.completedGames

    Column(modifier) {

        Heading(
            text = "Completed games",
            modifier = Modifier.padding(start = 16.dp, top = 16.dp),
            textColor = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier)

        LazyRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(completedGames) {
                CompletedGameItem(modifier = Modifier
                    .clickable {
                        onGameClick(
                            Pair(
                                it.game, it.winner!!
                            )
                        )
                    }
                    .animateItem(fadeInSpec = null, fadeOutSpec = null), completedGame = it)
            }
        }
    }
}


@Composable
private fun EmptyListScreen(
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Heading(
            text = "Completed games",
            modifier = Modifier.padding(start = 16.dp, top = 8.dp),
            textColor = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier)

        LazyRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(5) {
                Card(
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    ), shape = RoundedCornerShape(6.dp), colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {

                        Row(
                            Modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            Image(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(width = 1.dp, color = Color.LightGray, CircleShape),
                                contentScale = ContentScale.Crop,
                                painter = painterResource(id = R.drawable.avatar_man1),
                                contentDescription = null
                            )

                            Column(
                                modifier = Modifier.height(64.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    modifier = Modifier,
                                    text = "",
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Image(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .border(width = 1.dp, color = Color.LightGray, CircleShape),
                                    contentScale = ContentScale.Crop,
                                    painter = painterResource(id = R.drawable.avatar_man1),
                                    contentDescription = null
                                )

                                Heading(
                                    modifier = Modifier.background(
                                        Color.Gray,
                                        shape = RoundedCornerShape(4.dp)
                                    ), text = "00:00", textColor = Color.Transparent
                                )
                            }
                        }

                        Heading(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .background(Color.Gray, shape = RoundedCornerShape(4.dp)),
                            text = "Game 01",
                            textColor = Color.Transparent
                        )

                    }
                }
            }
        }

    }
}

@Composable
fun CompletedGamesLoadingScreen(
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Heading(
            text = "Completed games",
            modifier = Modifier.padding(start = 16.dp, top = 8.dp),
            textColor = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier)

        LazyRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(5) {
                Card(
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 5.dp
                    ), shape = RoundedCornerShape(6.dp), colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {

                        Row(
                            Modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            Image(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(width = 1.dp, color = Color.LightGray, CircleShape),
                                contentScale = ContentScale.Crop,
                                painter = painterResource(id = R.drawable.avatar_man1),
                                contentDescription = null
                            )

                            Column(
                                modifier = Modifier.height(64.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    modifier = Modifier,
                                    text = "",
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Image(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .border(width = 1.dp, color = Color.LightGray, CircleShape),
                                    contentScale = ContentScale.Crop,
                                    painter = painterResource(id = R.drawable.avatar_man1),
                                    contentDescription = null
                                )

                                Heading(
                                    modifier = Modifier.background(
                                        Color.Gray,
                                        shape = RoundedCornerShape(4.dp)
                                    ), text = "00:00", textColor = Color.Transparent
                                )
                            }
                        }

                        Heading(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .background(Color.Gray, shape = RoundedCornerShape(4.dp)),
                            text = "Game 01",
                            textColor = Color.Transparent
                        )

                    }
                }
            }
        }

    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun CompletedGameItem(
    modifier: Modifier = Modifier,
    completedGame: com.learningis4fun.gameshub.domain.model.CompletedGame
) {

    val winner = completedGame.winner
    val otherPlayers = completedGame.otherPlayers

    val winnerImage = winner?.imageUrl?.toIntOrNull() ?: R.drawable.game_test_profile

    val otherPlayerImage =
        otherPlayers.firstOrNull()?.imageUrl?.toIntOrNull() ?: R.drawable.avatar_person1

    val morePlayers = if (otherPlayers.size > 1) "+${otherPlayers.size - 1}" else ""

    val animatedVisibilityScope = LocalAnimatedContentScope.current

    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ), shape = RoundedCornerShape(6.dp), colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {

            Row(
                Modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                val sharedImageKey = remember {
                    sharedImageElementKey(
                        playerId = completedGame.winner?.uniqueId ?: "",
                        gameId = completedGame.game.id
                    )
                }

                println("sharedImageKey: $sharedImageKey")

                with(LocalCustomSharedTransitionScope.current) {
                    Image(
                        modifier = Modifier
                            .sharedElement(
                                rememberSharedContentState(key = sharedImageKey),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.DarkGray)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.surface,
                                CircleShape
                            ),
                        contentScale = ContentScale.Crop,
                        painter = painterResource(id = winnerImage),
                        contentDescription = null
                    )

                }

                Column(
                    modifier = Modifier.height(64.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        modifier = Modifier,
                        text = "",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Row {
                        Image(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.DarkGray)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.surface,
                                    CircleShape
                                ),
                            contentScale = ContentScale.Crop,
                            painter = painterResource(id = otherPlayerImage),
                            contentDescription = null
                        )

                        if (otherPlayers.size > 1) {
                            Box(
                                Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.surface,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = morePlayers,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }

                    }

                    Heading(
                        isUppercase = false,
                        text = completedGame.duration.toUIDuration(),
                        textColor = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Heading(
                modifier = Modifier.padding(top = 8.dp),
                text = completedGame.game.name,
                textColor = MaterialTheme.colorScheme.onBackground
            )

        }
    }
}

@Composable
fun Heading(
    modifier: Modifier = Modifier,
    text: String,
    isUppercase: Boolean = true,
    textColor: Color = Color.White
) {
    Text(
        modifier = modifier,
        text = if (isUppercase) text.uppercase() else text.lowercase(),
        color = textColor,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Normal
        )
    )
}

@Preview
@Composable
private fun CompletedGameItemPrev() {
    GamesHubAppThemePrev {
        CompletedGameItem(
            modifier = Modifier,
            completedGame = com.learningis4fun.gameshub.domain.model.CompletedGame(
                game = Game(id = "", name = "Game 01", winnerId = "id"), players = listOf(
                    Player("id", profileName = "Hlompho"),
                    Player("id2", profileName = "Dev1"),
                    Player("id3", profileName = "Dev2"),
                )
            )
        )

    }
}

@Preview
@Composable
private fun CompletedGamesScreenPrev() {
    GamesHubAppTheme {
        CompletedGamesSection(
            modifier = Modifier.fillMaxWidth(), completedGamesState = CompletedGamesState(
                completedGames = listOf(
                    com.learningis4fun.gameshub.domain.model.CompletedGame(
                        game = Game(id = "", name = "Game 01", winnerId = "id"), players = listOf(
                            Player("id", profileName = "Hlompho"),
                            Player("id2", profileName = "Android"),
                        )
                    ), com.learningis4fun.gameshub.domain.model.CompletedGame(
                        game = Game(id = "", name = "Game 01", winnerId = "id"), players = listOf(
                            Player("id", profileName = "Hlompho"),
                            Player("id2", profileName = "Android"),
                        )
                    )
                )
            )
        )
    }
}

@Preview
@Composable
private fun CompletedGamesEmptyScreenPrev() {
    GamesHubAppTheme {
        CompletedGamesSection(
            modifier = Modifier.fillMaxWidth(), completedGamesState = CompletedGamesState(
                completedGames = emptyList()
            )
        )

    }
}
