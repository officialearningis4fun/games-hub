package com.learningis4fun.gameshub.presentation.start

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.commons.domain.model.Card
import com.learningis4fun.gameshub.commons.domain.model.Game
import com.learningis4fun.gameshub.commons.domain.model.Player
import com.learningis4fun.gameshub.commons.domain.util.Constants
import com.learningis4fun.gameshub.commons.domain.util.toTimeFormatted
import com.learningis4fun.gameshub.data.data_source.relations.GameWithPlayers
import com.learningis4fun.gameshub.domain.model.PlayerScreenInfo
import com.learningis4fun.gameshub.presentation.util.GamesHubAppThemePrev
import com.learningis4fun.gameshub.presentation.util.LocalAnimatedContentScope
import com.learningis4fun.gameshub.presentation.util.LocalCustomSharedTransitionScope
import com.learningis4fun.gameshub.presentation.util.sharedElementKeyOfOngoingGameSection

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun OngoingGamesSection(
    modifier: Modifier = Modifier,
    games: List<OnGoingGame>,
    onResumeClick: (String) -> Unit = {},
    onDeleteGame: (String) -> Unit = {}
) {

    val sharedTransitionScope = LocalCustomSharedTransitionScope.current
    val animatedContentScope = LocalAnimatedContentScope.current

    with(sharedTransitionScope) {
        Column(
            modifier = modifier.sharedElement(
                state = rememberSharedContentState(key = sharedElementKeyOfOngoingGameSection),
                animatedVisibilityScope = animatedContentScope
            )
        ) {
            HorizontalDivider()

            Heading(
                text = "Ongoing games",
                modifier = Modifier.padding(16.dp),
                textColor = MaterialTheme.colorScheme.onBackground
            )

            OnGoingGameItem(
                modifier = Modifier,
                ongoingGame = games.first(),
                onResumeClick = onResumeClick,
                onDeleteGame = onDeleteGame
            )
        }
    }
}

@Composable
private fun OnGoingGameItem(
    modifier: Modifier = Modifier,
    ongoingGame: OnGoingGame,
    cardHeight: Dp = 72.dp,
    onResumeClick: (String) -> Unit = {},
    onDeleteGame: (String) -> Unit = {}
) {

    val threeCards: List<Card> = ongoingGame.threeCards
    val gameWithPlayers: GameWithPlayers = ongoingGame.gameWithPlayers

    Column(
        modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        ConstraintLayout(
            Modifier.fillMaxWidth()
        ) {

            val startImage = imageOfCard(threeCards, 0)
            val centreImage = imageOfCard(threeCards, 1)
            val endImage = imageOfCard(threeCards, 2)

            val (startImg, centreImg, endImg) = createRefs()

            Image(
                modifier = Modifier
                    .constrainAs(startImg) {
                        top.linkTo(parent.top)
                        start.linkTo(centreImg.start)
                        end.linkTo(centreImg.start)
                    }
                    .padding(start = 8.dp)
                    .height(cardHeight)
                    .aspectRatio(1f)
                    .rotate(20f),
                contentScale = ContentScale.Fit,
                painter = painterResource(id = startImage),
                contentDescription = null
            )

            Image(
                modifier = Modifier
                    .constrainAs(endImg) {
                        end.linkTo(centreImg.end)
                        start.linkTo(centreImg.end)
                        top.linkTo(parent.top)
                    }
                    .padding(end = 8.dp)
                    .height(cardHeight)
                    .aspectRatio(1f)
                    .rotate(-200f),
                contentScale = ContentScale.Fit,
                painter = painterResource(id = endImage),
                contentDescription = null
            )

            Image(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .constrainAs(centreImg) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                    }
                    .height(cardHeight)
                    .aspectRatio(1f),
                contentScale = ContentScale.Fit,
                painter = painterResource(id = centreImage),
                contentDescription = null
            )
        }

        Text(
            modifier = Modifier.padding(vertical = 8.dp),
            text = gameWithPlayers.game.gameDuration.toTimeFormatted(),
            color = MaterialTheme.colorScheme.onSurface
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(gameWithPlayers.players) { player ->
                PlayerItem(
                    modifier = Modifier,
                    sizeOfPhoto = 42.dp,
                    playerScreenInfo = player.toPlayerScreenInfo()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    onResumeClick(ongoingGame.gameWithPlayers.game.id)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(text = "RESUME")
            }

            FloatingActionButton(
                modifier = Modifier.size(32.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                ),
                containerColor = Color.White,
                contentColor = Color.Black,
                onClick = { onDeleteGame(ongoingGame.gameWithPlayers.game.id) }
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Delete game"
                )
            }
        }
    }
}

@Composable
private fun PlayerItem(
    modifier: Modifier,
    sizeOfPhoto: Dp = 64.dp,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    playerScreenInfo: PlayerScreenInfo,
) {

    val image = playerScreenInfo.imageUrl?.toIntOrNull() ?: playerScreenInfo.defaultAvatar

    val isPlaying = playerScreenInfo.isCurrentlyPlaying

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
                        ),
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
            color = textColor,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )
    }

}

private fun imageOfCard(cards: List<Card>, index: Int): Int {
    if (cards.lastIndex < index) {
        return R.drawable.card_base
    }

    val cardImage = cards[index].image.toIntOrNull()

    return cardImage ?: R.drawable.card_base

}

@Preview
@Composable
private fun OnGoingGamesSectionLightPrev() {
    GamesHubAppThemePrev(darkTheme = false) {
        OngoingGamesSection(
            modifier = Modifier
                .fillMaxWidth(),
            games = ongoingGamesState.games
        )
    }
}


@Preview
@Composable
private fun OnGoingGamesSectionDarkPrev() {
    GamesHubAppThemePrev(darkTheme = true) {
        OngoingGamesSection(
            modifier = Modifier
                .fillMaxWidth(),
            games = ongoingGamesState.games
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
                        profileName = "Android",
                        imageUrl = R.drawable.avatar_boy2.toString()
                    ),
                    Player(
                        uniqueId = "id2",
                        profileName = "Hlompho",
                        imageUrl = R.drawable.ai_platform.toString()
                    )
                )
            )
        )
    )
)