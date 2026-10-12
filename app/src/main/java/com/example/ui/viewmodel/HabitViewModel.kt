package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ActivityLog
import com.example.data.model.Habit
import com.example.data.model.HabitCompletion
import com.example.data.model.HabitProgressItem
import com.example.data.model.ProgressStatus
import com.example.data.model.TaskItem
import com.example.data.repository.ActivityLogRepository
import com.example.data.repository.HabitRepository
import com.example.data.repository.TaskRepository
import com.example.ui.components.DayTrendData
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    TODAY,
    HABITS,
    TASKS,
    PROGRESS_JOURNAL
}

data class HabitUiState(
    val habits: List<Habit> = emptyList(),
    val todayCompletions: Map<Long, HabitCompletion> = emptyMap(),
    val allCompletions: List<HabitCompletion> = emptyList(),
    val tasks: List<TaskItem> = emptyList(),
    val activityLogs: List<ActivityLog> = emptyList(),
    val selectedTab: MainTab = MainTab.TODAY,
    val habitCategoryFilter: String = "Hamısı",
    val taskCategoryFilter: String = "Hamısı",
    val todayDateString: String = DateUtils.getTodayString()
) {
    val habitsWithProgress: List<HabitProgressItem>
        get() = habits.map { habit ->
            val comp = todayCompletions[habit.id]
            val completedCount = comp?.completedCount ?: 0
            val targetCount = habit.targetCount.coerceAtLeast(1)
            val isCompleted = comp?.isCompleted == true || completedCount >= targetCount
            val status = when {
                isCompleted -> ProgressStatus.COMPLETED
                completedCount > 0 -> ProgressStatus.IN_PROGRESS
                else -> ProgressStatus.NOT_STARTED
            }
            val percent = (completedCount.toFloat() / targetCount).coerceIn(0f, 1f)
            HabitProgressItem(
                habit = habit,
                completedCount = completedCount,
                targetCount = targetCount,
                progressStatus = status,
                progressPercent = percent,
                isCompleted = isCompleted
            )
        }

    val weeklyTrendData: List<DayTrendData>
        get() {
            val past7 = DateUtils.getPastDays(7)
            val compsByDate = allCompletions.filter { it.isCompleted }.groupBy { it.dateString }
            val total = habits.size.coerceAtLeast(1)
            return past7.map { dateStr ->
                val doneCount = compsByDate[dateStr]?.size ?: 0
                val rate = (doneCount.toFloat() / total).coerceIn(0f, 1f)
                DayTrendData(
                    dateString = dateStr,
                    dayLabel = DateUtils.getShortAzerbaijaniDay(dateStr),
                    completedHabits = doneCount,
                    totalHabits = total,
                    completionRate = rate
                )
            }
        }

    val completedHabitsTodayCount: Int
        get() = habits.count { habit ->
            val comp = todayCompletions[habit.id]
            comp != null && comp.isCompleted
        }

    val totalHabitsCount: Int
        get() = habits.size

    val habitProgressPercent: Float
        get() = if (totalHabitsCount == 0) 0f else completedHabitsTodayCount.toFloat() / totalHabitsCount

    val completedTasksCount: Int
        get() = tasks.count { it.isCompleted }

    val totalTasksCount: Int
        get() = tasks.size

    val taskProgressPercent: Float
        get() = if (totalTasksCount == 0) 0f else completedTasksCount.toFloat() / totalTasksCount

    val maxStreak: Int
        get() = habits.maxOfOrNull { it.currentStreak } ?: 0

    val totalCompletionsAllTime: Int
        get() = allCompletions.count { it.isCompleted }
}

