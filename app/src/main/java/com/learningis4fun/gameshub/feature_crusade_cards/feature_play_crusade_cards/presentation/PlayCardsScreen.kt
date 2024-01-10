package com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.Card
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.util.JokerActions
import com.learningis4fun.gameshub.feature_crusade_cards.commons.ui.theme.green
import com.learningis4fun.gameshub.feature_crusade_cards.commons.ui.theme.red
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.PlayerScreenInfo
import com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.domain.model.UserCard

@Composable
fun PlayCardsScreen(
    modifier: Modifier = Modifier,
    playCardsState: PlayCardsState,
    onEvent: (PlayCardsEvent) -> Unit = {},
    winner: (Player?) -> Unit = {}
) {

    if (playCardsState.winner != null) {
        winner(playCardsState.winner)
    }

    Box(modifier, contentAlignment = Alignment.CenterEnd) {
        Column(modifier = Modifier.fillMaxSize()) {
            PlayersSectionScreen(
                modifier = Modifier
                    .fillMaxWidth(),
                playersScreenInfo = playCardsState.playersScreenInfo
            )

            PlayedAndDustbinSection(
                playedCards = playCardsState.playedCards,
                showAllowOthersPlay = playCardsState.allowOthersPlay,
                onDustbinClick = {
                    onEvent(PlayCardsEvent.PickCardFromDustbin)
                },
                onAllowOthersPlay = {
                    onEvent(PlayCardsEvent.AllowOthersPlay)
                },
                showCurrentCardType = playCardsState.showCurrentCardTypeAndImage
            )

            MessagesSection(
                modifier = Modifier.fillMaxWidth(),
                message = playCardsState.message
            )

            UserCardsSection(
                modifier = Modifier.fillMaxWidth(),
                cards = playCardsState.userUnPlayedCards,
                onClick = {
                    onEvent(PlayCardsEvent.PlayCard(it))
                }
            )
        }

        if (playCardsState.showDefineJokerActions) {
            DefineJokerActionScreen(
                modifier = Modifier.padding(end = 16.dp),
                onClick = {
                    onEvent(PlayCardsEvent.DefineJokerAction(it))
                }
            )
        }

        if (playCardsState.showChooseCardTypeScreen) {
            ChooseCardTypeScreen(
                modifier = Modifier,
                onClick = {
                    onEvent(PlayCardsEvent.SetNewCardType(it))
                }
            )
        }
    }

}

@Composable
fun PlayersSectionScreen(
    modifier: Modifier = Modifier,
    playersScreenInfo: List<PlayerScreenInfo>
) {

    Column(
        modifier = modifier.padding(top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = "PLAYERS",
            color = Color.White
        )

        LazyRow(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {

            items(playersScreenInfo) {
                PlayerItem(
                    modifier = Modifier,
                    playerScreenInfo = it
                )
            }
        }
    }

}

@Composable
fun PlayerItem(
    modifier: Modifier,
    playerScreenInfo: PlayerScreenInfo
) {

    val image = playerScreenInfo.imageUrl?.toIntOrNull() ?: playerScreenInfo.defaultAvatar

    val isPlaying = playerScreenInfo.isCurrentlyPlaying

    Card(
        modifier = modifier,
        border = BorderStroke(
            width = 2.dp,
            color = if (isPlaying) Color.White else Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
        ) {

            Image(
                modifier = Modifier.size(64.dp),
                alpha = 0.7f,
                contentScale = ContentScale.Crop,
                painter = painterResource(id = image),
                contentDescription = null
            )

            Text(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                text = playerScreenInfo.playerName,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )

            Text(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(4.dp),
                text = playerScreenInfo.numOfCardsLeft,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )

        }
    }

}

@Composable
private fun PlayedAndDustbinSection(
    modifier: Modifier = Modifier,
    playedCards: List<UserCard>,
    showAllowOthersPlay: Boolean,
    showCurrentCardType: Pair<Boolean, String?>,
    onDustbinClick: () -> Unit,
    onAllowOthersPlay: () -> Unit
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayedCardsSection(
            modifier = Modifier.weight(1f),
            cards = playedCards,
            showCurrentCardType = showCurrentCardType
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Heading(modifier = Modifier, text = "00:00")
            Text(text = "Duration", color = Color.White)
        }

        DustbinSection(
            modifier = Modifier.weight(1f),
            onDustbinClick = onDustbinClick,
            onAllowOthersPlay = onAllowOthersPlay,
            showAllowOthersPlay = showAllowOthersPlay
        )
    }

}

