package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.NeonSkyBlue

@Composable
fun GoogleAccountConfirmDialog(
    isPt: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (email: String) -> Unit
) {
    val colors = LocalNeumorphicColors.current
    var emailInput by remember { mutableStateOf("marketing.director@gmail.com") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(colors.surface)
                .neumorphicElevation(
                    cornerRadius = 24.dp,
                    backgroundColor = colors.surface,
                    lightShadowColor = colors.lightShadow,
                    darkShadowColor = colors.darkShadow,
                    offset = 4.dp,
                    blur = 12.dp
                )
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Google Logo
                GoogleLogo(size = 32.dp)

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isPt) "CONECTAR CONTA GOOGLE" else "CONNECT GOOGLE ACCOUNT",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isPt) {
                        "Confirme seu endereço de email Google para habilitar o cronograma estratégico e a sincronização com o Google Calendar."
                    } else {
                        "Confirm your Google Account email to enable the marketing countdown and Google Calendar synchronization."
                    },
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = {
                        Text(
                            text = if (isPt) "Email da Conta Google" else "Google Account Email",
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("google_email_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedBorderColor = if (colors.isDark) NeonSkyBlue else colors.textPrimary,
                        unfocusedBorderColor = colors.textMuted
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onDismiss() }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = if (isPt) "Cancelar" else "Cancel",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = colors.textMuted
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .neumorphicElevation(
                                cornerRadius = 14.dp,
                                backgroundColor = colors.surface,
                                lightShadowColor = colors.lightShadow,
                                darkShadowColor = colors.darkShadow,
                                offset = 2.dp,
                                blur = 6.dp
                            )
                            .clickable(enabled = emailInput.contains("@")) {
                                onConfirm(emailInput.trim())
                            }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                            .testTag("confirm_google_account_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isPt) "Continuar" else "Continue",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (colors.isDark) NeonSkyBlue else colors.textPrimary
                        )
                    }
                }
            }
        }
    }
}
