package com.example.loanstar.data.local

import androidx.room.TypeConverter
import com.example.loanstar.data.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * Type converters for Room database
 * Handles conversion of complex types to/from database-storable formats
 */
class Converters {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    // ProfileDetails converters
    @TypeConverter
    fun fromProfileDetails(value: ProfileDetails?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toProfileDetails(value: String?): ProfileDetails? {
        return value?.let { json.decodeFromString<ProfileDetails>(it) }
    }

    // ContactInfo converters
    @TypeConverter
    fun fromContactInfo(value: ContactInfo?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toContactInfo(value: String?): ContactInfo? {
        return value?.let { json.decodeFromString<ContactInfo>(it) }
    }

    // AmortizationEntry list converters
    @TypeConverter
    fun fromAmortizationEntryList(value: List<AmortizationEntry>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toAmortizationEntryList(value: String?): List<AmortizationEntry>? {
        return value?.let { json.decodeFromString<List<AmortizationEntry>>(it) }
    }

    // EarningEntry list converters
    @TypeConverter
    fun fromEarningEntryList(value: List<EarningEntry>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toEarningEntryList(value: String?): List<EarningEntry>? {
        return value?.let { json.decodeFromString<List<EarningEntry>>(it) }
    }

    // String list converters (for generic lists)
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.split(",")?.map { it.trim() }
    }

    // Map converters (for generic JSON objects)
    @TypeConverter
    fun fromMap(value: Map<String, Any>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toMap(value: String?): Map<String, Any>? {
        return value?.let { json.decodeFromString<Map<String, Any>>(it) }
    }
}
