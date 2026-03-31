package com.learningis4fun.gameshub.commons.ui.components.create_new_game_tools

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun ToolChip(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    imageVector: ImageVector? = null,
    imageVectorTint: Color? = null,
    @DrawableRes icon: Int? = null,
    label: String
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(
                width = (0.5).dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (imageVector != null && imageVectorTint != null) {

            Icon(
                imageVector = imageVector,
                contentDescription = "",
                tint = imageVectorTint
            )
        }

        if (icon != null) {
            Image(
                modifier = Modifier
                    .height(24.dp)
                    .padding(2.dp)
                    .aspectRatio(0.7f),
                painter = painterResource(id = icon),
                contentDescription = "",
            )
        }

        Text(
            modifier = Modifier,
            text = label,
            style = MaterialTheme.typography.labelMedium
        )

        Icon(
            modifier = Modifier.size(18.dp),
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = ""
        )
    }

}