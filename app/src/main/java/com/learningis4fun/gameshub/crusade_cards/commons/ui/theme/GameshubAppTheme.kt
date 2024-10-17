package com.learningis4fun.gameshub.crusade_cards.commons.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = orange_500,
    onPrimary = white,
    primaryContainer = primaryContainerDark,
    secondary = orange_200,
    secondaryContainer = surfaceVariantDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    inversePrimary = inversePrimaryDark,
    onError = white,
    errorContainer = red
)

private val LightColors = lightColorScheme(
    primary = orange_500,
    onPrimary = black,
    primaryContainer = orange_500,
    secondary = orange_200,
    secondaryContainer = white,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurface = onSurfaceLight,
    inversePrimary = inversePrimaryLight,
    onError = white,
    errorContainer = red

)


@Composable
fun GamesHubAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}