package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.example.data.model.ThemePreferences

@Composable
fun MyApplicationTheme(
    preferences: ThemePreferences = ThemePreferences(),
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val colorScheme = OneUIThemeEngine.getColorScheme(preferences, isSystemDark)

    CompositionLocalProvider(
        LocalThemePreferences provides preferences
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
