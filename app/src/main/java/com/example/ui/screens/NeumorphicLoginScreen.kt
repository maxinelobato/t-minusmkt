package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.ui.components.BiometricSensorWidget
import com.example.ui.components.GoogleSSOCard
import com.example.ui.components.NeumorphicBottomDock
import com.example.ui.components.NotificationBannerCard
import com.example.ui.components.PhosphorIcon
import com.example.ui.components.SwipeLockHandle
import com.example.ui.components.TMinusBrandLogo
import com.example.ui.components.neonGlow
import com.example.ui.components.neumorphicElevation
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonSkyBlue

/**
 * Neumorphic & Neon Lock / Ambient Display Screen strictly matching Reference Images 1, 2, and 3:
 * - Giant digital clock & localized date
 * - Active notification card banner ("T-MINUS MKT • T-29 agora" with unlock & view details)
 * - Google Workspace SSO card with Credential Manager integration & authenticated session pill
 * - Biometric sensor card (cyan laser hologram in dark mode, concave neumorphic well in light mode)
 * - Bottom 4-button tactile dock (PT/EN, Mic, Bell, Sun/Moon)
 * - Interactive swipe-up / gesture unlock handle
 */
@Composable
fun NeumorphicLoginScreen(
    isPt: Boolean,
    isDarkMode: Boolean,
    hasExistingSession: Boolean,
    isAuthenticated: Boolean = false,
    userEmail: String? = null,
    timeDisplay: String = "10:42",
    amPmDisplay: String = "AM",
    dateDisplay: String = "Thursday, Nov 05",
    onGoogleSignInClick: () -> Unit,
    onBiometricUnlockClick: () -> Unit,
    onUnlockClick: () -> Unit,
    onToggleLanguage: () -> Unit,
    onToggleTheme: () -> Unit,
    onVoiceClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onLockClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LocalNeumorphicColors.current
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(top = statusBarPadding, bottom = navBarPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Scrollable Content Area (To fit all screens cleanly)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .widthIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Official T-Minus Mkt Brand Logotype (Adapts to light / dark neon)
                TMinusBrandLogo(
                    height = 42.dp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // TOP GIANT CLOCK & DATE (Matching Images 1, 2, 3)
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = timeDisplay,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp,
                        letterSpacing = (-1.5).sp,
                        color = colors.textPrimary
                    )

                    if (amPmDisplay.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = amPmDisplay,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = dateDisplay,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.5.sp,
                    color = colors.textSecondary
                )

                Spacer(modifier = Modifier.height(18.dp))

                // NOTIFICATION BANNER / TOP HEADER CARD
                if (isDarkMode) {
                    // Reference Image 1 Notification Card
                    NotificationBannerCard(
                        isPt = isPt,
                        isDark = true,
                        onUnlockClick = onUnlockClick,
                        onViewDetailsClick = onUnlockClick,
                        onDismissClick = {}
                    )
                } else {
                    // Reference Images 2 and 3 Top Header Card: "T-MINUS MKT • LOCK_STATE: ATIVO"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .neumorphicElevation(
                                cornerRadius = 20.dp,
                                backgroundColor = colors.surface,
                                lightShadowColor = colors.lightShadow,
                                darkShadowColor = colors.darkShadow,
                                offset = 4.dp,
                                blur = 10.dp
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "T-MINUS MKT",
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp,
                                    color = colors.textSecondary
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(NeonGreen)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "LOCK_STATE: ATIVO",
                                        fontFamily = InterFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.5.sp,
                                        color = Color(0xFF047857)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (isPt) "PLANEJAMENTO DE MARKETING" else "MARKETING PLANNING",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                letterSpacing = 0.5.sp,
                                color = colors.textPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // GOOGLE WORKSPACE SSO CARD / AUTHENTICATED SESSION PILL
                GoogleSSOCard(
                    isPt = isPt,
                    isDark = isDarkMode,
                    isAuthenticated = isAuthenticated,
                    userEmail = userEmail,
                    onGoogleSignInClick = onGoogleSignInClick,
                    onLockClick = onLockClick
                )

                Spacer(modifier = Modifier.height(16.dp))

                // BIOMETRIC SENSOR CARD (Holographic Laser Scanner or Sunken Well)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .then(
                            if (isDarkMode) {
                                Modifier
                                    .background(colors.surface.copy(alpha = 0.6f))
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
                        .padding(vertical = 18.dp, horizontal = 12.dp)
                ) {
                    BiometricSensorWidget(
                        isPt = isPt,
                        isDark = isDarkMode,
                        onClick = onBiometricUnlockClick
                    )
                }

                // In Dark Mode: Preview Countdown Section under biometrics (as in Image 1)
                if (isDarkMode) {
                    Spacer(modifier = Modifier.height(18.dp))

                    // Mini countdown preview card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .neonGlow(
                                color = NeonCyan,
                                cornerRadius = 20.dp,
                                glowRadius = 12.dp,
                                borderWidth = 1.4.dp
                            )
                            .background(colors.surface)
                            .clickable { onUnlockClick() }
                            .padding(vertical = 10.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isPt) "CONTAGEM REGRESSIVA:" else "COUNTDOWN:",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.2.sp,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isPt) "PLANEJAMENTO DE MARKETING" else "MARKETING PLANNING",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                letterSpacing = 1.sp,
                                color = NeonCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preview item 1 (GEO Research)
                    PreviewTaskCard(
                        timeline = if (isPt) "Vinte e nove dias antes (09/11):" else "Twenty-nine days before (09/11):",
                        title = if (isPt) "Iniciar pesquisa de GEO" else "Start GEO research",
                        iconName = "search",
                        iconColor = NeonCyan,
                        onClick = onUnlockClick
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preview item 2 (Blog Plan)
                    PreviewTaskCard(
                        timeline = if (isPt) "Catorze dias antes (24/11):" else "Fourteen days before (24/11):",
                        title = if (isPt) "Estruturar plano do blog" else "Structure blog plan",
                        iconName = "article",
                        iconColor = NeonPurple,
                        onClick = onUnlockClick
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // BOTTOM CONTROLS: 4-button Dock & Swipe Handle
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NeumorphicBottomDock(
                    isPt = isPt,
                    isDark = isDarkMode,
                    onToggleLanguage = onToggleLanguage,
                    onVoiceClick = onVoiceClick,
                    onNotificationClick = onNotificationClick,
                    onToggleTheme = onToggleTheme
                )

                Spacer(modifier = Modifier.height(10.dp))

                SwipeLockHandle(
                    isLocked = true,
                    onUnlock = onUnlockClick,
                    onLock = {},
                    label = if (isPt) "DESLIZE" else "SWIPE UP"
                )
            }
        }
    }
}

@Composable
private fun PreviewTaskCard(
    timeline: String,
    title: String,
    iconName: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    val colors = LocalNeumorphicColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .border(1.dp, colors.lightShadow.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Concave circular well
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(0xFF13171F))
                .border(1.dp, Color(0xFF222B38), CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = timeline,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = colors.textSecondary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        PhosphorIcon(
            name = iconName,
            tint = iconColor,
            glowColor = iconColor,
            size = 20.dp
        )
    }
}
