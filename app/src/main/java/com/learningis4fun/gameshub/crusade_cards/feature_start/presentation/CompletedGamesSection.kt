package com.learningis4fun.gameshub.crusade_cards.feature_start.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Game
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.toUIDuration
import com.learningis4fun.gameshub.crusade_cards.commons.ui.theme.GamesHubAppTheme
import com.learningis4fun.gameshub.crusade_cards.feature_start.domain.model.CompletedGame

@Composable
fun CompletedGamesSection(
    modifier: Modifier = Modifier,
    completedGamesState: CompletedGamesState,
    onGameClick: (String) -> Unit = {}
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
    onGameClick: (String) -> Unit = {}
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
                CompletedGameItem(
                    modifier = Modifier
                        .clickable { onGameClick(it.game.id) }
                        .animateItem(fadeInSpec = null, fadeOutSpec = null),
                    completedGame = it
                )
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
            modifier = Modifier
                .padding(start = 16.dp, top = 8.dp),
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
                    ),
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {

                        Row(
                            Modifier,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                    modifier = Modifier
                                        .background(Color.Gray, shape = RoundedCornerShape(4.dp)),
                                    text = "00:00",
                                    textColor = Color.Transparent
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
            modifier = Modifier
                .padding(start = 16.dp, top = 8.dp),
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
                    ),
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {

                        Row(
                            Modifier,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                    modifier = Modifier
                                        .background(Color.Gray, shape = RoundedCornerShape(4.dp)),
                                    text = "00:00",
                                    textColor = Color.Transparent
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
private fun CompletedGameItem(
    modifier: Modifier,
    completedGame: CompletedGame
) {

    val winner = completedGame.winner
    val otherPlayers = completedGame.otherPlayers

    val winnerImage = winner?.imageUrl?.toIntOrNull() ?: R.drawable.game_test_profile
    val otherPlayersImages = otherPlayers.map {
        it.imageUrl.toIntOrNull() ?: R.drawable.avatar_person1
    }

    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(
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
                Modifier,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Image(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.surface,
                            CircleShape
                        ),
                    contentScale = ContentScale.Crop,
                    painter = painterResource(id = winnerImage),
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
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Row {
                        otherPlayersImages.take(3).map { playerImage ->
                            Image(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.surface,
                                        CircleShape
                                    ),
                                contentScale = ContentScale.Crop,
                                painter = painterResource(id = playerImage),
                                contentDescription = null
                            )
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
    GamesHubAppTheme {
        CompletedGameItem(
            modifier = Modifier,
            completedGame = CompletedGame(
                game = Game(id = "", name = "Game 01", winnerId = "id"),
                players = listOf(
                    Player("id", profileName = "Hlompho"),
                    Player("id2", profileName = "Android"),
                )
            )
        )
    }
}

@Preview
@Composable
private fun CompletedGameItemDarkPrev() {
    GamesHubAppTheme(darkTheme = true) {
        CompletedGameItem(
            modifier = Modifier,
            completedGame = CompletedGame(
                game = Game(id = "", name = "Game 01", winnerId = "id"),
                players = listOf(
                    Player("id", profileName = "Hlompho"),
                    Player("id2", profileName = "Android"),
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
            modifier = Modifier
                .fillMaxWidth(),
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
            )
        )
    }
}

@Preview
@Composable
private fun CompletedGamesScreenDarkPrev() {
    GamesHubAppTheme(darkTheme = true) {
        CompletedGamesSection(
            modifier = Modifier
                .fillMaxWidth(),
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
            )
        )
    }
}

@Preview
@Composable
private fun CompletedGamesEmptyScreenPrev() {
    GamesHubAppTheme(darkTheme = false) {
        CompletedGamesSection(
            modifier = Modifier
                .background(Color.White)
                .fillMaxWidth(),
            completedGamesState = CompletedGamesState(
                completedGames = emptyList()
            )
        )
    }
}

@Preview
@Composable
private fun CompletedGamesEmptyScreenDarkPrev() {
    GamesHubAppTheme(darkTheme = true) {
        CompletedGamesSection(
            modifier = Modifier
                .fillMaxWidth(),
            completedGamesState = CompletedGamesState(
                completedGames = emptyList()
            )
        )
    }
}