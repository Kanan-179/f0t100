package com.example.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object HabitIconHelper {
    fun colorFromHex(hex: Long): Color {
        // Construct 32-bit ARGB Compose Color from hex Long, avoiding Compose ULong color space crash
        return Color(hex.toInt())
    }

    fun getIcon(name: String): ImageVector {
        return when (name) {
            "water_drop" -> Icons.Default.WaterDrop
            "menu_book", "book" -> Icons.Default.MenuBook
            "fitness_center" -> Icons.Default.FitnessCenter
            "directions_run" -> Icons.Default.DirectionsRun
            "directions_walk" -> Icons.Default.DirectionsWalk
            "self_improvement" -> Icons.Default.SelfImprovement
            "translate" -> Icons.Default.Translate
            "laptop_mac" -> Icons.Default.LaptopMac
            "savings" -> Icons.Default.Savings
            "nights_stay" -> Icons.Default.NightsStay
            "wb_sunny" -> Icons.Default.WbSunny
            "edit_note" -> Icons.Default.EditNote
            "assignment" -> Icons.Default.Assignment
            "headphones" -> Icons.Default.Headphones
            "spa" -> Icons.Default.Spa
            "psychology" -> Icons.Default.Psychology
            "timer" -> Icons.Default.Timer
            "fire" -> Icons.Default.LocalFireDepartment
            "auto_awesome" -> Icons.Default.AutoAwesome
            else -> Icons.Default.CheckCircle
        }
    }

    val availableIcons: List<Pair<String, String>> = listOf(
        "water_drop" to "Su içmək",
        "menu_book" to "Kitab mütaliəsi",
        "fitness_center" to "İdman & Gimnastika",
        "directions_run" to "Qaçış & Kardio",
        "directions_walk" to "Piyada gəzinti",
        "self_improvement" to "Meditasiya & Rahatlıq",
        "translate" to "Dil öyrənmə",
        "laptop_mac" to "İş & Kodlaşdırma",
        "savings" to "Qənaət & Büdcə",
        "nights_stay" to "Erkən yuxu",
        "wb_sunny" to "Erkən oyanış",
        "psychology" to "Zehin tapmacaları"
    )

    val availableColors = listOf(
        0xFF10B981, // Emerald
        0xFF0EA5E9, // Sky Blue
        0xFF8B5CF6, // Purple
        0xFFF59E0B, // Amber / Orange
        0xFFEF4444, // Red
        0xFFEC4899, // Pink
        0xFF14B8A6, // Teal
        0xFF6366F1  // Indigo
    )

    val habitCategories = listOf(
        "Sağlamlıq",
        "Məhsuldarlıq",
        "İdman",
        "Zehin",
        "Öyrənmə",
        "Şəxsi"
    )

    val taskCategories = listOf(
        "İş",
        "Şəxsi",
        "Təhsil",
        "Ev",
        "Layihə"
    )
}