@Composable
private fun PlayedCardsSection(
    modifier: Modifier,
    cards: List<UserCard>,
    showCurrentCardType: Pair<Boolean, String?>
) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Heading(modifier = Modifier, text = "Played")
        val card = cards.firstOrNull()
        val image = card?.image?.toIntOrNull() ?: R.drawable.card_base

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.7f),
                painter = painterResource(id = image),
                contentDescription = null
            )
            CurrentCardTypeScreen(
                modifier = Modifier,
                showCurrentCardType = showCurrentCardType
            )
        }
    }
}

@Composable
private fun CurrentCardTypeScreen(
    modifier: Modifier,
    showCurrentCardType: Pair<Boolean, String?>
) {

    showCurrentCardType.second?.let {
        val show = showCurrentCardType.first
        if (show) {
            val cardTypeImage = it.toIntOrNull()
            cardTypeImage?.let {
                Column(
                    modifier = modifier
                        .clip(RoundedCornerShape(8.dp))
                        .fillMaxWidth()
                        .aspectRatio(0.7f)
                        .background(Color.White.copy(alpha = 0.5f))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Image(
                        modifier = Modifier.size(48.dp),
                        painter = painterResource(id = cardTypeImage),
                        contentDescription = null
                    )

                }
            }
        }
    }


}

@Composable
private fun DustbinSection(
    modifier: Modifier,
    showAllowOthersPlay: Boolean,
    onDustbinClick: () -> Unit,
    onAllowOthersPlay: () -> Unit
) {

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Heading(modifier = Modifier, text = "Dustbin")

        Box(
        ) {
            Image(
                modifier = Modifier
                    .clickable { onDustbinClick() }
                    .fillMaxWidth()
                    .aspectRatio(0.7f),
                painter = painterResource(id = R.drawable.back),
                contentDescription = null
            )
            if (showAllowOthersPlay) {
                AllowOthersPlayScreen(
                    onClick = onAllowOthersPlay
                )
            }
        }
    }

}

@Composable
private fun MessagesSection(
    modifier: Modifier,
    message: String
) {

    Box(
        modifier
            .padding(16.dp)
            .border(
                width = 1.dp,
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = message, color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1
        )
    }

}


@Composable
private fun UserCardsSection(
    modifier: Modifier,
    cards: List<UserCard>,
    onClick: (UserCard) -> Unit
) {

    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Heading(modifier = Modifier, text = "Your Cards")

        UserCardsSummary(
            modifier = Modifier.fillMaxWidth(),
            cards = cards.map {
                Card(
                    id = it.cardId,
                    name = it.name,
                    type = it.type,
                    image = it.image
                )
            })

        LazyHorizontalGrid(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            rows = GridCells.Fixed(1),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cards) {
                val image = it.image.toIntOrNull() ?: R.drawable.card_base
                Image(
                    modifier = Modifier
                        .clickable { onClick(it) }
                        .aspectRatio(0.7f),
                    painter = painterResource(id = image),
                    contentDescription = it.name
                )
            }
        }
    }

}

@Composable
fun UserCardsSummary(
    modifier: Modifier,
    cards: List<Card>
) {

    val spade = cards.count { it.type == Constants.CardTypes.SPADE }
    val club = cards.count { it.type == Constants.CardTypes.CLUBS }
    val heart = cards.count { it.type == Constants.CardTypes.HEART }
    val diamond = cards.count { it.type == Constants.CardTypes.DIAMOND }

    Row(
        modifier = modifier.padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        UserCardsSummaryItem(
            text = spade.toString(),
            icon = R.drawable.suit_spade
        )
        UserCardsSummaryItem(
            text = club.toString(),
            icon = R.drawable.suit_club
        )
        UserCardsSummaryItem(
            text = heart.toString(),
            icon = R.drawable.suit_heart
        )
        UserCardsSummaryItem(
            text = diamond.toString(),
            icon = R.drawable.suit_diamond
        )
    }
}

@Composable
fun UserCardsSummaryItem(
    modifier: Modifier = Modifier,
    text: String,
    icon: Int
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = icon),
            contentDescription = null
        )
        Text(text = text)
    }
}


