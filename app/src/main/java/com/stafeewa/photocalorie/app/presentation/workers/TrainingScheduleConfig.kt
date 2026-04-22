package com.stafeewa.photocalorie.app.presentation.workers

object TrainingScheduleConfig {

    private const val MIN_ALLOWED_HOURS = 24
    private const val MAX_ALLOWED_HOURS = 24 * 7
    private const val MIN_ALLOWED_EXAMPLES = 1
    private const val MAX_ALLOWED_EXAMPLES = 100

    fun normalizeFrequencyHours(value: Int): Int {
        return value.coerceIn(MIN_ALLOWED_HOURS, MAX_ALLOWED_HOURS)
    }

    fun normalizeMinExamples(value: Int): Int {
        return value.coerceIn(MIN_ALLOWED_EXAMPLES, MAX_ALLOWED_EXAMPLES)
    }
}