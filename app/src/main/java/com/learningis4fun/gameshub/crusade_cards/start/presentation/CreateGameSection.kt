package com.learningis4fun.gameshub.crusade_cards.start.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.learningis4fun.gameshub.R

@Composable
fun CreateGameSection(
    modifier: Modifier = Modifier,
    cardHeight: Dp = 72.dp,
    onStartClick: () -> Unit = {}
) {

    Column(
        modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        ConstraintLayout(
            Modifier.fillMaxWidth()
        ) {

            val (cloud, startImg, centreImg, endImg, text) = createRefs()

            Image(
                modifier = Modifier
                    .constrainAs(cloud) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .height(64.dp)
                    .aspectRatio(1f),
                contentScale = ContentScale.Fit,
                painter = painterResource(id = R.drawable.ic_outline_cloud),
                contentDescription = null
            )

            Image(
                modifier = Modifier
                    .constrainAs(startImg) {
                        top.linkTo(cloud.bottom)
                        end.linkTo(cloud.start)
                    }
                    .padding(start = 8.dp)
                    .height(cardHeight)
                    .aspectRatio(1f)
                    .rotate(20f),
                contentScale = ContentScale.Fit,
                painter = painterResource(id = R.drawable.heart_king),
                contentDescription = null
            )

            Image(
                modifier = Modifier
                    .constrainAs(endImg) {
                        start.linkTo(cloud.end)
                        top.linkTo(cloud.bottom)
                    }
                    .padding(end = 8.dp)
                    .height(cardHeight)
                    .aspectRatio(1f)
                    .rotate(-200f),
                contentScale = ContentScale.Fit,
                painter = painterResource(id = R.drawable.diamond_queen),
                contentDescription = null
            )

            Image(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .constrainAs(centreImg) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(cloud.bottom)
                    }
                    .height(cardHeight)
                    .aspectRatio(1f),
                contentScale = ContentScale.Fit,
                painter = painterResource(id = R.drawable.joker_red),
                contentDescription = null
            )

            Text(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .constrainAs(text) {
                        top.linkTo(centreImg.bottom)
                        start.linkTo(startImg.start)
                        end.linkTo(endImg.end)
                    }
                    .wrapContentHeight(),
                text = stringResource(id = R.string.challenge_your_phone_to_a_crusade),
                color = Color.Black,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Normal
                ),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            modifier = Modifier.padding(8.dp),
            onClick = onStartClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xffff8800)
            )
        ) {
            Icon(
                painterResource(id = R.drawable.ic_outline_phone_android),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "START")
        }
    }
}

@Preview
@Composable
private fun CreateGameSectionPrev() {
    CreateGameSection(
        Modifier
            .fillMaxWidth()
            .background(Color.White),
        cardHeight = 96.dp
    )
}