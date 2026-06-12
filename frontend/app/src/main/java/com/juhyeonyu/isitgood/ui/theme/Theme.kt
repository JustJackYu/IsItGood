package com.juhyeonyu.isitgood.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

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

// Maps a stored font-size preference to a multiplier applied on top of the device's own font scale.
fun fontScaleFor(fontSize: String): Float = when (fontSize) {
    "SMALL" -> 0.9f
    "LARGE" -> 1.15f
    else -> 1f // MEDIUM or unknown
}

@Composable
fun IsItGoodTheme(
    fontScale: Float = 1f,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography
    ) {
        // Override the effective font scale so all sp-based text resizes uniformly app-wide.
        CompositionLocalProvider(
            LocalDensity provides Density(
                density = density.density,
                fontScale = density.fontScale * fontScale
            )
        ) {
            content()
        }
    }
}