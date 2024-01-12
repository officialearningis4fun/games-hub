package com.learningis4fun.gameshub.crusade_cards.feature_play_crusade_cards.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.feature_play_crusade_cards.domain.model.UserCard

@Composable
fun PlayedCardsSection(
    modifier: Modifier,
    cards: List<UserCard>,
    showCurrentCardType: Pair<Boolean, String?>
) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Heading(modifier = Modifier, text = "Played")

        val card3Img = getImageOrDefault(cards, 0)
        val card2Img = getImageOrDefault(cards, 1)
        val card1Img = getImageOrDefault(cards, 2)

        Box(
            modifier = Modifier,
            contentAlignment = Alignment.Center
        ) {
            ConstraintLayout(
                modifier = Modifier
            ) {

                val (startImg, centreImg, endImg, img) = createRefs()

                val centreGuideline = createGuidelineFromStart(0.5f)

                Image(
                    modifier = Modifier
                        .constrainAs(startImg) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                        }
                        .aspectRatio(0.7f),
                    painter = painterResource(id = card1Img),
                    contentDescription = null
                )
                Image(
                    modifier = Modifier
                        .constrainAs(centreImg) {
                            top.linkTo(parent.top)
                            start.linkTo(startImg.start)
                            end.linkTo(endImg.end)
                        }
                        .aspectRatio(0.7f),
                    painter = painterResource(id = card2Img),
                    contentDescription = null
                )

                Image(
                    modifier = Modifier
                        .constrainAs(endImg) {
                            top.linkTo(parent.top)
                            start.linkTo(centreGuideline)
                            end.linkTo(parent.end)
                        }
                        .aspectRatio(0.7f),
                    painter = painterResource(id = card3Img),
                    contentDescription = null
                )
                CurrentCardTypeScreen(
                    modifier = Modifier
                        .constrainAs(img) {
                            top.linkTo(endImg.top)
                            start.linkTo(endImg.start)
                            end.linkTo(endImg.end)
                            bottom.linkTo(endImg.bottom)
                        }.aspectRatio(0.7f),
                    showCurrentCardType = showCurrentCardType
                )

            }
        }


    }
}

private fun getImageOrDefault(cards: List<UserCard>, index: Int): Int {
    return if (cards.lastIndex >= index) cards[index].image.toIntOrNull()
        ?: R.drawable.card_base else R.drawable.card_base
}

@Composable
private fun CurrentCardTypeScreen(
    modifier: Modifier,
    showCurrentCardType: Pair<Boolean, String?>
) {

    showCurrentCardType.second?.toIntOrNull()?.let { cardTypeImage ->
        val show = showCurrentCardType.first
        if (show) {
            Box(modifier) {
                Image(
                    modifier = Modifier
                        .aspectRatio(0.7f),
                    painter = painterResource(id = R.drawable.card_base),
                    contentDescription = null
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    (0..2).forEach { _ ->
                        Image(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(32.dp),
                            painter = painterResource(id = cardTypeImage),
                            contentDescription = null
                        )

                    }
                }

            }
        }
    }


}

@Preview
@Composable
private fun PlayedCardsSectionPrev() {
    val color = Color(0xffff8800)
    PlayedCardsSection(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = color),
        cards = listOf(
            UserCard(
                gameCardId = "1",
                playerId = "id2",
                cardId = "1d1",
                name = "",
                type = "",
                gameId = "id",
                isPlayed = true,
                image = R.drawable.diamond_king.toString(),
                playTime = 0L,
                pickedTime = 0L
            ),
            UserCard(
                gameCardId = "1",
                playerId = "id2",
                cardId = "1d2",
                name = "",
                type = "",
                gameId = "id",
                isPlayed = true,
                image = R.drawable.diamond_2.toString(),
                playTime = 0L,
                pickedTime = 0L
            ),
            UserCard(
                gameCardId = "1",
                playerId = "id2",
                cardId = "1d3",
                name = "",
                type = "",
                gameId = "id",
                isPlayed = true,
                image = R.drawable.diamond_3.toString(),
                playTime = 0L,
                pickedTime = 0L
            )
        ),
        showCurrentCardType = Pair(true, R.drawable.suit_club.toString()),
    )
}