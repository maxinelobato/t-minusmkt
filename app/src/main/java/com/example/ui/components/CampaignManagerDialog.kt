package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CountdownTimerEntity
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LocalNeumorphicColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TimerStatusFilter {
    ALL,
    ACTIVE,
    UPCOMING
}

@Composable
fun CampaignManagerDialog(
    isPt: Boolean,
    timers: List<CountdownTimerEntity>,
    activeTimerId: Long?,
    onSelectActive: (Long) -> Unit,
    onAddNewTimer: () -> Unit,
    onEditTimer: (CountdownTimerEntity) -> Unit,
    onDeleteTimer: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val neuColors = LocalNeumorphicColors.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf(TimerStatusFilter.ALL) }

    val standardCategories = listOf("Product", "Growth", "Social", "Brand", "PR", "Ads")
    val availableCategories = remember(timers) {
        (standardCategories + timers.map { it.categoryTag }).distinct()
    }

    val now = remember { System.currentTimeMillis() }

    val filteredTimers = remember(timers, searchQuery, selectedCategory, selectedStatus, activeTimerId) {
        timers.filter { timer ->
            val isTimerActive = timer.isActive || timer.id == activeTimerId
            val matchesStatus = when (selectedStatus) {
                TimerStatusFilter.ALL -> true
                TimerStatusFilter.ACTIVE -> isTimerActive
                TimerStatusFilter.UPCOMING -> !isTimerActive && timer.targetTimestamp > now
            }
            val matchesCategory = selectedCategory == null || timer.categoryTag.equals(selectedCategory, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                timer.title.contains(searchQuery, ignoreCase = true) ||
                timer.notes.contains(searchQuery, ignoreCase = true) ||
                timer.categoryTag.contains(searchQuery, ignoreCase = true)

            matchesStatus && matchesCategory && matchesQuery
        }
    }

    val isFilterActive = searchQuery.isNotBlank() || selectedCategory != null || selectedStatus != TimerStatusFilter.ALL

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .testTag("campaign_manager_dialog"),
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPt) "Campanhas de Marketing" else "Marketing Launch Timers",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isPt) "Gerencie, filtre e selecione o cronômetro ativo" else "Search, filter and switch active countdowns",
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("campaign_manager_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // M3 Filled Action Button: "+ New Campaign"
                Button(
                    onClick = onAddNewTimer,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_new_campaign_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPt) "Novo Cronômetro de Lançamento" else "New Launch Countdown",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Persistent Recessed Neumorphic Search Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphicInset(
                            cornerRadius = 16.dp,
                            backgroundColor = neuColors.surface,
                            darkShadowColor = neuColors.insetDark,
                            lightShadowColor = neuColors.insetLight,
                            offset = 2.dp,
                            blur = 4.dp
                        )
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("campaign_search_input"),
                        placeholder = {
                            Text(
                                text = if (isPt) "Buscar por título, categoria ou notas..." else "Search by title, tag, or notes...",
                                fontFamily = InterFontFamily,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = searchQuery.isNotEmpty(),
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                IconButton(
                                    onClick = {
                                        searchQuery = ""
                                        focusManager.clearFocus()
                                    },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("campaign_search_clear_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // M3 Status Filter Chips Row (Borderless)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedStatus == TimerStatusFilter.ALL,
                        onClick = { selectedStatus = TimerStatusFilter.ALL },
                        border = null,
                        elevation = FilterChipDefaults.filterChipElevation(elevation = 2.dp),
                        label = { Text(if (isPt) "Todos" else "All", fontFamily = InterFontFamily, fontSize = 11.5.sp) },
                        modifier = Modifier.testTag("status_filter_all"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = neuColors.surface,
                            labelColor = neuColors.textSecondary
                        )
                    )
                    FilterChip(
                        selected = selectedStatus == TimerStatusFilter.ACTIVE,
                        onClick = { selectedStatus = TimerStatusFilter.ACTIVE },
                        border = null,
                        elevation = FilterChipDefaults.filterChipElevation(elevation = 2.dp),
                        label = { Text(if (isPt) "Ativo" else "Active", fontFamily = InterFontFamily, fontSize = 11.5.sp) },
                        modifier = Modifier.testTag("status_filter_active"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            containerColor = neuColors.surface,
                            labelColor = neuColors.textSecondary
                        )
                    )
                    FilterChip(
                        selected = selectedStatus == TimerStatusFilter.UPCOMING,
                        onClick = { selectedStatus = TimerStatusFilter.UPCOMING },
                        border = null,
                        elevation = FilterChipDefaults.filterChipElevation(elevation = 2.dp),
                        label = { Text(if (isPt) "Em Breve" else "Upcoming", fontFamily = InterFontFamily, fontSize = 11.5.sp) },
                        modifier = Modifier.testTag("status_filter_upcoming"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            containerColor = neuColors.surface,
                            labelColor = neuColors.textSecondary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // M3 Category Filter Chips (Horizontal Scrollable, Borderless)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        border = null,
                        elevation = FilterChipDefaults.filterChipElevation(elevation = 2.dp),
                        label = { Text(if (isPt) "Todas Tags" else "All Tags", fontFamily = InterFontFamily, fontSize = 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = neuColors.surface,
                            labelColor = neuColors.textSecondary
                        ),
                        modifier = Modifier.testTag("category_filter_all")
                    )

                    availableCategories.forEach { category ->
                        val isSelected = selectedCategory?.equals(category, ignoreCase = true) == true
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategory = if (isSelected) null else category
                            },
                            border = null,
                            elevation = FilterChipDefaults.filterChipElevation(elevation = 2.dp),
                            label = { Text(category, fontFamily = InterFontFamily, fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = neuColors.surface,
                                labelColor = neuColors.textSecondary
                            ),
                            modifier = Modifier.testTag("category_filter_${category.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Results Counter & Reset Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val countText = if (isPt) {
                        "Exibindo ${filteredTimers.size} de ${timers.size} campanhas"
                    } else {
                        "Showing ${filteredTimers.size} of ${timers.size} campaigns"
                    }

                    Text(
                        text = countText,
                        fontFamily = InterFontFamily,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isFilterActive) {
                        FilledTonalButton(
                            onClick = {
                                searchQuery = ""
                                selectedCategory = null
                                selectedStatus = TimerStatusFilter.ALL
                                focusManager.clearFocus()
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("reset_filters_btn"),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isPt) "Limpar Filtros" else "Reset Filters",
                                fontFamily = InterFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // List of Launch Timers / Empty State
                if (timers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isPt) "Nenhuma campanha cadastrada. Adicione sua primeira!" else "No launch timers yet. Tap above to create one!",
                            fontFamily = InterFontFamily,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (filteredTimers.isEmpty()) {
                    // Empty search result state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isPt) "Nenhuma campanha encontrada" else "No campaigns found",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isPt) "Tente buscar com outro termo ou alterar os filtros de tag e status." else "Try adjusting your search terms or category and status filters.",
                                fontFamily = InterFontFamily,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            FilledTonalButton(
                                onClick = {
                                    searchQuery = ""
                                    selectedCategory = null
                                    selectedStatus = TimerStatusFilter.ALL
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("empty_state_reset_btn")
                            ) {
                                Text(
                                    text = if (isPt) "Limpar Filtros" else "Clear Filters",
                                    fontFamily = InterFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(filteredTimers, key = { it.id }) { timer ->
                            CampaignTimerCard(
                                timer = timer,
                                isActive = timer.isActive || timer.id == activeTimerId,
                                isPt = isPt,
                                onSelectActive = { onSelectActive(timer.id) },
                                onEdit = { onEditTimer(timer) },
                                onDelete = { onDeleteTimer(timer.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CampaignTimerCard(
    timer: CountdownTimerEntity,
    isActive: Boolean,
    isPt: Boolean,
    onSelectActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val targetDateString = remember(timer.targetTimestamp, isPt) {
        val pattern = if (isPt) "dd/MM/yyyy • HH:mm" else "MMM dd, yyyy • hh:mm a"
        SimpleDateFormat(pattern, if (isPt) Locale("pt", "BR") else Locale.US)
            .format(Date(timer.targetTimestamp))
    }

    // Real-time diff calculation
    val diff = (timer.targetTimestamp - System.currentTimeMillis()).coerceAtLeast(0)
    val seconds = (diff / 1000) % 60
    val minutes = (diff / (1000 * 60)) % 60
    val hours = (diff / (1000 * 60 * 60)) % 24
    val days = diff / (1000 * 60 * 60 * 24)

    val cardModifier = Modifier
        .fillMaxWidth()
        .testTag("campaign_timer_card_${timer.id}")

    if (isActive) {
        ElevatedCard(
            modifier = cardModifier,
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
        ) {
            CampaignTimerCardContent(
                timer = timer,
                isActive = true,
                isPt = isPt,
                targetDateString = targetDateString,
                days = days,
                hours = hours,
                minutes = minutes,
                seconds = seconds,
                onSelectActive = onSelectActive,
                onEdit = onEdit,
                onDelete = onDelete
            )
        }
    } else {
        Card(
            modifier = cardModifier,
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            CampaignTimerCardContent(
                timer = timer,
                isActive = false,
                isPt = isPt,
                targetDateString = targetDateString,
                days = days,
                hours = hours,
                minutes = minutes,
                seconds = seconds,
                onSelectActive = onSelectActive,
                onEdit = onEdit,
                onDelete = onDelete
            )
        }
    }
}

@Composable
private fun CampaignTimerCardContent(
    timer: CountdownTimerEntity,
    isActive: Boolean,
    isPt: Boolean,
    targetDateString: String,
    days: Long,
    hours: Long,
    minutes: Long,
    seconds: Long,
    onSelectActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header Row: Category Badge & Active Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Category Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = timer.categoryTag.uppercase(),
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    letterSpacing = 0.5.sp
                )
            }

            // Active Badge
            if (isActive) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isPt) "ATIVO" else "ACTIVE",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Campaign Title
        Text(
            text = timer.title,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        )

        // Target Launch Date
        Text(
            text = "${if (isPt) "Data Alvo:" else "Target Date:"} $targetDateString",
            fontFamily = InterFontFamily,
            fontSize = 12.sp,
            color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
            else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Live Countdown Ticker Box: Days : Hours : Mins : Secs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isActive) MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.surfaceContainer
                )
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TickerUnit(value = days.toString(), label = if (isPt) "DIAS" else "DAYS")
            TickerSeparator()
            TickerUnit(value = hours.toString().padStart(2, '0'), label = if (isPt) "HORAS" else "HOURS")
            TickerSeparator()
            TickerUnit(value = minutes.toString().padStart(2, '0'), label = if (isPt) "MIN" else "MINS")
            TickerSeparator()
            TickerUnit(value = seconds.toString().padStart(2, '0'), label = if (isPt) "SEG" else "SECS")
        }

        // Notes preview if present
        if (timer.notes.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "“${timer.notes}”",
                fontFamily = InterFontFamily,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Card Bottom Actions: Select Active, Edit, Delete
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (!isActive) {
                Button(
                    onClick = onSelectActive,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("set_active_btn_${timer.id}")
                ) {
                    Text(
                        text = if (isPt) "Ativar Campanha" else "Set as Active",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            } else {
                Text(
                    text = if (isPt) "● Conectado ao Lock Screen" else "● Driving lock screen timer",
                    fontFamily = InterFontFamily,
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("edit_timer_btn_${timer.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_timer_btn_${timer.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TickerUnit(
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun TickerSeparator() {
    Text(
        text = ":",
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.outline
    )
}
