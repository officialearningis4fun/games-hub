package com.learningis4fun.gameshub.crusade_cards.start.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Card
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Game
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.toTimeFormatted
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.relations.GameWithPlayers

@Composable
fun OngoingGameSection(
    modifier: Modifier = Modifier,
    ongoingGameState: OngoingGameState,
    cardHeight: Dp = 72.dp,
    onResumeClick: () -> Unit = {}
) {

    val threeCards: List<Card> = ongoingGameState.threeCards
    val gameWithPlayers: GameWithPlayers = ongoingGameState.gameWithPlayers

    Column(
        modifier.padding(16.dp),
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
            color = Color.Black
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(gameWithPlayers.players) {
                PlayerItem(
                    modifier = Modifier.size(42.dp),
                    player = it
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onResumeClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xffff8800)
            )
        ) {
            Text(text = "RESUME")
        }
    }

}

@Composable
fun PlayerItem(
    modifier: Modifier,
    player: Player
) {

    val image = player.imageUrl.toIntOrNull() ?: R.drawable.game_test_profile

    Card(
        modifier = modifier,
        shape = CircleShape
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
        ) {

            Image(
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Crop,
                painter = painterResource(id = image),
                contentDescription = null
            )
        }
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
private fun OnGoingGameSectionPrev() {
    OngoingGameSection(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White),
        cardHeight = 96.dp,
        ongoingGameState = OngoingGameState(
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
}