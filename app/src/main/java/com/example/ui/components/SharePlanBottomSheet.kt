package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandCyanLight
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple

/**
 * Material 3 Modal Bottom Sheet launched from the Bottom Navigation Bar's Share tab,
 * offering rendered Countdown Card Bitmap Image sharing or Formatted Text Summary sharing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharePlanBottomSheet(
    isPt: Boolean,
    onDismiss: () -> Unit,
    onShareImageCard: () -> Unit,
    onShareTextSummary: () -> Unit
) {
    val colors = LocalNeumorphicColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        contentColor = colors.textPrimary,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp)
                .testTag("share_plan_bottom_sheet")
        ) {
            Text(
                text = if (isPt) "Compartilhar Planejamento" else "Share Launch Plan",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isPt) "Escolha o formato para enviar sua contagem regressiva" else "Choose how to share your marketing countdown",
                fontFamily = InterFontFamily,
                fontSize = 13.sp,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            ShareOptionCard(
                icon = Icons.Outlined.Image,
                iconTint = if (colors.isDark) NeonCyan else BrandCyanLight,
                title = if (isPt) "Card Visual de Contagem (Imagem)" else "Countdown Visual Card (Image)",
                subtitle = if (isPt) "Exporta um card PNG estilizado com o prazo T-Minus" else "Exports a styled PNG card with your T-Minus badge",
                testTag = "share_option_image_card",
                onClick = {
                    onDismiss()
                    onShareImageCard()
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            ShareOptionCard(
                icon = Icons.Outlined.Description,
                iconTint = if (colors.isDark) NeonPurple else Color(0xFF7C3AED),
                title = if (isPt) "Resumo em Texto (Checklist)" else "Text Summary (Checklist)",
                subtitle = if (isPt) "Envia todas as etapas e status por mensagem ou e-mail" else "Sends all milestones and completion status via text",
                testTag = "share_option_text_summary",
                onClick = {
                    onDismiss()
                    onShareTextSummary()
                }
            )
        }
    }
}

@Composable
private fun ShareOptionCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    val colors = LocalNeumorphicColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neumorphicElevation(
                cornerRadius = 20.dp,
                backgroundColor = colors.surface,
                lightShadowColor = colors.lightShadow,
                darkShadowColor = colors.darkShadow,
                offset = 4.dp,
                blur = 9.dp
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .neumorphicInset(
                        cornerRadius = 14.dp,
                        backgroundColor = colors.surface,
                        darkShadowColor = colors.insetDark,
                        lightShadowColor = colors.insetLight,
                        offset = 2.dp,
                        blur = 4.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontFamily = InterFontFamily,
                    fontSize = 12.5.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}
