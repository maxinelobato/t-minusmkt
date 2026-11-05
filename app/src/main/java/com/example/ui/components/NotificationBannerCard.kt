package com.example.ui.components

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
import com.example.ui.theme.NeonSkyBlue

/**
 * Top active notification card as shown in Reference Image 1:
 * "T-MINUS MKT • T-29 agora"
 * "Start GEO research"
 * "Definir estratégia global para lançamento em 09/11"
 * "🔒 Toque para desbloquear" | "Ver detalhes →"
 */
@Composable
fun NotificationBannerCard(
    isPt: Boolean,
    isDark: Boolean,
    onUnlockClick: () -> Unit,
    onViewDetailsClick: () -> Unit,
    onDismissClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LocalNeumorphicColors.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .then(
                if (isDark) {
                    Modifier
                        .border(1.2.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                        .background(colors.surface)
                } else {
                    Modifier.neumorphicElevation(
                        cornerRadius = 22.dp,
                        backgroundColor = colors.surface,
                        lightShadowColor = colors.lightShadow,
                        darkShadowColor = colors.darkShadow,
                        offset = 4.dp,
                        blur = 10.dp
                    )
                }
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("notification_banner_card")
    ) {
        Column {
            // Header Row: T-MINUS MKT | • T-29 | agora
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDark) Color(0xFF13171F) else Color(0xFFE2E8F0))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "T-MINUS MKT",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.5.sp,
                            letterSpacing = 0.8.sp,
                            color = if (isDark) Color.White else colors.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Cyan Dot + T-29
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(NeonCyan)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "T-29",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = NeonCyan
                    )
                }

                // Time label
                Text(
                    text = if (isPt) "agora" else "now",
                    fontFamily = InterFontFamily,
                    fontSize = 11.sp,
                    color = colors.textMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isPt) "Iniciar pesquisa de GEO" else "Start GEO research",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = colors.textPrimary
                )

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable { onDismissClick() },
                    contentAlignment = Alignment.Center
                ) {
                    PhosphorIcon(
                        name = "close",
                        tint = colors.textMuted,
                        size = 14.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Subtitle Description
            Text(
                text = if (isPt) {
                    "Definir estratégia global para lançamento em 09/11"
                } else {
                    "Define global strategy for launch on 09/11"
                },
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row: "Toque para desbloquear" | "Ver detalhes →"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable { onUnlockClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PhosphorIcon(
                        name = "lock",
                        tint = NeonCyan,
                        size = 13.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPt) "Toque para desbloquear" else "Tap to unlock",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = NeonCyan
                    )
                }

                Row(
                    modifier = Modifier.clickable { onViewDetailsClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isPt) "Ver detalhes" else "View details",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    PhosphorIcon(
                        name = "arrowright",
                        tint = NeonCyan,
                        size = 12.dp
                    )
                }
            }
        }
    }
}
