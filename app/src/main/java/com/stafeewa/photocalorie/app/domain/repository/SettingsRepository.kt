package com.stafeewa.photocalorie.app.domain.repository

import com.stafeewa.photocalorie.app.domain.entity.Language
import com.stafeewa.photocalorie.app.domain.entity.Settings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    fun getSettings(): Flow<Settings>

    suspend fun updateLanguage(language: Language)

    suspend fun updateInterval(minutes: Int)

    suspend fun updateNotificationEnabled(enabled: Boolean)

    suspend fun updateWifiOnly(wifiOnly: Boolean)

    suspend fun updateTrainingFrequencyHours(hours: Int)

    suspend fun updateMinTrainingExamples(count: Int)
}