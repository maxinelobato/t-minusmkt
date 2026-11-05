package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class NeumorphicColors(
    val background: Color,
    val surface: Color,
    val lightShadow: Color,
    val darkShadow: Color,
    val insetDark: Color,
    val insetLight: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val isDark: Boolean
)

val LocalNeumorphicColors = staticCompositionLocalOf {
    NeumorphicColors(
        background = SoftLightBackground,
        surface = SoftLightSurface,
        lightShadow = SoftLightHighlight,
        darkShadow = SoftLightShadow,
        insetDark = SoftLightInsetDark,
        insetLight = SoftLightInsetLight,
        textPrimary = SoftLightTextPrimary,
        textSecondary = SoftLightTextSecondary,
        textMuted = SoftLightTextMuted,
        isDark = false
    )
}

private val LightColorScheme = lightColorScheme(
    primary = SoftLightTextPrimary,
    onPrimary = SoftLightSurface,
    background = SoftLightBackground,
    onBackground = SoftLightTextPrimary,
    surface = SoftLightSurface,
    onSurface = SoftLightTextPrimary,
    surfaceVariant = SoftLightSurface,
    onSurfaceVariant = SoftLightTextSecondary
)

private val DarkColorScheme = darkColorScheme(
    primary = CyberDarkTextPrimary,
    onPrimary = CyberDarkSurface,
    background = CyberDarkBackground,
    onBackground = CyberDarkTextPrimary,
    surface = CyberDarkSurface,
    onSurface = CyberDarkTextPrimary,
    surfaceVariant = CyberDarkSurface,
    onSurfaceVariant = CyberDarkTextSecondary
)

@Composable
fun TMinusMktTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val neumorphicColors = if (darkTheme) {
        NeumorphicColors(
            background = CyberDarkBackground,
            surface = CyberDarkSurface,
            lightShadow = CyberDarkHighlight,
            darkShadow = CyberDarkShadow,
            insetDark = CyberDarkInsetDark,
            insetLight = CyberDarkInsetLight,
            textPrimary = CyberDarkTextPrimary,
            textSecondary = CyberDarkTextSecondary,
            textMuted = CyberDarkTextMuted,
            isDark = true
        )
    } else {
        NeumorphicColors(
            background = SoftLightBackground,
            surface = SoftLightSurface,
            lightShadow = SoftLightHighlight,
            darkShadow = SoftLightShadow,
            insetDark = SoftLightInsetDark,
            insetLight = SoftLightInsetLight,
            textPrimary = SoftLightTextPrimary,
            textSecondary = SoftLightTextSecondary,
            textMuted = SoftLightTextMuted,
            isDark = false
        )
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalNeumorphicColors provides neumorphicColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
