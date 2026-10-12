package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "HABIT", "TASK", "NOTE", "MILESTONE"
    val title: String,
    val detail: String = "",
    val dateString: String, // "yyyy-MM-dd"
    val timestamp: Long = System.currentTimeMillis(),
    val iconName: String = "star",
    val colorHex: Long = 0xFF10B981,
    val mood: String? = null
)
