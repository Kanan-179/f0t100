package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

enum class ProgressStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED
}

data class HabitProgressItem(
    val habit: Habit,
    val completedCount: Int,
    val targetCount: Int,
    val progressStatus: ProgressStatus,
    val progressPercent: Float,
    val isCompleted: Boolean
)

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    val description: String = "",
    val category: String = "Sağlamlıq", // Sağlamlıq, Məhsuldarlıq, İdman, Zehin, Öyrənmə
    val iconName: String = "check_circle",
    val colorHex: Long = 0xFF10B981,
    @ColumnInfo(name = "target_frequency") val targetFrequency: String = "DAILY", // DAILY, WEEKDAYS, WEEKENDS, WEEKLY
    val targetCount: Int = 1,
    val unit: String = "dəfə", // dəfə, stəkan, səhifə, dəqiqə
    val timeOfDay: String = "İstənilən vaxt", // Səhər, Günorta, Axşam, İstənilən vaxt
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalCompletions: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
) {
    @get:Ignore
    val title: String get() = name

    @get:Ignore
    val frequency: String get() = targetFrequency
}
