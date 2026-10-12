package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_completions",
    indices = [Index(value = ["habitId", "dateString"], unique = true)]
)
data class HabitCompletion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val dateString: String, // "yyyy-MM-dd"
    val completedCount: Int = 1,
    val isCompleted: Boolean = true,
    val note: String? = null,
    val completedAt: Long = System.currentTimeMillis()
)
