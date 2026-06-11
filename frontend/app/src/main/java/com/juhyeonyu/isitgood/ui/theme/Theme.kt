package com.juhyeonyu.isitgood.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary              = Cerulean,
    onPrimary            = Color.White,
    primaryContainer     = CoolSteel,
    onPrimaryContainer   = CeruleanAlt,
    secondary            = PacificBlue,
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFB3EAF5),
    onSecondaryContainer = CeruleanAlt,
    background           = Platinum,
    onBackground         = Color(0xFF0D1B2A),
    surface              = Color.White,
    onSurface            = Color(0xFF0D1B2A),
    surfaceVariant       = CoolSteel,
    onSurfaceVariant     = CeruleanAlt,
    outline              = CeruleanAlt,
    error                = Color(0xFFBA1A1A),
    onError              = Color.White,
)

@Composable
fun IsItGoodTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}