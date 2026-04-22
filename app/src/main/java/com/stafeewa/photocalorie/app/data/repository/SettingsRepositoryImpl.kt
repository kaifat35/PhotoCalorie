package com.stafeewa.photocalorie.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.stafeewa.photocalorie.app.data.mapper.toInterval
import com.stafeewa.photocalorie.app.domain.entity.Language
import com.stafeewa.photocalorie.app.domain.entity.Settings
import com.stafeewa.photocalorie.app.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")


class SettingsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : SettingsRepository {

    private val languageKey = stringPreferencesKey("language")
    private val intervalKey = intPreferencesKey("interval")
    private val notificationsEnabledKey = booleanPreferencesKey("notifications_enabled")
    private val wifiOnlyKey = booleanPreferencesKey("wifi_only")
    private val trainingFrequencyHoursKey = intPreferencesKey("training_frequency_hours")
    private val minTrainingExamplesKey = intPreferencesKey("min_training_examples")

    override fun getSettings(): Flow<Settings> {
        return context.dataStore.data.map { preferences ->
            val languageAsString = preferences[languageKey] ?: Settings.DEFAULT_LANGUAGE.name
            val language = Language.valueOf(languageAsString)
            val interval = preferences[intervalKey]?.toInterval() ?: Settings.DEFAULT_INTERVAL
            val notificationsEnabled =
                preferences[notificationsEnabledKey] ?: Settings.DEFAULT_NOTIFICATION_ENABLED
            val wifiOnly = preferences[wifiOnlyKey] ?: Settings.DEFAULT_WIFI_ONLY
            val trainingFrequencyHours = preferences[trainingFrequencyHoursKey]
                ?: Settings.DEFAULT_TRAINING_FREQUENCY_HOURS
            val minTrainingExamples = preferences[minTrainingExamplesKey]
                ?: Settings.DEFAULT_MIN_TRAINING_EXAMPLES

            Settings(
                language = language,
                interval = interval,
                notificationsEnabled = notificationsEnabled,
                wifiOnly = wifiOnly,
                trainingFrequencyHours = trainingFrequencyHours,
                minTrainingExamples = minTrainingExamples
            )
        }
    }

    override suspend fun updateLanguage(language: Language) {
        context.dataStore.edit { preferences ->
            preferences[languageKey] = language.name
        }
    }

    override suspend fun updateInterval(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[intervalKey] = minutes
        }
    }

    override suspend fun updateNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[notificationsEnabledKey] = enabled
        }
    }

    override suspend fun updateWifiOnly(wifiOnly: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[wifiOnlyKey] = wifiOnly
        }
    }

    override suspend fun updateTrainingFrequencyHours(hours: Int) {
        context.dataStore.edit { preferences ->
            preferences[trainingFrequencyHoursKey] = hours
        }
    }

    override suspend fun updateMinTrainingExamples(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[minTrainingExamplesKey] = count
        }
    }
}