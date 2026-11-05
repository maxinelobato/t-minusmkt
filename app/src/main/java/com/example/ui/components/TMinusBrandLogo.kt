package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.NeonCyan

/**
 * Official T-Minus Mkt Brand Logo component.
 * Automatically adapts between Light Mode and Dark Mode Neon variants
 * as confirmed by the user.
 */
@Composable
fun TMinusBrandLogo(
    modifier: Modifier = Modifier,
    height: Dp = 44.dp,
    showContainer: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalNeumorphicColors.current
    val isDark = colors.isDark

    val logoRes = if (isDark) R.drawable.logo_dark else R.drawable.logo_light

    val baseModifier = modifier
        .height(height)
        .then(
            if (showContainer) {
                if (isDark) {
                    Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.surface)
                        .border(1.2.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                } else {
                    Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .neumorphicElevation(
                            cornerRadius = 22.dp,
                            backgroundColor = colors.surface,
                            lightShadowColor = colors.lightShadow,
                            darkShadowColor = colors.darkShadow,
                            offset = 3.dp,
                            blur = 6.dp
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                }
            } else {
                Modifier.clip(RoundedCornerShape(20.dp))
            }
        )
        .then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        )
        .testTag("t_minus_brand_logo")

    Box(
        modifier = baseModifier,
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = logoRes),
            contentDescription = "T-Minus Mkt Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.height(height - 4.dp)
        )
    }
}
