package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.data.model.BubbleShapeType
import com.example.data.model.ThemePreferences

val LocalThemePreferences = compositionLocalOf { ThemePreferences() }

object OneUIThemeEngine {

    fun parseColor(hex: String, fallback: Color): Color {
        return try {
            val cleanHex = hex.removePrefix("#")
            val colorLong = cleanHex.toLong(16)
            if (cleanHex.length == 6) {
                Color(colorLong or 0x00000000FF000000)
            } else {
                Color(colorLong)
            }
        } catch (e: Exception) {
            fallback
        }
    }

    fun getColorScheme(prefs: ThemePreferences, isSystemDark: Boolean): ColorScheme {
        val primary = parseColor(prefs.customAccentHex, Color(0xFF06B6D4))

        // In Dark mode, pure black or OLED backgrounds, no grays or gradients!
        return if (prefs.isOledBlack || isSystemDark) {
            darkColorScheme(
                primary = primary,
                onPrimary = Color.Black,
                primaryContainer = primary.copy(alpha = 0.2f),
                onPrimaryContainer = primary,
                secondary = Color(0xFF38BDF8),
                onSecondary = Color.Black,
                background = Color(0xFF000000), // Pure OLED Black
                onBackground = Color(0xFFFFFFFF),
                surface = Color(0xFF000000),    // Pure OLED Black
                onSurface = Color(0xFFFFFFFF),
                surfaceVariant = Color(0xFF0D0D10), // Ultra dark contrast card
                onSurfaceVariant = Color(0xFFA1A1AA),
                outline = Color(0xFF27272A),
                outlineVariant = Color(0xFF18181B)
            )
        } else {
            // Light Scheme (clean minimal)
            lightColorScheme(
                primary = primary,
                onPrimary = Color.White,
                primaryContainer = primary.copy(alpha = 0.12f),
                onPrimaryContainer = primary,
                secondary = Color(0xFF0284C7),
                background = Color(0xFFFAFAFA),
                onBackground = Color(0xFF09090B),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF09090B),
                surfaceVariant = Color(0xFFF4F4F5),
                onSurfaceVariant = Color(0xFF71717A),
                outline = Color(0xFFE4E4E7),
                outlineVariant = Color(0xFFF4F4F5)
            )
        }
    }

    fun getBubbleShape(
        shapeType: BubbleShapeType,
        isFromUser: Boolean,
        radiusDp: Int = 20
    ): Shape {
        val r = radiusDp.dp
        val small = 4.dp
        return when (shapeType) {
            BubbleShapeType.ROUNDED -> {
                if (isFromUser) {
                    RoundedCornerShape(topStart = r, topEnd = r, bottomStart = r, bottomEnd = small)
                } else {
                    RoundedCornerShape(topStart = r, topEnd = r, bottomStart = small, bottomEnd = r)
                }
            }
            BubbleShapeType.RECTANGULAR -> {
                RoundedCornerShape(4.dp) // Sharp modern rectangle
            }
            BubbleShapeType.PILL -> {
                RoundedCornerShape(28.dp) // Smooth pill
            }
            BubbleShapeType.CHAT_TAIL -> {
                if (isFromUser) {
                    RoundedCornerShape(topStart = r, topEnd = 2.dp, bottomStart = r, bottomEnd = r)
                } else {
                    RoundedCornerShape(topStart = 2.dp, topEnd = r, bottomStart = r, bottomEnd = r)
                }
            }
        }
    }
}
