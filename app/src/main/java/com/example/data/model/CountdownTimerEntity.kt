package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database entity representing an individual marketing launch countdown timer.
 * Stores target timestamp, custom campaign label, category tag, and notes.
 */
@Entity(tableName = "countdown_timers")
data class CountdownTimerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetTimestamp: Long,
    val categoryTag: String = "Product",
    val notes: String = "",
    val accentColorHex: String = "#00FFFF",
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
