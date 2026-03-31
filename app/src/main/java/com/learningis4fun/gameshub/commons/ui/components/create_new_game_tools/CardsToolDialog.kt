package com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.learningis4fun.gameshub.R
import com.learningis4fun.gameshub.commons.domain.util.toDoubleDigits
import com.learningis4fun.gameshub.presentation.util.GamesHubAppThemePrev

@Composable
fun CardsToolDialog(
    modifier: Modifier = Modifier,
    onDismissRequested: () -> Unit = {},
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    showDialog: Boolean = false,
    numOfCardsPerPlayer: Int
) {
    if (showDialog) {
        NewGameToolBaseDialog(onDismissRequest = { onDismissRequested() }) {
            Column(
                modifier = modifier.background(MaterialTheme.colorScheme.background)
            ) {
                HorizontalDivider()

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = "Number of cards per player"
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    modifier = Modifier.height(144.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    item {
                        IncreaseDecreaseItem(
                            modifier = Modifier.padding(end = 8.dp),
                            count = numOfCardsPerPlayer,
                            onDecrease = onDecrease,
                            onIncrease = onIncrease
                        )
                    }
                    items((0..5).toList()) {
                        BackCardItem(
                            modifier = Modifier.fillMaxHeight()
                        )
                    }
                }

            }
        }
    }
}

@Composable
private fun IncreaseDecreaseItem(
    modifier: Modifier = Modifier,
    count: Int,
    onDecrease: () -> Unit = {},
    onIncrease: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .border(
                width = 1.dp,
                MaterialTheme.colorScheme.onBackground,
                shape = MaterialTheme.shapes.medium
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            modifier = Modifier,
            onClick = { onIncrease() }
        ) {
            Icon(
                modifier = Modifier
                    .rotate(-90f),
                imageVector = Icons.AutoMirrored.Sharp.ArrowForward,
                contentDescription = "increase"
            )
        }

        HorizontalDivider(
            modifier = Modifier.width(24.dp)
        )

        Text(
            modifier = Modifier.padding(vertical = 8.dp),
            text = count.toDoubleDigits(),
            style = MaterialTheme.typography.titleLarge
        )

        HorizontalDivider(
            modifier = Modifier.width(24.dp)
        )

        IconButton(
            modifier = Modifier,
            onClick = { onDecrease() }
        ) {
            Icon(
                modifier = Modifier
                    .rotate(90f),
                imageVector = Icons.AutoMirrored.Sharp.ArrowForward,
                contentDescription = "Increase"
            )
        }
    }

}

@Composable
private fun BackCardItem(
    modifier: Modifier = Modifier,
) {
    val imageRes = R.drawable.back

    Column(
        modifier = modifier
            .fillMaxHeight()
            .aspectRatio(0.7f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
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

    }

}

@Composable
@Preview
private fun CardsToolDialogPrev() {
    GamesHubAppThemePrev {
        CardsToolDialog(
            onDecrease = { /*TODO*/ },
            onIncrease = { /*TODO*/ },
            numOfCardsPerPlayer = 4
        )
    }
}
