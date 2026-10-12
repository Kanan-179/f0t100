package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.model.Habit
import com.example.data.repository.ActivityLogRepository
import com.example.data.repository.HabitRepository
import com.example.data.repository.TaskRepository
import com.example.ui.components.AddHabitDialog
import com.example.ui.components.AddNoteDialog
import com.example.ui.components.AddTaskDialog
import com.example.ui.components.HabitDetailSheet
import com.example.ui.screens.HabitsScreen
import com.example.ui.screens.ProgressJournalScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.HabitViewModel
import com.example.ui.viewmodel.HabitViewModelFactory
import com.example.ui.viewmodel.MainTab
import com.example.util.HabitNotificationHelper
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        HabitNotificationHelper.createNotificationChannel(applicationContext)

        val database = AppDatabase.getDatabase(applicationContext)
        val habitRepository = HabitRepository(database.habitDao(), database.activityLogDao())
        val taskRepository = TaskRepository(database.taskDao(), database.activityLogDao())
        val activityLogRepository = ActivityLogRepository(database.activityLogDao())
        val factory = HabitViewModelFactory(habitRepository, taskRepository, activityLogRepository)

        setContent {
            MyApplicationTheme {
                val viewModel: HabitViewModel = viewModel(factory = factory)
                HabitApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitApp(viewModel: HabitViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddHabitDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var selectedHabitForDetail by remember { mutableStateOf<Habit?>(null) }

    // Android 13+ Notification Permission Launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val sent = HabitNotificationHelper.sendKenanDailyReminder(context)
            if (sent) {
                scope.launch {
                    snackbarHostState.showSnackbar("Kənan, bildiriş göndərildi! Bildirişlər panelinə bax 🔔")
                }
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Bildiriş göndərmək üçün icazə verilmədi.")
            }
        }
    }

    fun triggerKenanReminder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            val sent = HabitNotificationHelper.sendKenanDailyReminder(context)
            scope.launch {
                if (sent) {
                    snackbarHostState.showSnackbar("Kənan, bildiriş göndərildi! 🔔")
                } else {
                    snackbarHostState.showSnackbar("Bildiriş göndərilə bilmədi.")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "HabitFlow",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    // Send Kenan notification reminder
                    IconButton(
                        onClick = { triggerKenanReminder() },
                        modifier = Modifier.testTag("send_notification_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Bildiriş Göndər",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = uiState.selectedTab == MainTab.TODAY,
                    onClick = { viewModel.selectTab(MainTab.TODAY) },
                    icon = { Icon(Icons.Default.Today, contentDescription = "Bugün") },
                    label = { Text("Bugün", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("nav_today")
                )
                NavigationBarItem(
                    selected = uiState.selectedTab == MainTab.HABITS,
                    onClick = { viewModel.selectTab(MainTab.HABITS) },
                    icon = { Icon(Icons.Default.Repeat, contentDescription = "Vərdişlər") },
                    label = { Text("Vərdişlər", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("nav_habits")
                )
                NavigationBarItem(
                    selected = uiState.selectedTab == MainTab.TASKS,
                    onClick = { viewModel.selectTab(MainTab.TASKS) },
                    icon = { Icon(Icons.Default.Checklist, contentDescription = "Tasklar") },
                    label = { Text("Tasklar", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tasks")
                )
                NavigationBarItem(
                    selected = uiState.selectedTab == MainTab.PROGRESS_JOURNAL,
                    onClick = { viewModel.selectTab(MainTab.PROGRESS_JOURNAL) },
                    icon = { Icon(Icons.Default.Insights, contentDescription = "Tərəqqi & Jurnal") },
                    label = { Text("Tərəqqi", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("nav_journal")
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = uiState.selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { targetTab ->
                when (targetTab) {
                    MainTab.TODAY -> TodayScreen(
                        state = uiState,
                        onToggleHabit = { habitId -> viewModel.toggleHabit(habitId) },
                        onProgressChange = { habitId, count -> viewModel.updateHabitProgress(habitId, count) },
                        onOpenHabitDetails = { habit -> selectedHabitForDetail = habit },
                        onDeleteHabit = { habit -> viewModel.deleteHabit(habit) },
                        onToggleTask = { taskId -> viewModel.toggleTask(taskId) },
                        onDeleteTask = { task -> viewModel.deleteTask(task) },
                        onAddNewHabit = { showAddHabitDialog = true },
                        onAddNewTask = { showAddTaskDialog = true },
                        onAddReflectionNote = { showAddNoteDialog = true },
                        onSendReminder = { triggerKenanReminder() }
                    )

                    MainTab.HABITS -> HabitsScreen(
                        state = uiState,
                        onCategoryFilterChange = { cat -> viewModel.setHabitCategoryFilter(cat) },
                        onToggleHabit = { habitId -> viewModel.toggleHabit(habitId) },
                        onProgressChange = { habitId, count -> viewModel.updateHabitProgress(habitId, count) },
                        onOpenHabitDetails = { habit -> selectedHabitForDetail = habit },
                        onDeleteHabit = { habit -> viewModel.deleteHabit(habit) },
                        onAddNewHabit = { showAddHabitDialog = true }
                    )

                    MainTab.TASKS -> TasksScreen(
                        state = uiState,
                        onCategoryFilterChange = { cat -> viewModel.setTaskCategoryFilter(cat) },
                        onToggleTask = { taskId -> viewModel.toggleTask(taskId) },
                        onDeleteTask = { task -> viewModel.deleteTask(task) },
                        onAddNewTask = { showAddTaskDialog = true }
                    )

                    MainTab.PROGRESS_JOURNAL -> ProgressJournalScreen(
                        state = uiState,
                        onAddReflectionNote = { showAddNoteDialog = true },
                        onDeleteLog = { log -> viewModel.deleteLog(log) }
                    )
                }
            }
        }
    }

    // Add Habit Dialog
    if (showAddHabitDialog) {
        AddHabitDialog(
            onDismiss = { showAddHabitDialog = false },
            onConfirm = { title, description, category, iconName, colorHex, targetCount, unit, timeOfDay ->
                viewModel.addHabit(
                    title = title,
                    description = description,
                    category = category,
                    iconName = iconName,
                    colorHex = colorHex,
                    targetCount = targetCount,
                    unit = unit,
                    timeOfDay = timeOfDay
                )
                showAddHabitDialog = false
            }
        )
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, description, category, dueDate, dueTime, priority ->
                viewModel.addTask(
                    title = title,
                    description = description,
                    category = category,
                    dueDate = dueDate,
                    dueTime = dueTime,
                    priority = priority
                )
                showAddTaskDialog = false
            }
        )
    }

    // Add Reflection Note Dialog
    if (showAddNoteDialog) {
        AddNoteDialog(
            onDismiss = { showAddNoteDialog = false },
            onConfirm = { title, detail, mood ->
                viewModel.addReflectionNote(title, detail, mood)
                showAddNoteDialog = false
            }
        )
    }

    // Habit Detail Modal Sheet
    selectedHabitForDetail?.let { habit ->
        HabitDetailSheet(
            habit = habit,
            allCompletions = uiState.allCompletions,
            onDismiss = { selectedHabitForDetail = null },
            onDelete = {
                viewModel.deleteHabit(habit)
                selectedHabitForDetail = null
            }
        )
    }
}
