package com.brainzomatch.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ==============================================================================
// Brainzo Match — Official Design System & Theme Tokens
// Package: com.brainzomatch.app.ui.theme
// ==============================================================================

// Primary & Secondary Brand Colors
val BrainzoPurple = Color(0xFF7C3AED)
val BrainzoPurpleDark = Color(0xFF5B21B6)
val BrainzoPurpleLight = Color(0xFFA78BFA)
val BrainzoSky = Color(0xFF0EA5E9)
val BrainzoSkyLight = Color(0xFFE0F2FE)
val BrainzoGold = Color(0xFFF59E0B)
val BrainzoGoldLight = Color(0xFFFEF3C7)
val BrainzoOrange = Color(0xFFF97316)
val BrainzoOrangeLight = Color(0xFFFFF7ED)
val BrainzoMint = Color(0xFF10B981)
val BrainzoMintLight = Color(0xFFD1FAE5)

// Surface & Background Neutral Tones
val BrainzoBgLight = Color(0xFFFCF8FF)
val BrainzoSurface = Color(0xFFFFFFFF)
val BrainzoSurfaceLow = Color(0xFFF6F2FF)
val BrainzoSurfaceDim = Color(0xFFDAD6FF)

// Text & Content Tones
val BrainzoTextDark = Color(0xFF1E1B4B)
val BrainzoTextMuted = Color(0xFF6B7280)

val BrainzoShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

val BrainzoTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Black,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        color = BrainzoTextDark
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Black,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        color = BrainzoTextDark
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = BrainzoTextDark
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        color = BrainzoTextDark
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = BrainzoTextMuted
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = Color.White
    )
)

private val LightColorScheme = lightColorScheme(
    primary = BrainzoPurple,
    onPrimary = Color.White,
    primaryContainer = BrainzoSurfaceLow,
    onPrimaryContainer = BrainzoPurpleDark,
    secondary = BrainzoSky,
    onSecondary = Color.White,
    tertiary = BrainzoGold,
    background = BrainzoBgLight,
    onBackground = BrainzoTextDark,
    surface = BrainzoSurface,
    onSurface = BrainzoTextDark,
    surfaceVariant = BrainzoSurfaceLow,
    onSurfaceVariant = BrainzoTextMuted
)

@Composable
fun BrainzoMatchTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = BrainzoTypography,
        shapes = BrainzoShapes,
        content = content
    )
}
