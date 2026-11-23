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

val M3LightColorScheme = lightColorScheme(
    primary = M3LightPrimary,
    onPrimary = M3LightOnPrimary,
    primaryContainer = M3LightPrimaryContainer,
    onPrimaryContainer = M3LightOnPrimaryContainer,
    secondary = M3LightSecondary,
    onSecondary = M3LightOnSecondary,
    secondaryContainer = M3LightSecondaryContainer,
    onSecondaryContainer = M3LightOnSecondaryContainer,
    tertiary = M3LightTertiary,
    onTertiary = M3LightOnTertiary,
    tertiaryContainer = M3LightTertiaryContainer,
    onTertiaryContainer = M3LightOnTertiaryContainer,
    background = M3LightBackground,
    onBackground = M3LightOnBackground,
    surface = M3LightSurface,
    onSurface = M3LightOnSurface,
    surfaceVariant = M3LightSurfaceVariant,
    onSurfaceVariant = M3LightOnSurfaceVariant,
    surfaceContainer = M3LightSurfaceContainer,
    surfaceContainerHigh = M3LightSurfaceContainerHigh,
    surfaceContainerHighest = M3LightSurfaceContainerHighest,
    surfaceContainerLow = M3LightSurfaceContainerLow,
    surfaceContainerLowest = M3LightSurfaceContainerLowest,
    outline = M3LightOutline,
    outlineVariant = M3LightOutlineVariant
)

val M3DarkColorScheme = darkColorScheme(
    primary = M3DarkPrimary,
    onPrimary = M3DarkOnPrimary,
    primaryContainer = M3DarkPrimaryContainer,
    onPrimaryContainer = M3DarkOnPrimaryContainer,
    secondary = M3DarkSecondary,
    onSecondary = M3DarkOnSecondary,
    secondaryContainer = M3DarkSecondaryContainer,
    onSecondaryContainer = M3DarkOnSecondaryContainer,
    tertiary = M3DarkTertiary,
    onTertiary = M3DarkOnTertiary,
    tertiaryContainer = M3DarkTertiaryContainer,
    onTertiaryContainer = M3DarkOnTertiaryContainer,
    background = M3DarkBackground,
    onBackground = M3DarkOnBackground,
    surface = M3DarkSurface,
    onSurface = M3DarkOnSurface,
    surfaceVariant = M3DarkSurfaceVariant,
    onSurfaceVariant = M3DarkOnSurfaceVariant,
    surfaceContainer = M3DarkSurfaceContainer,
    surfaceContainerHigh = M3DarkSurfaceContainerHigh,
    surfaceContainerHighest = M3DarkSurfaceContainerHighest,
    surfaceContainerLow = M3DarkSurfaceContainerLow,
    surfaceContainerLowest = M3DarkSurfaceContainerLowest,
    outline = M3DarkOutline,
    outlineVariant = M3DarkOutlineVariant
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

    val colorScheme = if (darkTheme) M3DarkColorScheme else M3LightColorScheme

    CompositionLocalProvider(LocalNeumorphicColors provides neumorphicColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
