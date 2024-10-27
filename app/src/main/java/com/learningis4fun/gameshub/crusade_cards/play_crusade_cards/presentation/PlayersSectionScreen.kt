package com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.crusade_cards.commons.ui.theme.GamesHubAppTheme
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.PlayerScreenInfo

@Composable
fun MultiplePlayersSectionScreen(
    modifier: Modifier = Modifier,
    playersScreenInfo: List<PlayerScreenInfo>,
    duration: String
) {

    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        Row(
            modifier = Modifier.padding(start = 32.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Heading(
                modifier = Modifier,
                text = duration,
                textColor = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))

            LazyRow(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(playersScreenInfo) { _, player ->
                    PlayerItem(
                        modifier = Modifier,
                        playerScreenInfo = player
                    )
                }
            }
        }
    }

}

@Composable
fun PlayersSectionScreen(
    modifier: Modifier = Modifier,
    playersScreenInfo: List<PlayerScreenInfo>,
    duration: String
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
                    playerScreenInfo = player1
                )
                Heading(
                    modifier = Modifier,
                    text = duration,
                    textColor = MaterialTheme.colorScheme.onPrimary
                )

                PlayerItem(
                    modifier = Modifier,
                    playerScreenInfo = player2
                )
            }

        }
    }

}

@Composable
private fun PlayerItem(
    modifier: Modifier,
    playerScreenInfo: PlayerScreenInfo
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
                        .size(64.dp)
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

@Preview
@Composable
private fun PlayersSectionLightPrev() {
    GamesHubAppTheme {
        PlayersSectionScreen(
            playersScreenInfo = playersScreenInfo.take(2), duration = "00:20"
        )
    }
}

@Preview
@Composable
private fun PlayersSectionDarkPrev() {
    GamesHubAppTheme(darkTheme = true) {
        PlayersSectionScreen(
            playersScreenInfo = playersScreenInfo.take(2), duration = "00:20"
        )
    }
}

@Preview
@Composable
private fun MultiPlayersSectionLightPrev() {
    GamesHubAppTheme {
        MultiplePlayersSectionScreen(
            playersScreenInfo = playersScreenInfo,
            duration = "00:20"
        )
    }
}

@Preview
@Composable
private fun MultiPlayersSectionDarkPrev() {
    GamesHubAppTheme(darkTheme = true) {
        MultiplePlayersSectionScreen(
            playersScreenInfo = playersScreenInfo,
            duration = "00:20"
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