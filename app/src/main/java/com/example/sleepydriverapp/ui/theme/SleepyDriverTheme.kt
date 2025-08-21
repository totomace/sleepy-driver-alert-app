package com.example.sleepydriverapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun SleepyDriverTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFFFF6B35),
            secondary = Color(0xFFFF8E53),
            background = Color(0xFFF5F5F5),
            surface = Color.White,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF2D2D2D),
            onSurface = Color(0xFF2D2D2D)
        ),
        content = content
    )
}