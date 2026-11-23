package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// Tailwind Zinc Soft Neumorphism Palette (#E4E4E7 Light / #27272A Dark)
// blended with Material Design 3 semantic tokens & Neon Dark Mode Accents
// =========================================================================

// Core Zinc Neutrals
val Zinc50 = Color(0xFFFAFAFA)
val Zinc100 = Color(0xFFF4F4F5)
val Zinc200 = Color(0xFFE4E4E7) // Light Mode Primary Background
val Zinc300 = Color(0xFFD4D4D8)
val Zinc400 = Color(0xFFA1A1AA)
val Zinc500 = Color(0xFF71717A)
val Zinc600 = Color(0xFF52525B)
val Zinc700 = Color(0xFF3F3F46)
val Zinc800 = Color(0xFF27272A) // Dark Mode Primary Background
val Zinc900 = Color(0xFF18181B)

// Material 3 Light Scheme mapped to Tailwind Zinc (#E4E4E7)
val M3LightPrimary = Color(0xFF06B6D4) // Brand Cyan
val M3LightOnPrimary = Color(0xFFFFFFFF)
val M3LightPrimaryContainer = Color(0xFFD8D8DF) // Soft Zinc pill indicator
val M3LightOnPrimaryContainer = Color(0xFF18181B)

val M3LightSecondary = Color(0xFF0284C7)
val M3LightOnSecondary = Color(0xFFFFFFFF)
val M3LightSecondaryContainer = Color(0xFFD8D8DF) // Active NavBar pill in Light
val M3LightOnSecondaryContainer = Color(0xFF18181B)

val M3LightTertiary = Color(0xFF7C3AED)
val M3LightOnTertiary = Color(0xFFFFFFFF)
val M3LightTertiaryContainer = Color(0xFFEDE9FE)
val M3LightOnTertiaryContainer = Color(0xFF2E1065)

val M3LightBackground = Zinc200 // #E4E4E7
val M3LightOnBackground = Zinc900
val M3LightSurface = Zinc200 // #E4E4E7
val M3LightOnSurface = Zinc900
val M3LightSurfaceVariant = Color(0xFFDDDDE2)
val M3LightOnSurfaceVariant = Zinc600
val M3LightSurfaceContainerLowest = Color(0xFFF4F4F6)
val M3LightSurfaceContainerLow = Color(0xFFECECEF)
val M3LightSurfaceContainer = Color(0xFFEBEBEE) // Raised Neumorphic Card Surface
val M3LightSurfaceContainerHigh = Color(0xFFF0F0F3)
val M3LightSurfaceContainerHighest = Color(0xFFDADAE0)
val M3LightOutline = Zinc500
val M3LightOutlineVariant = Zinc300

// Material 3 Dark Scheme mapped to Tailwind Zinc (#27272A)
val M3DarkPrimary = Color(0xFF22D3EE) // Glowing Neon Cyan
val M3DarkOnPrimary = Color(0xFF082F49)
val M3DarkPrimaryContainer = Color(0xFF3A3A40)
val M3DarkOnPrimaryContainer = Color(0xFFFAFAFA)

val M3DarkSecondary = Color(0xFF4ADE80) // Neon Green
val M3DarkOnSecondary = Color(0xFF052E16)
val M3DarkSecondaryContainer = Color(0xFF3B3B42) // Active NavBar pill in Dark
val M3DarkOnSecondaryContainer = Color(0xFFFAFAFA)

val M3DarkTertiary = Color(0xFFC084FC) // Neon Purple
val M3DarkOnTertiary = Color(0xFF3B0764)
val M3DarkTertiaryContainer = Color(0xFF4C1D95)
val M3DarkOnTertiaryContainer = Color(0xFFF3E8FF)

val M3DarkBackground = Zinc800 // #27272A
val M3DarkOnBackground = Zinc50
val M3DarkSurface = Zinc800 // #27272A
val M3DarkOnSurface = Zinc50
val M3DarkSurfaceVariant = Color(0xFF323236)
val M3DarkOnSurfaceVariant = Zinc400
val M3DarkSurfaceContainerLowest = Color(0xFF1E1E21)
val M3DarkSurfaceContainerLow = Color(0xFF232326)
val M3DarkSurfaceContainer = Color(0xFF2C2C30) // Raised Neumorphic Card Surface
val M3DarkSurfaceContainerHigh = Color(0xFF323237)
val M3DarkSurfaceContainerHighest = Color(0xFF3B3B42)
val M3DarkOutline = Zinc500
val M3DarkOutlineVariant = Zinc700

// Soft Neumorphism Dual Shadow Tokens — Light Mode (#E4E4E7)
val SoftLightBackground = Zinc200
val SoftLightSurface = Color(0xFFEBEBEF)
val SoftLightHighlight = Color(0xFFFFFFFF)
val SoftLightShadow = Color(0xFFC2C2CB)
val SoftLightInsetDark = Color(0xFFC4C4CD)
val SoftLightInsetLight = Color(0xFFFFFFFF)
val SoftLightTextPrimary = Zinc900
val SoftLightTextSecondary = Zinc600
val SoftLightTextMuted = Zinc500

// Soft Neumorphism Dual Shadow Tokens — Dark Mode (#27272A)
val CyberDarkBackground = Zinc800
val CyberDarkSurface = Color(0xFF2C2C30)
val CyberDarkHighlight = Color(0xFF38383E)
val CyberDarkShadow = Color(0xFF151518)
val CyberDarkInsetDark = Color(0xFF18181B)
val CyberDarkInsetLight = Color(0xFF38383E)
val CyberDarkTextPrimary = Zinc50
val CyberDarkTextSecondary = Zinc400
val CyberDarkTextMuted = Zinc500

// Vibrant Neon Accents (Dark Mode Icon Glows & Countdown Badge)
val NeonCyan = Color(0xFF22D3EE)
val NeonBadgeCyan = Color(0xFF00E5FF)
val NeonSkyBlue = Color(0xFF38BDF8)
val NeonPurple = Color(0xFFC084FC)
val NeonYellow = Color(0xFFFACC15)
val NeonAmber = Color(0xFFFBBF24)
val NeonPink = Color(0xFFF472B6)
val NeonMagenta = Color(0xFFD946EF)
val NeonGreen = Color(0xFF4ADE80)
val BrandGreenDark = Color(0xFF22C55E)
val BrandCyanLight = Color(0xFF06B6D4)

// Lock Screen Light Mode Icon Stroke Colors (subtle colored icons matching reference)
val LightLockPurple = Color(0xFF9333EA)
val LightLockAmber = Color(0xFFF59E0B)
val LightLockGreen = Color(0xFF22C55E)
val LightLockCyan = Color(0xFF06B6D4)
val LightLockPink = Color(0xFFEC4899)
