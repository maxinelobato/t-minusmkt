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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonSkyBlue

/**
 * Google Workspace SSO card and authenticated session pill matching Reference Images 1, 2, and 3:
 * - "GOOGLE WORKSPACE SSO" with "AES-256" badge
 * - "Continuar com o Google" neumorphic button
 * - Subtitle: "Autenticação via Google Credential Manager & Firebase"
 * - Or active session: "• SESSÃO AUTENTICADA (TEE)" with "Bloquear" action
 */
@Composable
fun GoogleSSOCard(
    isPt: Boolean,
    isDark: Boolean,
    isAuthenticated: Boolean,
    userEmail: String?,
    onGoogleSignInClick: () -> Unit,
    onLockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalNeumorphicColors.current

    if (isAuthenticated) {
        // Authenticated Session Pill matching Image 1: "• SESSÃO AUTENTICADA (TEE)" | "Bloquear"
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .then(
                    if (isDark) {
                        Modifier
                            .border(1.dp, NeonGreen.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                            .background(Color(0xFF0F1813))
                    } else {
                        Modifier
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                            .background(Color(0xFFECFDF5))
                    }
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .testTag("authenticated_session_pill")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(NeonGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPt) "SESSÃO AUTENTICADA (TEE)" else "AUTHENTICATED SESSION (TEE)",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp,
                        color = if (isDark) NeonGreen else Color(0xFF047857)
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onLockClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PhosphorIcon(
                        name = "lock",
                        tint = if (isDark) NeonSkyBlue else colors.textPrimary,
                        size = 12.dp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isPt) "Bloquear" else "Lock",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.5.sp,
                        color = if (isDark) NeonSkyBlue else colors.textPrimary
                    )
                }
            }
        }
    } else {
        // SSO Login Card matching Reference Images 2 and 3
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .then(
                    if (isDark) {
                        Modifier
                            .border(1.dp, colors.lightShadow.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                            .background(colors.surface)
                    } else {
                        Modifier.neumorphicElevation(
                            cornerRadius = 24.dp,
                            backgroundColor = colors.surface,
                            lightShadowColor = colors.lightShadow,
                            darkShadowColor = colors.darkShadow,
                            offset = 4.dp,
                            blur = 10.dp
                        )
                    }
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .testTag("google_sso_card")
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Top Badge Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PhosphorIcon(
                            name = "shield",
                            tint = if (isDark) NeonSkyBlue else Color(0xFF2563EB),
                            size = 13.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GOOGLE WORKSPACE SSO",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            letterSpacing = 0.8.sp,
                            color = if (isDark) NeonSkyBlue else Color(0xFF1E3A8A)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDark) Color(0xFF222B38) else Color(0xFFE2E8F0))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "AES-256",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = colors.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Continuar com o Google Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .then(
                            if (isDark) {
                                Modifier
                                    .background(Color(0xFF222834))
                                    .border(1.dp, Color(0xFF333E50), RoundedCornerShape(18.dp))
                            } else {
                                Modifier.neumorphicElevation(
                                    cornerRadius = 18.dp,
                                    backgroundColor = colors.surface,
                                    lightShadowColor = colors.lightShadow,
                                    darkShadowColor = colors.darkShadow,
                                    offset = 3.dp,
                                    blur = 6.dp
                                )
                            }
                        )
                        .clickable { onGoogleSignInClick() }
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                        .testTag("continue_with_google_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        GoogleLogo(size = 20.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isPt) "Continuar com o Google" else "Continue with Google",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = colors.textPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isPt) {
                        "Autenticação via Google Credential Manager & Firebase"
                    } else {
                        "Authentication via Google Credential Manager & Firebase"
                    },
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
