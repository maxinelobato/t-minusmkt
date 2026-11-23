package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CountdownTimerEntity
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LocalNeumorphicColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Prominent Neumorphic Countdown Timer Hero Card.
 *
 * Renders an extruded outer soft-UI card containing three recessed (`neumorphicInset`)
 * chambers for Days, Hours, and Minutes until the active target launch date.
 * Tapping anywhere on the card triggers haptic feedback and opens the Campaign Timer Manager.
 */
@Composable
fun NeumorphicCountdownHeroCard(
    activeTimer: CountdownTimerEntity?,
    countdownDays: Long,
    countdownHours: Long,
    countdownMinutes: Long,
    timerCount: Int,
    isPt: Boolean,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val neuColors = LocalNeumorphicColors.current
    val primaryColor = MaterialTheme.colorScheme.primary

    val accentColor = remember(activeTimer?.accentColorHex, primaryColor, neuColors.isDark) {
        parseCountdownAccentColor(activeTimer?.accentColorHex, primaryColor, neuColors.isDark)
    }

    val activeTitle = activeTimer?.title
        ?: if (isPt) "PLANEJAMENTO DE MARKETING" else "MARKETING PLANNING"
    val activeTag = activeTimer?.categoryTag ?: "Product"

    val targetTimestamp = activeTimer?.targetTimestamp ?: remember {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.DECEMBER)
            set(Calendar.DAY_OF_MONTH, 8)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis
    }

    val formattedLaunchDate = remember(targetTimestamp, isPt) {
        formatCountdownTargetDate(targetTimestamp, isPt)
    }

    val isLaunchReached = countdownDays <= 0L && countdownHours <= 0L && countdownMinutes <= 0L

    // Subtle live heartbeat animation for the active status indicator
    val infiniteTransition = rememberInfiniteTransition(label = "countdown_heartbeat")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_dot_alpha"
    )

    val cardAccessibilityLabel = if (isPt) {
        "Contagem regressiva para $activeTitle: $countdownDays dias, $countdownHours horas, $countdownMinutes minutos até $formattedLaunchDate. Toque para gerenciar campanhas."
    } else {
        "Countdown for $activeTitle: $countdownDays days, $countdownHours hours, $countdownMinutes minutes until $formattedLaunchDate. Tap to manage campaigns."
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .fluidNeumorphicClick(onClick = onCardClick)
            .neumorphicElevation(
                cornerRadius = 24.dp,
                backgroundColor = neuColors.surface,
                lightShadowColor = neuColors.lightShadow,
                darkShadowColor = neuColors.darkShadow,
                offset = 6.dp,
                blur = 14.dp
            )
            .clip(RoundedCornerShape(24.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp)
            .semantics {
                role = Role.Button
                contentDescription = cardAccessibilityLabel
            }
            .testTag("active_campaign_countdown_banner")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Meta Row: Live Status + Category Tag + Target Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .alpha(if (isLaunchReached) 1f else pulseAlpha)
                            .clip(CircleShape)
                            .background(if (isLaunchReached) Color(0xFF10B981) else accentColor)
                    )

                    Text(
                        text = if (isPt) "CONTAGEM" else "COUNTDOWN",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.5.sp,
                        letterSpacing = 1.1.sp,
                        color = neuColors.textSecondary
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor.copy(alpha = 0.16f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = activeTag.uppercase(),
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp,
                            letterSpacing = 0.5.sp,
                            color = accentColor
                        )
                    }
                }

                // Target Launch Date Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (neuColors.isDark) Color.White.copy(alpha = 0.06f)
                            else Color.Black.copy(alpha = 0.05f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("countdown_target_date_badge")
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = neuColors.textSecondary,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = formattedLaunchDate,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.5.sp,
                        color = neuColors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Active Launch Title
            Text(
                text = activeTitle.uppercase(),
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.5.sp,
                letterSpacing = 0.8.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = neuColors.textPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Three Recessed Neumorphic Chambers: DAYS | HOURS | MINUTES
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeumorphicTimeUnitWell(
                    value = countdownDays,
                    label = if (isPt) "DIAS" else "DAYS",
                    accentColor = accentColor,
                    isHighlighted = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("countdown_days_box")
                )

                NeumorphicTimeUnitWell(
                    value = countdownHours,
                    label = if (isPt) "HORAS" else "HOURS",
                    accentColor = neuColors.textPrimary,
                    isHighlighted = false,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("countdown_hours_box")
                )

                NeumorphicTimeUnitWell(
                    value = countdownMinutes,
                    label = "MIN",
                    accentColor = accentColor,
                    isHighlighted = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("countdown_minutes_box")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer Action / Status Cue
            if (isLaunchReached) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.16f))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isPt) "DIA DO LANÇAMENTO!" else "LAUNCH DAY REACHED!",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        letterSpacing = 0.6.sp,
                        color = Color(0xFF10B981)
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isPt) {
                            "Toque para alternar campanhas ($timerCount)"
                        } else {
                            "Tap to manage launch timers ($timerCount)"
                        },
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = neuColors.textSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

/**
 * Individual recessed (`neumorphicInset`) chamber for displaying a single
 * time unit (Days, Hours, or Minutes) with smooth animated digit transitions.
 */
@Composable
private fun NeumorphicTimeUnitWell(
    value: Long,
    label: String,
    accentColor: Color,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier
) {
    val neuColors = LocalNeumorphicColors.current
    val formattedValue = value.coerceAtLeast(0L).toString().padStart(2, '0')

    Box(
        modifier = modifier
            .neumorphicInset(
                cornerRadius = 16.dp,
                backgroundColor = neuColors.surface,
                darkShadowColor = neuColors.insetDark,
                lightShadowColor = neuColors.insetLight,
                offset = 3.dp,
                blur = 6.dp
            )
            .padding(vertical = 14.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = formattedValue,
                transitionSpec = {
                    (slideInVertically { -it / 3 } + fadeIn(tween(220)))
                        .togetherWith(slideOutVertically { it / 3 } + fadeOut(tween(220)))
                },
                label = "time_unit_digit_flip"
            ) { targetDigits ->
                Text(
                    text = targetDigits,
                    style = TextStyle(
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp,
                        letterSpacing = (-0.5).sp,
                        fontFeatureSettings = "tnum",
                        color = if (isHighlighted) accentColor else neuColors.textPrimary
                    ),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = label,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 1.2.sp,
                color = neuColors.textMuted
            )
        }
    }
}

internal fun formatCountdownTargetDate(timestampMillis: Long, isPt: Boolean): String {
    return try {
        val locale = if (isPt) Locale("pt", "BR") else Locale.US
        val pattern = if (isPt) "dd MMM yyyy" else "MMM dd, yyyy"
        SimpleDateFormat(pattern, locale).format(Date(timestampMillis))
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    } catch (_: Exception) {
        "2026-12-08"
    }
}

private fun parseCountdownAccentColor(
    hex: String?,
    fallback: Color,
    isDark: Boolean
): Color {
    if (hex.isNullOrBlank()) return fallback
    return try {
        val parsed = Color(android.graphics.Color.parseColor(hex))
        // Ensure comfortable legibility in Light Mode if a bright neon cyan is stored
        if (!isDark && hex.equals("#00FFFF", ignoreCase = true)) {
            Color(0xFF00639B)
        } else {
            parsed
        }
    } catch (_: Exception) {
        fallback
    }
}
