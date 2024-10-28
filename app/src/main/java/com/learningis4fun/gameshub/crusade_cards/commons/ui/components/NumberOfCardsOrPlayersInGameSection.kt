package com.learningis4fun.gameshub.crusade_cards.commons.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.automirrored.sharp.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.toDoubleDigits

@Composable
fun NumberOfCardsOrPlayersInGameSection(
    modifier: Modifier,
    title: String = "Number of cards per player",
    count: Int,
    onDecreaseNumber: () -> Unit,
    onIncreaseNumber: () -> Unit,
) {

    Column(
        modifier = modifier,
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = title,
            style = MaterialTheme.typography.labelLarge
        )

        Row(
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp)
                .border(
                    width = 1.dp,
                    MaterialTheme.colorScheme.onBackground,
                    shape = MaterialTheme.shapes.medium
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                modifier = Modifier,
                onClick = onDecreaseNumber
            ) {
                Icon(
                    modifier = Modifier
                        .rotate(90f),
                    imageVector = Icons.AutoMirrored.Sharp.ArrowForward,
                    contentDescription = "decrease"
                )
            }

            VerticalDivider(
                modifier = Modifier.height(24.dp)
            )

            Text(
                modifier = Modifier.padding(horizontal = 24.dp),
                text = count.toDoubleDigits(),
                style = MaterialTheme.typography.titleLarge
            )

            VerticalDivider(
                modifier = Modifier.height(24.dp)
            )

            IconButton(
                modifier = Modifier,
                onClick = onIncreaseNumber
            ) {
                Icon(
                    modifier = Modifier
                        .rotate(90f),
                    imageVector = Icons.AutoMirrored.Sharp.ArrowBack,
                    contentDescription = "Increase"
                )
            }
        }
    }
}

