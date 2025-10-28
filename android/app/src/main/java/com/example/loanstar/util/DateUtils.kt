package com.example.loanstar.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Date utility functions for the Loanstar application
 */
object DateUtils {

    /**
     * Get start of day in milliseconds
     */
    fun getStartOfDay(timeInMillis: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            this.timeInMillis = timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    /**
     * Get end of day in milliseconds
     */
    fun getEndOfDay(timeInMillis: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            this.timeInMillis = timeInMillis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }

    /**
     * Get start of month in milliseconds
     */
    fun getStartOfMonth(timeInMillis: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            this.timeInMillis = timeInMillis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    /**
     * Get end of month in milliseconds
     */
    fun getEndOfMonth(timeInMillis: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            this.timeInMillis = timeInMillis
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }

    /**
     * Add days to a date
     */
    fun addDays(timeInMillis: Long, days: Int): Long {
        val calendar = Calendar.getInstance().apply {
            this.timeInMillis = timeInMillis
            add(Calendar.DAY_OF_MONTH, days)
        }
        return calendar.timeInMillis
    }

    /**
     * Add months to a date
     */
    fun addMonths(timeInMillis: Long, months: Int): Long {
        val calendar = Calendar.getInstance().apply {
            this.timeInMillis = timeInMillis
            add(Calendar.MONTH, months)
        }
        return calendar.timeInMillis
    }

    /**
     * Calculate difference in days between two dates
     */
    fun daysBetween(startMillis: Long, endMillis: Long): Int {
        val diffMillis = endMillis - startMillis
        return (diffMillis / (1000 * 60 * 60 * 24)).toInt()
    }

    /**
     * Calculate difference in months between two dates
     */
    fun monthsBetween(startMillis: Long, endMillis: Long): Int {
        val startCalendar = Calendar.getInstance().apply { timeInMillis = startMillis }
        val endCalendar = Calendar.getInstance().apply { timeInMillis = endMillis }

        val yearsDiff = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR)
        val monthsDiff = endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH)

        return yearsDiff * 12 + monthsDiff
    }

    /**
     * Check if date is in the past
     */
    fun isPast(timeInMillis: Long): Boolean {
        return timeInMillis < System.currentTimeMillis()
    }

    /**
     * Check if date is in the future
     */
    fun isFuture(timeInMillis: Long): Boolean {
        return timeInMillis > System.currentTimeMillis()
    }

    /**
     * Check if date is today
     */
    fun isToday(timeInMillis: Long): Boolean {
        val today = getStartOfDay()
        val tomorrow = addDays(today, 1)
        return timeInMillis >= today && timeInMillis < tomorrow
    }

    /**
     * Check if date is within next N days
     */
    fun isWithinNextDays(timeInMillis: Long, days: Int): Boolean {
        val now = System.currentTimeMillis()
        val future = addDays(now, days)
        return timeInMillis > now && timeInMillis <= future
    }

    /**
     * Get days until a date (positive if future, negative if past)
     */
    fun daysUntil(timeInMillis: Long): Int {
        return daysBetween(System.currentTimeMillis(), timeInMillis)
    }

    /**
     * Format relative time (e.g., "2 days ago", "in 3 days")
     */
    fun formatRelativeTime(timeInMillis: Long): String {
        val now = System.currentTimeMillis()
        val diffMillis = timeInMillis - now
        val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

        return when {
            diffDays == 0 -> "Today"
            diffDays == 1 -> "Tomorrow"
            diffDays == -1 -> "Yesterday"
            diffDays > 1 -> "In $diffDays days"
            diffDays < -1 -> "${-diffDays} days ago"
            else -> timeInMillis.toDateString()
        }
    }

    /**
     * Parse date string to milliseconds
     */
    fun parseDate(dateString: String, pattern: String = "MMM dd, yyyy"): Long? {
        return try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            sdf.parse(dateString)?.time
        } catch (e: Exception) {
            android.util.Log.e("loanstar", "Error parsing date string '$dateString' with pattern '$pattern': ${e.message}", e)
            null
        }
    }

    /**
     * Get current timestamp in milliseconds
     */
    fun now(): Long = System.currentTimeMillis()

    /**
     * Get date range for filtering (start and end of day)
     */
    fun getDateRange(startMillis: Long, endMillis: Long): Pair<Long, Long> {
        return Pair(getStartOfDay(startMillis), getEndOfDay(endMillis))
    }

    /**
     * Get formatted date range string
     */
    fun formatDateRange(startMillis: Long, endMillis: Long): String {
        val start = startMillis.toDateString()
        val end = endMillis.toDateString()
        return "$start - $end"
    }

    /**
     * Check if two dates are on the same day
     */
    fun isSameDay(timeMillis1: Long, timeMillis2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = timeMillis1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = timeMillis2 }

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Get week start (Monday)
     */
    fun getStartOfWeek(timeInMillis: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            this.timeInMillis = timeInMillis
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    /**
     * Get week end (Sunday)
     */
    fun getEndOfWeek(timeInMillis: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            this.timeInMillis = timeInMillis
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }
}
