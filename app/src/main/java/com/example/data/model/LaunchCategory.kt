package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class LaunchCategory(
    val id: String,
    val labelPt: String,
    val labelEn: String,
    val primaryColor: Color,
    val darkBgColor: Color,
    val lightBgColor: Color
) {
    MARKETING(
        id = "Marketing",
        labelPt = "Marketing",
        labelEn = "Marketing",
        primaryColor = Color(0xFF0284C7),
        darkBgColor = Color(0xFF0C4A6E),
        lightBgColor = Color(0xFFE0F2FE)
    ),
    ENGINEERING(
        id = "Engineering",
        labelPt = "Engenharia",
        labelEn = "Engineering",
        primaryColor = Color(0xFF6366F1),
        darkBgColor = Color(0xFF312E81),
        lightBgColor = Color(0xFFEEF2FF)
    ),
    DESIGN(
        id = "Design",
        labelPt = "Design",
        labelEn = "Design",
        primaryColor = Color(0xFFE11D48),
        darkBgColor = Color(0xFF881337),
        lightBgColor = Color(0xFFFFE4E6)
    ),
    OPERATIONS(
        id = "Operations",
        labelPt = "Operações",
        labelEn = "Operations",
        primaryColor = Color(0xFF059669),
        darkBgColor = Color(0xFF064E3B),
        lightBgColor = Color(0xFFD1FAE5)
    );

    fun getDisplayName(isPt: Boolean): String = if (isPt) labelPt else labelEn

    companion object {
        fun fromId(id: String?): LaunchCategory {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: MARKETING
        }
    }
}
