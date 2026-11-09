package com.codestitcher.game2048.game

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object GameColors {
    // Pure OLED Black
    val Background = Color(0xFF000000)
    
    // AMOLED Optimized Board & Surfaces
    val BoardBackground = Color(0xFF0E1117)
    val BoardBorder = Color(0xFF1C222E)
    val EmptyCell = Color(0xFF161B26)
    
    val CardBackground = Color(0xFF12151D)
    val CardBorder = Color(0xFF222938)
    
    val TextWhite = Color(0xFFFFFFFF)
    val TextMuted = Color(0xFF8E9BAE)
    val TextSubtle = Color(0xFF556070)

    // Crimson Accent for Game Over
    val RedAccent = Color(0xFFFF3B30)
    val RedAccentDark = Color(0xFFDC2626)
    
    // Gold Accent for Win
    val GoldAccent = Color(0xFFFFD700)
    val GoldAccentDark = Color(0xFFF59E0B)

    // AMOLED Jewel-Tone Gradients for Tiles
    fun getTileBrush(value: Int): Brush {
        return when (value) {
            2 -> Brush.verticalGradient(listOf(Color(0xFF243044), Color(0xFF1E2838)))
            4 -> Brush.verticalGradient(listOf(Color(0xFFFF7A59), Color(0xFFEE5335)))
            8 -> Brush.verticalGradient(listOf(Color(0xFFFF9F1C), Color(0xFFF28500)))
            16 -> Brush.verticalGradient(listOf(Color(0xFFFF5722), Color(0xFFE64A19)))
            32 -> Brush.verticalGradient(listOf(Color(0xFFE63946), Color(0xFFD62828)))
            64 -> Brush.verticalGradient(listOf(Color(0xFFF72585), Color(0xFFD81159)))
            128 -> Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
            256 -> Brush.verticalGradient(listOf(Color(0xFF06B6D4), Color(0xFF0284C7)))
            512 -> Brush.verticalGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)))
            1024 -> Brush.verticalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)))
            2048 -> Brush.verticalGradient(listOf(Color(0xFFFFD700), Color(0xFFFF9E00)))
            4096 -> Brush.verticalGradient(listOf(Color(0xFFFF007F), Color(0xFF7928CA)))
            else -> Brush.verticalGradient(listOf(Color(0xFF00F5D4), Color(0xFF7B2CBF)))
        }
    }

    fun getTileTextColor(value: Int): Color {
        return when (value) {
            2048 -> Color(0xFF000000) // Crisp pitch black on glowing gold
            else -> Color(0xFFFFFFFF)
        }
    }

    fun getTileBorderColor(value: Int): Color {
        return when (value) {
            2 -> Color(0xFF33435C)
            4 -> Color(0xFFFF8E72)
            8 -> Color(0xFFFFB347)
            16 -> Color(0xFFFF7043)
            32 -> Color(0xFFFF5252)
            64 -> Color(0xFFFF4081)
            128 -> Color(0xFF34D399)
            256 -> Color(0xFF38BDF8)
            512 -> Color(0xFF60A5FA)
            1024 -> Color(0xFFA78BFA)
            2048 -> Color(0xFFFFE066)
            else -> Color(0xFFE0E7FF)
        }
    }
}
