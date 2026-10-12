package com.example.data.repository

import com.example.data.local.ActivityLogDao
import com.example.data.model.ActivityLog
import com.example.util.DateUtils
import kotlinx.coroutines.flow.Flow

class ActivityLogRepository(
    private val activityLogDao: ActivityLogDao
) {
    val allLogs: Flow<List<ActivityLog>> = activityLogDao.getAllLogs()

    fun getLogsForDate(dateString: String): Flow<List<ActivityLog>> =
        activityLogDao.getLogsForDate(dateString)

    suspend fun insertLog(log: ActivityLog): Long = activityLogDao.insertLog(log)

    suspend fun addReflectionNote(title: String, detail: String, mood: String) {
        val today = DateUtils.getTodayString()
        activityLogDao.insertLog(
            ActivityLog(
                type = "NOTE",
                title = title.ifBlank { "Günün Qeydi ✍️" },
                detail = detail,
                dateString = today,
                colorHex = 0xFF8B5CF6,
                iconName = "edit_note",
                mood = mood
            )
        )
    }

    suspend fun deleteLog(log: ActivityLog) = activityLogDao.deleteLog(log)

    suspend fun clearAll() = activityLogDao.clearAllLogs()

    suspend fun resetAllData() {
        activityLogDao.clearAllLogs()
        seedBeneficialDefaults()
    }

    suspend fun seedDefaultsIfEmpty() {
        if (activityLogDao.getLogCount() == 0) {
            seedBeneficialDefaults()
        }
    }

    private suspend fun seedBeneficialDefaults() {
        val today = DateUtils.getTodayString()
        activityLogDao.insertLog(
            ActivityLog(
                type = "NOTE",
                title = "0-dan Yeni Başlanğıc! 🎯",
                detail = "Kənan, bu gündən etibarən bütün vərdiş və tasklarını özün daxil etməyə başla. Uğurlar!",
                dateString = today,
                timestamp = System.currentTimeMillis(),
                colorHex = 0xFF10B981,
                iconName = "wb_sunny",
                mood = "🔥 Başlayırıq!"
            )
        )
    }
}
