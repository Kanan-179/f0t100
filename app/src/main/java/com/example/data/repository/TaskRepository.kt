package com.example.data.repository

import com.example.data.local.ActivityLogDao
import com.example.data.local.TaskDao
import com.example.data.model.ActivityLog
import com.example.data.model.TaskItem
import com.example.util.DateUtils
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao,
    private val activityLogDao: ActivityLogDao
) {
    val allTasks: Flow<List<TaskItem>> = taskDao.getAllTasks()

    fun getTasksForDate(dateString: String): Flow<List<TaskItem>> =
        taskDao.getTasksForDate(dateString)

    suspend fun insertTask(task: TaskItem): Long {
        val id = taskDao.insertTask(task)
        activityLogDao.insertLog(
            ActivityLog(
                type = "TASK",
                title = "Yeni Task Təyin Edildi",
                detail = "\"${task.title}\" (${task.category}) əlavə edildi.",
                dateString = DateUtils.getTodayString(),
                colorHex = if (task.priority == "HIGH") 0xFFEF4444 else 0xFF0D9488,
                iconName = "assignment"
            )
        )
        return id
    }

    suspend fun updateTask(task: TaskItem) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: TaskItem) {
        taskDao.deleteTask(task)
    }

    suspend fun toggleTaskCompletion(taskId: Long) {
        val task = taskDao.getTaskById(taskId) ?: return
        val newStatus = !task.isCompleted
        val completedAt = if (newStatus) System.currentTimeMillis() else null

        taskDao.updateTask(
            task.copy(
                isCompleted = newStatus,
                completedAt = completedAt
            )
        )

        if (newStatus) {
            activityLogDao.insertLog(
                ActivityLog(
                    type = "TASK",
                    title = "Task Tamamlandı ✅",
                    detail = "\"${task.title}\" uğurla yekunlaşdırıldı.",
                    dateString = DateUtils.getTodayString(),
                    colorHex = 0xFF10B981,
                    iconName = "check_circle",
                    mood = "🎯 Hazırdır!"
                )
            )
        }
    }

    suspend fun clearAllPastDataAndStartFresh() {
        taskDao.resetAllTasksProgress()
    }

    suspend fun resetAllData() {
        taskDao.deleteAllTasks()
        seedBeneficialDefaults()
    }

    suspend fun seedDefaultsIfEmpty() {
        if (taskDao.getTaskCount() == 0) {
            seedBeneficialDefaults()
        }
    }

    private suspend fun seedBeneficialDefaults() {
        val today = DateUtils.getTodayString()
        val task1 = TaskItem(
            title = "Sabahkı günün 3 əsas prioritetini müəyyən etmək",
            description = "Fokus və məhsuldarlıq üçün günün ən vacib 3 işini seçmək",
            category = "İş",
            dueDate = today,
            dueTime = "21:00",
            priority = "HIGH",
            isCompleted = false,
            completedAt = null
        )
        taskDao.insertTask(task1)

        val task2 = TaskItem(
            title = "30 dəqiqə fasiləsiz dərindən işləmək (Deep Work)",
            description = "Bütün bildirişləri söndürüb əsas layihəyə tam fokuslanmaq",
            category = "İş",
            dueDate = today,
            dueTime = "11:30",
            priority = "HIGH",
            isCompleted = false,
            completedAt = null
        )
        taskDao.insertTask(task2)

        val task3 = TaskItem(
            title = "Təmiz havada 20 dəqiqəlik piyada gəzinti",
            description = "Bədəni hərəkətdə saxlamaq və beyni təmizləmək",
            category = "Sağlamlıq",
            dueDate = today,
            dueTime = "18:00",
            priority = "MEDIUM",
            isCompleted = false,
            completedAt = null
        )
        taskDao.insertTask(task3)

        val task4 = TaskItem(
            title = "Həftəlik büdcə və maliyyə xərclərini nəzərdən keçirmək",
            description = "Qənaət hədəfləri və gərəksiz xərclərin təhlili",
            category = "Şəxsi",
            dueDate = today,
            dueTime = "20:30",
            priority = "MEDIUM",
            isCompleted = false,
            completedAt = null
        )
        taskDao.insertTask(task4)

        val task5 = TaskItem(
            title = "Günün yekununda HabitFlow-a nə etmisənsə daxil etmək",
            description = "Kənan, günün nəticələrini və etdiklərini qeyd etməyi unutma!",
            category = "Şəxsi",
            dueDate = today,
            dueTime = "22:00",
            priority = "HIGH",
            isCompleted = false,
            completedAt = null
        )
        taskDao.insertTask(task5)
    }
}
