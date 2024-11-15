package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Card
import com.learningis4fun.gameshub.crusade_cards.commons.ui.theme.GamesHubAppTheme
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.PlayerScreenInfo

@Composable
fun MultiplePlayersSectionScreen(
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    playersScreenInfo: List<PlayerScreenInfo>,
) {

    val playersListState = rememberLazyListState()

    LaunchedEffect(key1 = playersScreenInfo) {
        val player = playersScreenInfo.firstOrNull { it.isCurrentlyPlaying }

        if (player != null) {
            val index = playersScreenInfo.indexOf(player)
            playersListState.animateScrollToItem(index = index)
        }
    }

    LazyRow(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            space = 16.dp,
            alignment = Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        itemsIndexed(playersScreenInfo) { _, player ->
            PlayerItem(
                modifier = Modifier,
                textColor = textColor,
                playerScreenInfo = player
            )
        }
    }


}

@Composable
fun PlayersSectionScreen(
    modifier: Modifier = Modifier,
    playersScreenInfo: List<PlayerScreenInfo>,
    duration: String,
    textColor: Color = MaterialTheme.colorScheme.onPrimary
) {

    val playerImgHeight = 64.dp

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val player1 = if (playersScreenInfo.isNotEmpty()) playersScreenInfo[0] else null

        val player2 = if (playersScreenInfo.size >= 2) playersScreenInfo[1] else null

        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (player2 == null || player1 == null) {
                Spacer(modifier = Modifier.height(playerImgHeight))
            } else {

                PlayerItem(
                    modifier = Modifier,
                    textColor = textColor,
                    playerScreenInfo = player1
                )
                Heading(
                    modifier = Modifier,
                    text = duration,
                    textColor = textColor
                )

                PlayerItem(
                    modifier = Modifier,
                    textColor = textColor,
                    playerScreenInfo = player2
                )
            }

        }
    }

}

@Composable
fun PlayerItem(
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

@Composable
fun CardItem(
    modifier: Modifier = Modifier,
    card: Card,
    count: Int = 0
) {
    val imageRes = card.image.toIntOrNull() ?: R.drawable.card_base

    Column(
        modifier = modifier
            .fillMaxHeight()
            .aspectRatio(0.7f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
        ) {
            Card(
                modifier = Modifier,
                shape = RoundedCornerShape(4.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Image(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(0.7f),
                    contentScale = ContentScale.Crop,
                    painter = painterResource(id = imageRes),
                    contentDescription = null
                )

            }
            if (count != 0) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(horizontal = 1.dp)
                        .background(
                            color = MaterialTheme.colorScheme.error,
                            shape = RectangleShape
                        )
                        .padding(8.dp),
                    text = count.toString(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onError,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

    }

}

@Preview
@Composable
private fun PlayerItemLightPrev() {
    GamesHubAppTheme {
        PlayerItem(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background),
            playerScreenInfo = playersScreenInfo.first()
        )
    }
}

@Preview
@Composable
private fun PlayersSectionLightPrev() {
    GamesHubAppTheme {
        PlayersSectionScreen(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background),
            playersScreenInfo = playersScreenInfo.take(2), duration = "00:20"
        )
    }
}

@Preview
@Composable
private fun PlayersSectionDarkPrev() {
    GamesHubAppTheme(darkTheme = true) {
        PlayersSectionScreen(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background),
            playersScreenInfo = playersScreenInfo.take(2), duration = "00:20"
        )
    }
}

@Preview
@Composable
private fun MultiPlayersSectionLightPrev() {
    GamesHubAppTheme {
        MultiplePlayersSectionScreen(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background),
            playersScreenInfo = playersScreenInfo,
        )
    }
}

@Preview
@Composable
private fun MultiPlayersSectionDarkPrev() {
    GamesHubAppTheme(darkTheme = true) {
        MultiplePlayersSectionScreen(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background),
            playersScreenInfo = playersScreenInfo,
        )
    }
}

private val playersScreenInfo = listOf(
    PlayerScreenInfo(
        "id1",
        "You",
        "",
        R.drawable.game_test_profile,
        "7",
        false,
    ),
    PlayerScreenInfo(
        "id2",
        "Dev1",
        "",
        R.drawable.game_test_profile,
        "4",
        true,
    ),
    PlayerScreenInfo(
        "id2",
        "Dev2",
        "",
        R.drawable.game_test_profile,
        "4",
        true,
    ),
    PlayerScreenInfo(
        "id3",
        "Dev3",
        "",
        R.drawable.game_test_profile,
        "4",
        true,
    ),
    PlayerScreenInfo(
        "id4",
        "Dev4",
        "",
        R.drawable.game_test_profile,
        "4",
        true,
    )
)