class HabitViewModel(
    private val habitRepository: HabitRepository,
    private val taskRepository: TaskRepository,
    private val activityLogRepository: ActivityLogRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(MainTab.TODAY)
    private val _habitCategoryFilter = MutableStateFlow("Hamısı")
    private val _taskCategoryFilter = MutableStateFlow("Hamısı")
    private val _todayString = MutableStateFlow(DateUtils.getTodayString())

    init {
        viewModelScope.launch {
            habitRepository.seedDefaultsIfEmpty()
            taskRepository.seedDefaultsIfEmpty()
            activityLogRepository.seedDefaultsIfEmpty()
            // Clean slate: ensure all past legacy completions are cleared and habits/tasks start from zero
            habitRepository.clearAllCompletionsAndResetToZero()
            taskRepository.clearAllPastDataAndStartFresh()
        }
    }

    private val filterState = combine(_selectedTab, _habitCategoryFilter, _taskCategoryFilter) { tab, hCat, tCat ->
        Triple(tab, hCat, tCat)
    }

    val uiState: StateFlow<HabitUiState> = combine(
        habitRepository.allHabits,
        habitRepository.allCompletions,
        taskRepository.allTasks,
        activityLogRepository.allLogs,
        filterState
    ) { habits, allComps, tasks, logs, filters ->
        val (tab, habitCat, taskCat) = filters
        val todayStr = _todayString.value
        val todayCompsMap = allComps
            .filter { it.dateString == todayStr }
            .associateBy { it.habitId }

        HabitUiState(
            habits = habits,
            todayCompletions = todayCompsMap,
            allCompletions = allComps,
            tasks = tasks,
            activityLogs = logs,
            selectedTab = tab,
            habitCategoryFilter = habitCat,
            taskCategoryFilter = taskCat,
            todayDateString = todayStr
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitUiState()
    )

    fun selectTab(tab: MainTab) {
        _selectedTab.value = tab
    }

    fun setHabitCategoryFilter(category: String) {
        _habitCategoryFilter.value = category
    }

    fun setTaskCategoryFilter(category: String) {
        _taskCategoryFilter.value = category
    }

    fun toggleHabit(habitId: Long) {
        viewModelScope.launch {
            habitRepository.toggleHabitCompletion(habitId, _todayString.value)
        }
    }

    fun updateHabitProgress(habitId: Long, newCount: Int) {
        viewModelScope.launch {
            habitRepository.updateHabitProgress(habitId, _todayString.value, newCount)
        }
    }

    fun addHabit(
        title: String,
        description: String,
        category: String,
        iconName: String,
        colorHex: Long,
        targetCount: Int,
        unit: String,
        timeOfDay: String,
        targetFrequency: String = "DAILY"
    ) {
        viewModelScope.launch {
            val habit = Habit(
                name = title,
                description = description,
                category = category,
                iconName = iconName,
                colorHex = colorHex,
                targetFrequency = targetFrequency,
                targetCount = targetCount.coerceAtLeast(1),
                unit = unit.ifBlank { "dəfə" },
                timeOfDay = timeOfDay
            )
            habitRepository.insertHabit(habit)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habit)
        }
    }

    fun toggleTask(taskId: Long) {
        viewModelScope.launch {
            taskRepository.toggleTaskCompletion(taskId)
        }
    }

    fun addTask(
        title: String,
        description: String,
        category: String,
        dueDate: String,
        dueTime: String,
        priority: String
    ) {
        viewModelScope.launch {
            val task = TaskItem(
                title = title,
                description = description,
                category = category,
                dueDate = dueDate,
                dueTime = dueTime,
                priority = priority
            )
            taskRepository.insertTask(task)
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
        }
    }

    fun addReflectionNote(title: String, detail: String, mood: String) {
        viewModelScope.launch {
            activityLogRepository.addReflectionNote(title, detail, mood)
        }
    }

    fun updateHabitFrequency(habitId: Long, newFrequency: String) {
        viewModelScope.launch {
            val habit = uiState.value.habits.firstOrNull { it.id == habitId } ?: return@launch
            habitRepository.updateHabit(habit.copy(targetFrequency = newFrequency))
        }
    }

    fun setHabitProgressStatus(habitId: Long, status: ProgressStatus) {
        viewModelScope.launch {
            val progressItem = uiState.value.habitsWithProgress.firstOrNull { it.habit.id == habitId } ?: return@launch
            val newCount = when (status) {
                ProgressStatus.COMPLETED -> progressItem.habit.targetCount
                ProgressStatus.IN_PROGRESS -> (progressItem.habit.targetCount / 2).coerceAtLeast(1)
                ProgressStatus.NOT_STARTED -> 0
            }
            habitRepository.updateHabitProgress(habitId, _todayString.value, newCount)
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            habitRepository.resetAllData()
            taskRepository.resetAllData()
            activityLogRepository.resetAllData()
        }
    }

    fun deleteLog(log: ActivityLog) {
        viewModelScope.launch {
            activityLogRepository.deleteLog(log)
        }
    }
}

class HabitViewModelFactory(
    private val habitRepository: HabitRepository,
    private val taskRepository: TaskRepository,
    private val activityLogRepository: ActivityLogRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitViewModel::class.java)) {
            return HabitViewModel(habitRepository, taskRepository, activityLogRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
