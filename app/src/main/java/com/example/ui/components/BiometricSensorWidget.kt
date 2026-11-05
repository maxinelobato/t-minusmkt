package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonSkyBlue

/**
 * Biometric Sensor component faithful to Reference Images 1, 2, and 3:
 * - In Dark Mode: Cyan holographic fingerprint with animated horizontal scanline beam.
 * - In Light Mode: Tactile sunken circular neumorphic well with teal fingerprint.
 */
@Composable
fun BiometricSensorWidget(
    isPt: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalNeumorphicColors.current

    val infiniteTransition = rememberInfiniteTransition(label = "scan_beam")
    val scanYProgress by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_progress"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("biometric_sensor_widget"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Biometric Sensor Button
        Box(
            modifier = Modifier
                .size(86.dp)
                .clip(CircleShape)
                .then(
                    if (isDark) {
                        Modifier
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        NeonCyan.copy(alpha = 0.25f * glowPulse),
                                        Color.Transparent
                                    )
                                )
                            )
                    } else {
                        Modifier.neumorphicInset(
                            cornerRadius = 43.dp,
                            backgroundColor = colors.surface,
                            darkShadowColor = colors.insetDark,
                            lightShadowColor = colors.insetLight,
                            offset = 3.dp,
                            blur = 6.dp
                        )
                    }
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            // Fingerprint icon
            PhosphorIcon(
                name = "fingerprint",
                tint = if (isDark) NeonCyan.copy(alpha = 0.95f) else Color(0xFF0284C7),
                glowColor = if (isDark) NeonCyan else null,
                size = 46.dp
            )

            // Laser scan beam in dark mode
            if (isDark) {
                Canvas(modifier = Modifier.size(70.dp)) {
                    val y = size.height * scanYProgress
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                NeonCyan.copy(alpha = 0.9f * glowPulse),
                                NeonCyan,
                                NeonCyan.copy(alpha = 0.9f * glowPulse),
                                Color.Transparent
                            )
                        ),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 2.5.dp.toPx()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Instructional Text
        Text(
            text = if (isPt) {
                "Toque no sensor biométrico ou use o reconhecimento facial"
            } else {
                "Touch the biometric sensor or use facial recognition"
            },
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.5.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Security Badges (Face ID & Keystore)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Face ID Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (isDark) {
                            Modifier
                                .border(1.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .background(NeonCyan.copy(alpha = 0.08f))
                        } else {
                            Modifier
                                .background(Color(0xFFE2E8F0))
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PhosphorIcon(
                        name = "faceid",
                        tint = if (isDark) NeonCyan else colors.textPrimary,
                        size = 13.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isDark) "FACE ID ATIVO" else "Face ID",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp,
                        color = if (isDark) NeonCyan else colors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // TEE / Keystore Pill
            Row(verticalAlignment = Alignment.CenterVertically) {
                PhosphorIcon(
                    name = "lock",
                    tint = colors.textMuted,
                    size = 11.dp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isDark) "TEE Keystore AES-256" else "Android Keystore",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.5.sp,
                    color = colors.textMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Hardware TEE subtext
        Text(
            text = if (isPt) {
                "Protegido por EncryptedSharedPreferences (Hardware TEE)"
            } else {
                "Protected by EncryptedSharedPreferences (Hardware TEE)"
            },
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 9.5.sp,
            color = colors.textMuted.copy(alpha = 0.75f)
        )
    }
}
