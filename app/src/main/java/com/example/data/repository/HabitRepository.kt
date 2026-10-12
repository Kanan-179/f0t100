package com.example.data.repository

import com.example.data.local.ActivityLogDao
import com.example.data.local.HabitDao
import com.example.data.model.ActivityLog
import com.example.data.model.Habit
import com.example.data.model.HabitCompletion
import com.example.util.DateUtils
import kotlinx.coroutines.flow.Flow

class HabitRepository(
    private val habitDao: HabitDao,
    private val activityLogDao: ActivityLogDao
) {
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()
    val allCompletions: Flow<List<HabitCompletion>> = habitDao.getAllCompletions()

    fun getCompletionsForDate(dateStr: String): Flow<List<HabitCompletion>> =
        habitDao.getCompletionsForDate(dateStr)

    fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletion>> =
        habitDao.getCompletionsForHabit(habitId)

    suspend fun insertHabit(habit: Habit): Long {
        val id = habitDao.insertHabit(habit)
        activityLogDao.insertLog(
            ActivityLog(
                type = "MILESTONE",
                title = "Yeni Vərdiş Başlandı",
                detail = "\"${habit.name}\" hədəfi əlavə edildi.",
                dateString = DateUtils.getTodayString(),
                colorHex = habit.colorHex,
                iconName = habit.iconName
            )
        )
        return id
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteCompletionsForHabit(habit.id)
        habitDao.deleteHabit(habit)
    }

    suspend fun toggleHabitCompletion(habitId: Long, dateString: String) {
        val existing = habitDao.getCompletion(habitId, dateString)
        val habit = habitDao.getHabitByIdDirect(habitId) ?: return

        if (existing != null && existing.isCompleted) {
            // Uncheck
            habitDao.deleteCompletion(habitId, dateString)
            recalculateStreak(habitId)
        } else {
            // Check as completed
            val completion = HabitCompletion(
                habitId = habitId,
                dateString = dateString,
                completedCount = habit.targetCount,
                isCompleted = true,
                completedAt = System.currentTimeMillis()
            )
            habitDao.insertOrUpdateCompletion(completion)
            recalculateStreak(habitId)

            // Log activity
            activityLogDao.insertLog(
                ActivityLog(
                    type = "HABIT",
                    title = "Vərdiş Tamamlandı",
                    detail = "\"${habit.name}\" uğurla icra olundu! (${habit.targetCount} ${habit.unit})",
                    dateString = dateString,
                    colorHex = habit.colorHex,
                    iconName = habit.iconName,
                    mood = "🔥 Əla!"
                )
            )
        }
    }

    suspend fun updateHabitProgress(habitId: Long, dateString: String, newCount: Int) {
        val habit = habitDao.getHabitByIdDirect(habitId) ?: return
        val clampedCount = newCount.coerceAtLeast(0)
        val isCompleted = clampedCount >= habit.targetCount

        if (clampedCount == 0) {
            habitDao.deleteCompletion(habitId, dateString)
        } else {
            val completion = HabitCompletion(
                habitId = habitId,
                dateString = dateString,
                completedCount = clampedCount,
                isCompleted = isCompleted,
                completedAt = System.currentTimeMillis()
            )
            habitDao.insertOrUpdateCompletion(completion)

            if (isCompleted) {
                activityLogDao.insertLog(
                    ActivityLog(
                        type = "HABIT",
                        title = "Hədəfə Çatıldı",
                        detail = "\"${habit.name}\" üçün $clampedCount / ${habit.targetCount} ${habit.unit} tamamlandı!",
                        dateString = dateString,
                        colorHex = habit.colorHex,
                        iconName = habit.iconName,
                        mood = "⭐ Super!"
                    )
                )
            }
        }
        recalculateStreak(habitId)
    }

    private suspend fun recalculateStreak(habitId: Long) {
        val habit = habitDao.getHabitByIdDirect(habitId) ?: return
        val pastDays = DateUtils.getPastDays(60).reversed()
        
        var currentStreak = 0
        var foundBreak = false

        for (day in pastDays) {
            val comp = habitDao.getCompletion(habitId, day)
            if (comp != null && comp.isCompleted) {
                if (!foundBreak) {
                    currentStreak++
                }
            } else {
                if (day == DateUtils.getTodayString()) {
                    continue
                } else {
                    foundBreak = true
                }
            }
        }

        val newBest = maxOf(habit.bestStreak, currentStreak)
        val totalCompletions = if (habit.totalCompletions <= 0) currentStreak else habit.totalCompletions + 1

        habitDao.updateHabit(
            habit.copy(
                currentStreak = currentStreak,
                bestStreak = newBest,
                totalCompletions = maxOf(totalCompletions, currentStreak)
            )
        )
    }

    suspend fun clearAllCompletionsAndResetToZero() {
        habitDao.deleteAllCompletions()
        val habits = habitDao.getAllHabitsDirect()
        for (h in habits) {
            habitDao.updateHabit(h.copy(currentStreak = 0, bestStreak = 0, totalCompletions = 0))
        }
    }

    suspend fun resetAllData() {
        habitDao.deleteAllCompletions()
        habitDao.deleteAllHabits()
        seedBeneficialDefaults()
    }

    suspend fun seedDefaultsIfEmpty() {
        if (habitDao.getHabitCount() == 0) {
            seedBeneficialDefaults()
        }
    }

    private suspend fun seedBeneficialDefaults() {
        val habit1 = Habit(
            name = "8 stəkan su içmək",
            description = "Bədəni gümrah və nəmli saxlamaq üçün hər gün 2 litr su",
            category = "Sağlamlıq",
            iconName = "water_drop",
            colorHex = 0xFF0EA5E9,
            targetFrequency = "DAILY",
            targetCount = 8,
            unit = "stəkan",
            timeOfDay = "İstənilən vaxt",
            currentStreak = 0,
            bestStreak = 0,
            totalCompletions = 0
        )
        habitDao.insertHabit(habit1)

        val habit2 = Habit(
            name = "Kitab oxumaq (20 səhifə)",
            description = "Şəxsi inkişaf və dünyagörüşü üçün gündəlik mütaliə",
            category = "Öyrənmə",
            iconName = "menu_book",
            colorHex = 0xFF10B981,
            targetFrequency = "DAILY",
            targetCount = 20,
            unit = "səhifə",
            timeOfDay = "Axşam",
            currentStreak = 0,
            bestStreak = 0,
            totalCompletions = 0
        )
        habitDao.insertHabit(habit2)

        val habit3 = Habit(
            name = "Səhər İdmanı və Dartınma",
            description = "Qan dövranını sürətləndirmək və günə enerjili başlamaq",
            category = "İdman",
            iconName = "fitness_center",
            colorHex = 0xFFF59E0B,
            targetFrequency = "DAILY",
            targetCount = 15,
            unit = "dəqiqə",
            timeOfDay = "Səhər",
            currentStreak = 0,
            bestStreak = 0,
            totalCompletions = 0
        )
        habitDao.insertHabit(habit3)

        val habit4 = Habit(
            name = "Göz gimnastikası & Ekran fasiləsi",
            description = "Kompüter və telefon qarşısında gözləri qorumaq üçün fasilə",
            category = "Sağlamlıq",
            iconName = "spa",
            colorHex = 0xFF14B8A6,
            targetFrequency = "DAILY",
            targetCount = 3,
            unit = "dəfə",
            timeOfDay = "Günorta",
            currentStreak = 0,
            bestStreak = 0,
            totalCompletions = 0
        )
        habitDao.insertHabit(habit4)

        val habit5 = Habit(
            name = "Xarici dil öyrənmək",
            description = "Hər gün ən az 5 yeni xarici dil sözü və ya ifadəsi",
            category = "Öyrənmə",
            iconName = "translate",
            colorHex = 0xFFEC4899,
            targetFrequency = "DAILY",
            targetCount = 5,
            unit = "söz",
            timeOfDay = "Günorta",
            currentStreak = 0,
            bestStreak = 0,
            totalCompletions = 0
        )
        habitDao.insertHabit(habit5)

        val habit6 = Habit(
            name = "Günün planını qurmaq",
            description = "Günün 3 əsas prioritetini əvvəlcədən təyin etmək",
            category = "Məhsuldarlıq",
            iconName = "edit_note",
            colorHex = 0xFF8B5CF6,
            targetFrequency = "DAILY",
            targetCount = 1,
            unit = "dəfə",
            timeOfDay = "Səhər",
            currentStreak = 0,
            bestStreak = 0,
            totalCompletions = 0
        )
        habitDao.insertHabit(habit6)

        val habit7 = Habit(
            name = "10 dəqiqə Dərin Nəfəs & Meditasiya",
            description = "Zehni sakitləşdirmək, diqqəti cəmləmək və stressi atmaq",
            category = "Zehin",
            iconName = "self_improvement",
            colorHex = 0xFF6366F1,
            targetFrequency = "DAILY",
            targetCount = 10,
            unit = "dəqiqə",
            timeOfDay = "Axşam",
            currentStreak = 0,
            bestStreak = 0,
            totalCompletions = 0
        )
        habitDao.insertHabit(habit7)

        val habit8 = Habit(
            name = "Erkən yatmaq (23:00)",
            description = "Keyfiyyətli beyin dincəlməsi və sağlam yuxu rejimi",
            category = "Sağlamlıq",
            iconName = "nights_stay",
            colorHex = 0xFF3B82F6,
            targetFrequency = "DAILY",
            targetCount = 1,
            unit = "dəfə",
            timeOfDay = "Axşam",
            currentStreak = 0,
            bestStreak = 0,
            totalCompletions = 0
        )
        habitDao.insertHabit(habit8)

        // Clean start: NO past completions inserted. Everything starts from zero!
    }
}
