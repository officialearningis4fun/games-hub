package com.learningis4fun.gameshub.crusade_cards.start.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.toTimeFormatted
import com.learningis4fun.gameshub.crusade_cards.start.domain.model.CompletedGame

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompletedGamesSection(
    modifier: Modifier = Modifier,
    completedGamesState: CompletedGamesState
) {

    val completedGames = completedGamesState.completedGames

    Column(modifier) {
        Heading(
            text = "Completed games",
            modifier = Modifier.padding(start = 16.dp, top = 8.dp),
            textColor = Color.Black
        )
        Spacer(modifier = Modifier)

        if (completedGames.isEmpty()) {
            EmptyListScreen()
        } else {
            LazyRow(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(completedGames) {
                    CompletedGameItem(
                        modifier = Modifier.animateItemPlacement(),
                        completedGame = it
                    )
                }
            }
        }

    }

}

@Composable
private fun EmptyListScreen() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier.height(64.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {}

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Heading(text = "")
            Heading(text = "")
        }
    }
}

@Composable
private fun CompletedGameItem(
    modifier: Modifier,
    completedGame: CompletedGame
) {

    val winner = completedGame.winner
    val otherPlayer = completedGame.otherPlayer

    val winnerImage = winner?.imageUrl?.toIntOrNull() ?: R.drawable.game_test_profile
    val otherPlayerImage = otherPlayer?.imageUrl?.toIntOrNull() ?: R.drawable.game_test_profile

    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(64.dp)
                ) {

                    Image(
                        modifier = Modifier.size(64.dp),
                        contentScale = ContentScale.Crop,
                        painter = painterResource(id = winnerImage),
                        contentDescription = null
                    )

                    Text(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp),
                        text = winner?.profileName ?: "",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .width(64.dp)
                ) {

                    Image(
                        modifier = Modifier.size(64.dp),
                        contentScale = ContentScale.Crop,
                        painter = painterResource(id = otherPlayerImage),
                        contentDescription = null
                    )

                    Text(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp),
                        text = otherPlayer?.profileName ?: "",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Heading(text = completedGame.game.name, textColor = Color.Black)
                Heading(text = completedGame.duration.toTimeFormatted(), textColor = Color.Black)
            }
        }
    }
}

@Composable
fun Heading(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color = Color.White
) {
    Text(
        modifier = modifier,
        text = text.uppercase(),
        color = textColor,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Normal
        )
    )
}

@Preview
@Composable
private fun CompletedGamesScreenPrev() {
    CompletedGamesSection(
        modifier = Modifier
            .background(Color.White)
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