package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.util.DateUtils
import com.example.util.HabitIconHelper

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        description: String,
        category: String,
        dueDate: String,
        dueTime: String,
        priority: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(HabitIconHelper.taskCategories.first()) }
    var priority by remember { mutableStateOf("MEDIUM") }
    var dueChoice by remember { mutableStateOf("Bugün") }
    var dueTime by remember { mutableStateOf("") }

    val todayStr = remember { DateUtils.getTodayString() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Yeni Task Təyin Et",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Taskın adı *") },
                    placeholder = { Text("məs: Hesabatı hazırla") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input")
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Əlavə qeyd və detallar") },
                    placeholder = { Text("məs: Rəhbərliyə email vasitəsilə göndəriləcək") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // Priority Selection
                Text(
                    text = "Prioritet",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "HIGH" to "🔴 Yüksək",
                        "MEDIUM" to "🟡 Orta",
                        "LOW" to "🟢 Aşağı"
                    ).forEach { (pKey, pLabel) ->
                        FilterChip(
                            selected = priority == pKey,
                            onClick = { priority = pKey },
                            label = { Text(pLabel) }
                        )
                    }
                }

                // Category
                Text(
                    text = "Kateqoriya",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HabitIconHelper.taskCategories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                // Due Date
                Text(
                    text = "İcra tarixi",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Bugün", "İstənilən vaxt").forEach { choice ->
                        FilterChip(
                            selected = dueChoice == choice,
                            onClick = { dueChoice = choice },
                            label = { Text(choice) }
                        )
                    }
                }

                // Due Time
                OutlinedTextField(
                    value = dueTime,
                    onValueChange = { dueTime = it },
                    label = { Text("Vaxt (istəyə bağlı)") },
                    placeholder = { Text("məs: 16:30") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val computedDueDate = if (dueChoice == "Bugün") todayStr else ""
                        onConfirm(
                            title.trim(),
                            description.trim(),
                            selectedCategory,
                            computedDueDate,
                            dueTime.trim(),
                            priority
                        )
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("submit_task_button")
            ) {
                Text("Taskı Əlavə Et")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Ləğv et")
            }
        }
    )
}
