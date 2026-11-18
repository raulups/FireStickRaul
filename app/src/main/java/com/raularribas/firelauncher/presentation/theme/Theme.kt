package com.raularribas.firelauncher.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

val Background = Color(0xFF06070A)
val TileColor = Color(0xFF1B2030)
val Accent = Color(0xFFFFC857)
val TextPrimary = Color(0xFFF4F5F7)
val TextSecondary = Color(0xFFD7DBE3)
val DefaultHero = Color(0xFF1F3A70)
val TileShape = RoundedCornerShape(14.dp)

@Composable
fun FireLauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Accent,
            background = Background,
            surface = TileColor,
            onSurface = TextPrimary,
            onBackground = TextPrimary
        ),
        content = content
    )
}
