package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LaunchCategory
import com.example.data.model.TaskEntity
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LocalNeumorphicColors

/**
 * Horizontally scrollable row of borderless Material 3 / Neumorphic Filter Chips for filtering
 * launch milestones by category (All, Marketing, Engineering, Design, Operations).
 */
@Composable
fun MilestoneCategoryFilterRow(
    allTasks: List<TaskEntity>,
    selectedCategory: String?,
    isPt: Boolean,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val neuColors = LocalNeumorphicColors.current

    val categories = LaunchCategory.entries

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(vertical = 4.dp)
            .testTag("milestone_category_filter_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "All" / "Todas" Chip
        val isAllSelected = selectedCategory == null
        val totalCount = allTasks.size

        FilterChip(
            selected = isAllSelected,
            onClick = {
                triggerInstantHaptic(context)
                onCategorySelected(null)
            },
            border = null,
            elevation = FilterChipDefaults.filterChipElevation(
                elevation = if (isAllSelected) 0.dp else 3.dp,
                pressedElevation = 1.dp
            ),
            label = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isPt) "Todas" else "All",
                        fontFamily = InterFontFamily,
                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                    CategoryCountBadge(
                        count = totalCount,
                        isSelected = isAllSelected
                    )
                }
            },
            leadingIcon = if (isAllSelected) {
                {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else null,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                containerColor = neuColors.surface,
                labelColor = neuColors.textSecondary
            ),
            modifier = Modifier.testTag("category_filter_chip_all")
        )

        // Category-specific Chips
        categories.forEach { category ->
            val isSelected = selectedCategory.equals(category.id, ignoreCase = true)
            val categoryCount = allTasks.count { it.category.equals(category.id, ignoreCase = true) }

            FilterChip(
                selected = isSelected,
                onClick = {
                    triggerInstantHaptic(context)
                    if (isSelected) {
                        onCategorySelected(null)
                    } else {
                        onCategorySelected(category.id)
                    }
                },
                border = null,
                elevation = FilterChipDefaults.filterChipElevation(
                    elevation = if (isSelected) 0.dp else 3.dp,
                    pressedElevation = 1.dp
                ),
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = category.primaryColor,
                            modifier = Modifier.size(8.dp)
                        ) {}

                        Text(
                            text = category.getDisplayName(isPt),
                            fontFamily = InterFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )

                        CategoryCountBadge(
                            count = categoryCount,
                            isSelected = isSelected
                        )
                    }
                },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = category.primaryColor.copy(alpha = 0.22f),
                    selectedLabelColor = category.primaryColor,
                    containerColor = neuColors.surface,
                    labelColor = neuColors.textSecondary
                ),
                modifier = Modifier.testTag("category_filter_chip_${category.id.lowercase()}")
            )
        }
    }
}

@Composable
private fun CategoryCountBadge(
    count: Int,
    isSelected: Boolean
) {
    Surface(
        shape = CircleShape,
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        modifier = Modifier.padding(start = 2.dp)
    ) {
        Text(
            text = count.toString(),
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
