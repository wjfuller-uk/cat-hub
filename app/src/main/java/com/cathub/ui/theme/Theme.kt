package com.cathub.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF9800),       // Orange (Will's colour)
    secondary = Color(0xFF9E9E9E),     // Grey (Imogen's colour)
    tertiary = Color(0xFFFFFFFF),      // White (Lucy's colour)
    background = Color(0xFF1A237E),    // Dark blue (night)
    surface = Color(0xFF283593),       // Medium dark blue
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFFF9800),       // Orange (Will's colour)
    secondary = Color(0xFF9E9E9E),     // Grey (Imogen's colour)
    tertiary = Color(0xFFFFFFFF),      // White (Lucy's colour)
    background = Color(0xFF81D4FA),    // Light blue (morning)
    surface = Color(0xFFB3E5FC),       // Lighter blue
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

@Composable
fun CatHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled — we want our cat colours
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            // Dynamic color not used — we want consistent cat colours
            if (darkTheme) DarkColorScheme else LightColorScheme
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content,
    )
}
