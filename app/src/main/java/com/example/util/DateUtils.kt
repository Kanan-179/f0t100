package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val isoFormatThreadLocal = ThreadLocal.withInitial {
        SimpleDateFormat("yyyy-MM-dd", Locale.US)
    }
    private val timeFormatThreadLocal = ThreadLocal.withInitial {
        SimpleDateFormat("HH:mm", Locale.US)
    }

    private val isoFormat: SimpleDateFormat get() = isoFormatThreadLocal.get() ?: SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val timeFormat: SimpleDateFormat get() = timeFormatThreadLocal.get() ?: SimpleDateFormat("HH:mm", Locale.US)

    @Synchronized
    fun getTodayString(): String {
        return isoFormat.format(Date())
    }

    @Synchronized
    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    @Synchronized
    fun parseDate(dateStr: String): Date? {
        return try {
            isoFormat.parse(dateStr)
        } catch (_: Exception) {
            null
        }
    }

    @Synchronized
    fun getAzerbaijaniDateFormatted(dateStr: String): String {
        val date = parseDate(dateStr) ?: Date()
        val cal = Calendar.getInstance().apply { time = date }
        
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val monthIdx = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)
        val dayOfWeekIdx = cal.get(Calendar.DAY_OF_WEEK)

        val months = arrayOf(
            "Yanvar", "Fevral", "Mart", "Aprel", "May", "İyun",
            "İyul", "Avqust", "Sentyabr", "Oktyabr", "Noyabr", "Dekabr"
        )
        val days = arrayOf(
            "Bazar", "Bazar ertəsi", "Çərşənbə axşamı", "Çərşənbə",
            "Cümə axşamı", "Cümə", "Şənbə"
        )

        val monthName = if (monthIdx in months.indices) months[monthIdx] else ""
        val dayName = if (dayOfWeekIdx in 1..7) days[dayOfWeekIdx - 1] else ""

        return "$day $monthName $year, $dayName"
    }

    @Synchronized
    fun getShortAzerbaijaniDay(dateStr: String): String {
        val date = parseDate(dateStr) ?: return ""
        val cal = Calendar.getInstance().apply { time = date }
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "B.e"
            Calendar.TUESDAY -> "Ç.a"
            Calendar.WEDNESDAY -> "Çər"
            Calendar.THURSDAY -> "C.a"
            Calendar.FRIDAY -> "Cüm"
            Calendar.SATURDAY -> "Şən"
            Calendar.SUNDAY -> "Baz"
            else -> ""
        }
    }

    @Synchronized
    fun getDayNumber(dateStr: String): Int {
        val date = parseDate(dateStr) ?: return 1
        val cal = Calendar.getInstance().apply { time = date }
        return cal.get(Calendar.DAY_OF_MONTH)
    }

    /**
     * Returns a list of past N days up to today (inclusive), oldest to newest.
     */
    @Synchronized
    fun getPastDays(count: Int): List<String> {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -(count - 1))
        for (i in 0 until count) {
            list.add(isoFormat.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }

    /**
     * Calculate previous day date string
     */
    @Synchronized
    fun getPreviousDate(dateStr: String): String {
        val date = parseDate(dateStr) ?: Date()
        val cal = Calendar.getInstance().apply {
            time = date
            add(Calendar.DAY_OF_YEAR, -1)
        }
        return isoFormat.format(cal.time)
    }
}