@Composable
private fun ChooseCardTypeScreen(
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit
) {

    Box {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Heading(
                modifier = Modifier,
                text = "Choose card type",
                textColor = Color.Black
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = {
                        onClick(Constants.CardTypes.SPADE)
                    },
                    content = {
                        Icon(
                            painter = painterResource(id = R.drawable.suit_spade),
                            contentDescription = "spade"
                        )
                    }
                )
                IconButton(
                    onClick = {
                        onClick(Constants.CardTypes.CLUBS)
                    },
                    content = {
                        Icon(
                            painter = painterResource(id = R.drawable.suit_club),
                            contentDescription = "spade"
                        )
                    }
                )
                IconButton(
                    onClick = {
                        onClick(Constants.CardTypes.HEART)
                    },
                    content = {
                        Icon(
                            tint = Color.Red,
                            painter = painterResource(id = R.drawable.suit_heart),
                            contentDescription = "spade"
                        )
                    }
                )
                IconButton(
                    onClick = {
                        onClick(Constants.CardTypes.DIAMOND)
                    },
                    content = {
                        Icon(
                            tint = Color.Red,
                            painter = painterResource(id = R.drawable.suit_diamond),
                            contentDescription = "spade"
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun DefineJokerActionScreen(
    modifier: Modifier,
    onClick: (JokerActions) -> Unit
) {
    Box {
        Column(
            modifier = modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            JokerActionButton(
                modifier = Modifier,
                onClick = {
                    onClick(JokerActions.Hit)
                },
                text = "Hit",
                imageResource = R.drawable.ic_baseline_fire,
                buttonColor = red
            )

            JokerActionButton(
                modifier = Modifier,
                onClick = {
                    onClick(JokerActions.Hit)
                },
                text = "Change",
                imageResource = R.drawable.ic_baseline_swap,
                buttonColor = green
            )
        }
    }
}

@Composable
private fun JokerActionButton(
    modifier: Modifier,
    onClick: () -> Unit,
    text: String,
    imageResource: Int,
    buttonColor: Color
) {
    Box {
        Column(
            modifier = modifier
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier
                    .clip(RectangleShape)
                    .size(56.dp)
                    .aspectRatio(1f)
                    .clickable { onClick() }
                    .border(color = Color.LightGray, width = 1.dp)
                    .padding(8.dp),
                painter = painterResource(id = imageResource),
                contentDescription = "spade",
                colorFilter = ColorFilter.tint(buttonColor)
            )
            Heading(
                modifier = Modifier,
                text = text,
                textColor = Color.Black
            )
        }
    }
}

@Composable
private fun AllowOthersPlayScreen(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Column(
        modifier = modifier
            .clickable { onClick() }
            .clip(RoundedCornerShape(8.dp))
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            modifier = Modifier.padding(bottom = 16.dp),
            text = "Allow others play",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Icon(
            modifier = Modifier.size(36.dp),
            imageVector = Icons.Default.Check,
            contentDescription = null
        )

    }

}

@Composable
private fun Heading(
    modifier: Modifier,
    text: String,
    textColor: Color = Color.White
) {
    Text(
        modifier = modifier.padding(vertical = 8.dp),
        text = text.uppercase(),
        color = textColor
    )
}

@Preview
@Composable
fun PlayCardsScreenPrev() {
    PlayCardsScreen(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        playCardsState = PlayCardsState(
            playersScreenInfo = listOf(
                PlayerScreenInfo(
                    "id1",
                    "Hlompho",
                    "",
                    R.drawable.game_test_profile,
                    "7",
                    false,
                ),
                PlayerScreenInfo(
                    "id2",
                    "Android",
                    "",
                    R.drawable.game_test_profile,
                    "4",
                    true,
                )
            ),
            showCurrentCardType = Pair(true, Constants.CardTypes.CLUBS),
            showDefineJokerActions = true,
            playedCards = listOf(
                UserCard(
                    gameCardId = "",
                    playerId = "id2",
                    cardId = "1d",
                    name = "",
                    type = "",
                    gameId = "id",
                    isPlayed = true,
                    image = R.drawable.diamond_king.toString(),
                    playTime = 0L,
                    pickedTime = 0L
                )
            ),
            message = "We getting the game ready for you...",
            userUnPlayedCards = Constants.Cards.cards.take(10).map {
                UserCard(
                    gameCardId = "game",
                    playerId = "id2",
                    cardId = it.id,
                    name = it.name,
                    type = it.type,
                    gameId = "id",
                    isPlayed = true,
                    image = it.image,
                    playTime = 0L,
                    pickedTime = 0L
                )
            }
        )
    )
}

