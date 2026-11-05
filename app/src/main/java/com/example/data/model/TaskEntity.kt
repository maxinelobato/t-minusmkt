package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timelinePt: String,
    val timelineEn: String,
    val titlePt: String,
    val titleEn: String,
    val iconType: String,
    val daysRemaining: Int,
    val targetDateFormatted: String,
    val isCompleted: Boolean = false,
    val isCustom: Boolean = false,
    val orderIndex: Int = 0
)
