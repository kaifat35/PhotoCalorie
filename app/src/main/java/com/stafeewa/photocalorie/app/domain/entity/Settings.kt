package com.stafeewa.photocalorie.app.domain.entity

import java.util.Locale

data class Settings(
    val language: Language,
    val interval: Interval,
    val notificationsEnabled: Boolean,
    val wifiOnly: Boolean,
    val trainingFrequencyHours: Int,
    val minTrainingExamples: Int
) {

    companion object {

        val DEFAULT_LANGUAGE = Language.ENGLISH
        val DEFAULT_INTERVAL = Interval.MIN_15
        const val DEFAULT_NOTIFICATION_ENABLED = false
        const val DEFAULT_WIFI_ONLY = true
        const val DEFAULT_TRAINING_FREQUENCY_HOURS = 24
        const val DEFAULT_MIN_TRAINING_EXAMPLES = 8
    }
}

enum class TrainingFrequencyOption(val hours: Int, val title: String) {
    DAILY(24, "24 ч"),
    EVERY_2_DAYS(48, "48 ч"),
    EVERY_3_DAYS(72, "72 ч"),
    EVERY_5_DAYS(120, "120 ч"),
    WEEKLY(168, "168 ч");

    companion object {
        fun fromHours(hours: Int): TrainingFrequencyOption {
            return entries.minByOrNull { kotlin.math.abs(it.hours - hours) } ?: DAILY
        }
    }
}

enum class MinTrainingExamplesOption(val count: Int, val title: String) {
    EXAMPLES_5(5, "5"),
    EXAMPLES_8(8, "8"),
    EXAMPLES_10(10, "10"),
    EXAMPLES_20(20, "20"),
    EXAMPLES_30(30, "30"),
    EXAMPLES_50(50, "50");

    companion object {
        fun fromCount(count: Int): MinTrainingExamplesOption {
            return entries.minByOrNull { kotlin.math.abs(it.count - count) } ?: EXAMPLES_8
        }
    }
}

enum class Language(val code: String) {
    ENGLISH("en"),
    RUSSIAN("ru");

    fun toReadableFormat(): String {
        return when (this) {
            ENGLISH -> "English"
            RUSSIAN -> "Русский"
        }
    }

    fun getLocale(): Locale = Locale(code)
}

enum class Interval(val minutes: Int) {
    MIN_15(15),
    MIN_30(30),
    HOUR_1(60),
    HOUR_2(120),
    HOUR_4(240),
    HOUR_8(480),
    HOUR_24(1440)
}