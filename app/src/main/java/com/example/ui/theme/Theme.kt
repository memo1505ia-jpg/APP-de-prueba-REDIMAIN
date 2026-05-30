package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Forces a gorgeous, premium, highly tactical dark executive theme for the Naval Command deck
private val MilitaryTacticalColorScheme = darkColorScheme(
    primary = CommandAmberPrimary,
    secondary = CommandAmberSecondary,
    tertiary = HolographicCyan,
    background = SlateBg,
    surface = SlateSurface,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = TextSlate200,
    onSurface = TextSlate200,
    surfaceVariant = BorderSlate800,
    outline = BorderSlate700
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force gorgeous tactical navy scheme
    dynamicColor: Boolean = false, // Preserve brand tactical colors
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MilitaryTacticalColorScheme,
        typography = Typography,
        content = content
    )
}
