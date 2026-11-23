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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CountdownTimerEntity
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.NeonCyan
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AddCountdownTimerDialog(
    isPt: Boolean,
    timerToEdit: CountdownTimerEntity? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, targetTimestamp: Long, categoryTag: String, notes: String, accentColorHex: String) -> Unit
) {
    var title by remember { mutableStateOf(timerToEdit?.title ?: "") }
    var notes by remember { mutableStateOf(timerToEdit?.notes ?: "") }
    var selectedCategory by remember { mutableStateOf(timerToEdit?.categoryTag ?: "Product") }
    var selectedColorHex by remember { mutableStateOf(timerToEdit?.accentColorHex ?: "#6750A4") }

    // Days from now offset calculator
    val initialDaysRemaining = remember(timerToEdit) {
        if (timerToEdit != null) {
            val diff = timerToEdit.targetTimestamp - System.currentTimeMillis()
            (diff / (1000 * 60 * 60 * 24)).coerceAtLeast(1).toInt()
        } else {
            30
        }
    }
    var daysAhead by remember { mutableIntStateOf(initialDaysRemaining) }

    val targetCalendar = remember(daysAhead) {
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, daysAhead)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
    }

    val formattedTargetDate = remember(targetCalendar, isPt) {
        val pattern = if (isPt) "dd 'de' MMMM, yyyy • HH:mm" else "MMMM dd, yyyy • hh:mm a"
        val locale = if (isPt) Locale("pt", "BR") else Locale.US
        SimpleDateFormat(pattern, locale).format(targetCalendar.time)
    }

    val categories = listOf("Product", "Growth", "Social", "Brand", "PR", "Ads")
    val colorPalettes = listOf("#6750A4", "#00639B", "#059669", "#D97706", "#7C3AED")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_countdown_timer_dialog"),
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dialog Header
                Text(
                    text = if (timerToEdit == null) {
                        if (isPt) "Novo Cronômetro de Lançamento" else "New Launch Countdown Timer"
                    } else {
                        if (isPt) "Editar Cronômetro de Lançamento" else "Edit Launch Countdown Timer"
                    },
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Campaign Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isPt) "Título da Campanha *" else "Campaign Title *") },
                    placeholder = { Text(if (isPt) "ex: Lançamento Q4" else "e.g. Q4 Major Release") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("timer_title_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Days Ahead / Target Deadline Selector
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(14.dp)
                ) {
                    Text(
                        text = if (isPt) "Prazo de Lançamento (T-Minus)" else "Launch Deadline (T-Minus)",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "T-$daysAhead ${if (isPt) "dias" else "days"}",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // -5 days
                            StepButton(text = "-5") {
                                daysAhead = (daysAhead - 5).coerceAtLeast(1)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            // -1 day
                            StepButton(text = "-1") {
                                daysAhead = (daysAhead - 1).coerceAtLeast(1)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            // +1 day
                            StepButton(text = "+1") {
                                daysAhead = (daysAhead + 1).coerceAtMost(365)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            // +5 days
                            StepButton(text = "+5") {
                                daysAhead = (daysAhead + 5).coerceAtMost(365)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = formattedTargetDate,
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Tag Pills
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isPt) "Categoria da Campanha" else "Campaign Category",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.take(3).forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                border = null,
                                elevation = FilterChipDefaults.filterChipElevation(elevation = 2.dp),
                                label = { Text(cat, fontFamily = InterFontFamily, fontSize = 11.5.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.drop(3).forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                border = null,
                                elevation = FilterChipDefaults.filterChipElevation(elevation = 2.dp),
                                label = { Text(cat, fontFamily = InterFontFamily, fontSize = 11.5.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Accent Color Selector
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isPt) "Cor de Destaque" else "Accent Color",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        colorPalettes.forEach { hex ->
                            val color = try {
                                Color(android.graphics.Color.parseColor(hex))
                            } catch (_: Exception) {
                                NeonCyan
                            }
                            val isSelected = selectedColorHex.equals(hex, ignoreCase = true)

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { selectedColorHex = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Strategic Notes Input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isPt) "Metas & Notas Estratégicas" else "Goals & Strategic Notes") },
                    placeholder = { Text(if (isPt) "ex: Alinhar beta VIP com assessoria de imprensa..." else "e.g. Coordinate VIP beta with press...") },
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("timer_notes_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons (Cancel / Save)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("timer_cancel_button")
                    ) {
                        Text(
                            text = if (isPt) "Cancelar" else "Cancel",
                            fontFamily = InterFontFamily
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(
                                    title.trim(),
                                    targetCalendar.timeInMillis,
                                    selectedCategory,
                                    notes.trim(),
                                    selectedColorHex
                                )
                            }
                        },
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("timer_save_button")
                    ) {
                        Text(
                            text = if (isPt) "Salvar Campanha" else "Save Campaign",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepButton(
    text: String,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Text(
            text = text,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp
        )
    }
}
