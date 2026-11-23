package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonSkyBlue
import com.example.ui.theme.NeonYellow

/**
 * Bottom 4-button Neumorphic Dock matching all 5 reference images:
 * 1. Language pill (PT/EN with cyan glow ring in dark mode)
 * 2. Voice input button (Mic)
 * 3. Notification alerts button (Bell with cyan dot badge)
 * 4. Theme toggle button (Sun with golden ring in dark mode, Moon in light mode)
 */
@Composable
fun NeumorphicBottomDock(
    isPt: Boolean,
    isDark: Boolean,
    onToggleLanguage: () -> Unit,
    onVoiceClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalNeumorphicColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Language Button (PT / EN)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .neumorphicElevation(
                    cornerRadius = 24.dp,
                    backgroundColor = colors.surface,
                    lightShadowColor = colors.lightShadow,
                    darkShadowColor = colors.darkShadow,
                    offset = 3.dp,
                    blur = 6.dp
                )
                .fluidNeumorphicClick { onToggleLanguage() }
                .testTag("dock_language_toggle"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isPt) "PT" else "EN",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isDark) NeonCyan else colors.textPrimary
            )
        }

        // 2. Microphone / Voice Button
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .neumorphicElevation(
                    cornerRadius = 24.dp,
                    backgroundColor = colors.surface,
                    lightShadowColor = colors.lightShadow,
                    darkShadowColor = colors.darkShadow,
                    offset = 3.dp,
                    blur = 6.dp
                )
                .fluidNeumorphicClick { onVoiceClick() }
                .testTag("dock_voice_button"),
            contentAlignment = Alignment.Center
        ) {
            PhosphorIcon(
                name = "mic",
                tint = if (isDark) NeonCyan else colors.textPrimary,
                size = 20.dp
            )
        }

        // 3. Notification Bell with Badge Dot
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .neumorphicElevation(
                    cornerRadius = 24.dp,
                    backgroundColor = colors.surface,
                    lightShadowColor = colors.lightShadow,
                    darkShadowColor = colors.darkShadow,
                    offset = 3.dp,
                    blur = 6.dp
                )
                .fluidNeumorphicClick { onNotificationClick() }
                .testTag("dock_notification_button"),
            contentAlignment = Alignment.Center
        ) {
            PhosphorIcon(
                name = "bell",
                tint = if (isDark) NeonPurple else colors.textPrimary,
                size = 20.dp
            )

            // Vibrant notification badge dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-11).dp, y = 11.dp)
                    .clip(CircleShape)
                    .background(NeonSkyBlue)
            )
        }

        // 4. Theme Toggle (Sun in dark, Moon in light)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .neumorphicElevation(
                    cornerRadius = 24.dp,
                    backgroundColor = colors.surface,
                    lightShadowColor = colors.lightShadow,
                    darkShadowColor = colors.darkShadow,
                    offset = 3.dp,
                    blur = 6.dp
                )
                .fluidNeumorphicClick { onToggleTheme() }
                .testTag("dock_theme_toggle"),
            contentAlignment = Alignment.Center
        ) {
            PhosphorIcon(
                name = if (isDark) "sun" else "moon",
                tint = if (isDark) NeonYellow else colors.textPrimary,
                glowColor = null,
                size = 22.dp
            )
        }
    }
}
