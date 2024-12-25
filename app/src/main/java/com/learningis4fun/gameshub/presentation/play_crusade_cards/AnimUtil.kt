package com.learningis4fun.gameshub.presentation.play_crusade_cards

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize

data class PlayCardSize(
    val start: IntSize = IntSize.Zero,
    val target: IntSize = IntSize.Zero
)

data class PlayCardOffset(
    val start: Offset = Offset.Zero,
    val target: Offset = Offset.Zero
)

data class PlayCardAnimDim(
    val start: CardAnimDim = CardAnimDim(),
    val target: CardAnimDim = CardAnimDim()
) {
    val startOffsetX = start.offset.x
    val startOffsetY = start.offset.y - start.size.height.div(2)

    val targetOffsetX = target.offset.x
    val targetOffsetY = target.offset.y - target.size.height.div(2)

    val cardOffset = PlayCardOffset(
        start = start.offset,
        target = target.offset
    )

    val startSizeHeight = start.size.height.toFloat()
    val startSizeWidth = start.size.width.toFloat()

    val targetSizeHeight = target.size.height.toFloat()
    val targetSizeWidth = target.size.width.toFloat()

    val cardSize = PlayCardSize(
        start = start.size,
        target = target.size
    )

}

data class CardAnimDim(
    val size: IntSize = IntSize.Zero,
    val offset: Offset = Offset.Zero
)