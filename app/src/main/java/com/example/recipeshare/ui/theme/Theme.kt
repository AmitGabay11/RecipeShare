package com.example.recipeshare.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Material 2 Light Colors
private val LightColors = lightColors(
    primary = Color(0xFF6200EE), // Purple
    onPrimary = Color.White, // White text on primary
    secondary = Color(0xFF03DAC5), // Teal
    onSecondary = Color.Black, // Black text on secondary
    background = Color(0xFFF6F6F6), // Light Gray
    onBackground = Color.Black // Black text on background
)

// Material 2 Dark Colors (optional)
private val DarkColors = lightColors(
    primary = Color(0xFFBB86FC),
    onPrimary = Color.Black,
    secondary = Color(0xFF03DAC5),
    onSecondary = Color.Black,
    background = Color(0xFF121212),
    onBackground = Color.White
)

@Composable
fun RecipeShareTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) DarkColors else LightColors,
        typography = MaterialTheme.typography, // Use Material 2 Typography
        shapes = MaterialTheme.shapes,
        content = content
    )
}


