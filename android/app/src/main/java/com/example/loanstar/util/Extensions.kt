package com.example.loanstar.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale

/**
 * Extension functions for common operations
 */

// Date formatting extensions
fun Long.toDateString(pattern: String = "MMM dd, yyyy"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toDateTimeString(pattern: String = "MMM dd, yyyy hh:mm a"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toTimeString(pattern: String = "hh:mm a"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(this))
}

// Currency formatting extensions
fun Double.toCurrencyString(currencyCode: String = "PHP"): String {
    val format = NumberFormat.getCurrencyInstance()
    format.currency = Currency.getInstance(currencyCode)
    return format.format(this)
}

fun Double.toPercentageString(decimals: Int = 2): String {
    return String.format(Locale.getDefault(), "%.${decimals}f%%", this)
}

// Number formatting
fun Double.formatDecimal(decimals: Int = 2): String {
    return String.format(Locale.getDefault(), "%.${decimals}f", this)
}

fun Int.formatWithCommas(): String {
    return NumberFormat.getNumberInstance(Locale.getDefault()).format(this)
}

// Validation extensions
fun String.isValidEmail(): Boolean {
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    return matches(emailPattern.toRegex())
}

fun String.isValidPhone(): Boolean {
    // Philippine phone number format (with or without country code)
    val phonePattern = "^(\\+?63|0)?[0-9]{10}$"
    return matches(phonePattern.toRegex())
}

// String utilities
fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault())
            else it.toString()
        }
    }
}

// Calculation helpers
fun Double.roundTo(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return kotlin.math.round(this * multiplier) / multiplier
}
