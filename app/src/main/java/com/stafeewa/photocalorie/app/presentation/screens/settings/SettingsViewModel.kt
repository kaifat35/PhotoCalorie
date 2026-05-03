package com.stafeewa.photocalorie.app.presentation.screens.settings

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stafeewa.photocalorie.app.PhotoCalorieApp
import com.stafeewa.photocalorie.app.domain.entity.Interval
import com.stafeewa.photocalorie.app.domain.entity.Language
import com.stafeewa.photocalorie.app.domain.entity.MinTrainingExamplesOption
import com.stafeewa.photocalorie.app.domain.entity.ThemeMode
import com.stafeewa.photocalorie.app.domain.entity.TrainingFrequencyOption
import com.stafeewa.photocalorie.app.domain.usecase.settings.GetSettingsUseCase
import com.stafeewa.photocalorie.app.domain.usecase.settings.UpdateIntervalUseCase
import com.stafeewa.photocalorie.app.domain.usecase.settings.UpdateLanguageUseCase
import com.stafeewa.photocalorie.app.domain.usecase.settings.UpdateMinTrainingExamplesUseCase
import com.stafeewa.photocalorie.app.domain.usecase.settings.UpdateNotificationsEnabledUseCase
import com.stafeewa.photocalorie.app.domain.usecase.settings.UpdateTrainingFrequencyUseCase
import com.stafeewa.photocalorie.app.domain.usecase.settings.UpdateWifiOnlyUseCase
import com.stafeewa.photocalorie.app.presentation.workers.TrainingScheduleConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
    getSettingsUseCase: GetSettingsUseCase,
    private val updateIntervalUseCase: UpdateIntervalUseCase,
    private val updateLanguageUseCase: UpdateLanguageUseCase,
    private val updateNotificationsEnabledUseCase: UpdateNotificationsEnabledUseCase,
    private val updateWifiOnlyUseCase: UpdateWifiOnlyUseCase,
    private val updateTrainingFrequencyUseCase: UpdateTrainingFrequencyUseCase,
    private val updateMinTrainingExamplesUseCase: UpdateMinTrainingExamplesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<SettingsState>(SettingsState.Initial)
    val state = _state.asStateFlow()

    // Событие для перезапуска Activity (после смены языка/темы)
    private val _restartAppEvent = MutableSharedFlow<Unit>()
    val restartAppEvent = _restartAppEvent.asSharedFlow()

    init {
        getSettingsUseCase()
            .onEach { settings ->
                val savedTheme = getSavedThemeMode()
                _state.update {
                    SettingsState.Configuration(
                        language = settings.language,
                        interval = settings.interval,
                        wifiOnly = settings.wifiOnly,
                        notificationsEnabled = settings.notificationsEnabled,
                        trainingFrequencyHours = settings.trainingFrequencyHours,
                        minTrainingExamples = settings.minTrainingExamples,
                        themeMode = savedTheme
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun getSavedThemeMode(): ThemeMode {
        val prefs = application.getSharedPreferences("app_settings", Application.MODE_PRIVATE)
        val themeName = prefs.getString("theme_mode", ThemeMode.DEFAULT.name) ?: ThemeMode.DEFAULT.name
        return try {
            ThemeMode.valueOf(themeName)
        } catch (e: IllegalArgumentException) {
            ThemeMode.DEFAULT
        }
    }

    fun processCommand(command: SettingsCommand) {
        viewModelScope.launch {
            when (command) {
                is SettingsCommand.SeWifiOnly -> {
                    updateWifiOnlyUseCase(command.wifiOnly)
                }
                is SettingsCommand.SelectInterval -> {
                    updateIntervalUseCase(command.interval)
                }
                is SettingsCommand.SelectLanguage -> {
                    updateLanguageUseCase(command.language)
                    saveAndApplyLanguage(command.language)
                    updateAppLocale(command.language.code)    // обновляем ресурсы приложения
                    _restartAppEvent.emit(Unit)               // перезапускаем Activity
                }
                is SettingsCommand.SetNotificationEnabled -> {
                    updateNotificationsEnabledUseCase(command.enabled)
                }
                is SettingsCommand.SetThemeMode -> {
                    updateThemeModeUseCase(command.themeMode)
                }
                is SettingsCommand.SetTrainingFrequencyHours -> {
                    updateTrainingFrequencyUseCase(
                        TrainingScheduleConfig.normalizeFrequencyHours(command.hours)
                    )
                }
                is SettingsCommand.SetMinTrainingExamples -> {
                    updateMinTrainingExamplesUseCase(
                        TrainingScheduleConfig.normalizeMinExamples(command.count)
                    )
                }
            }
        }
    }

    private fun saveAndApplyLanguage(language: Language) {
        val prefs = application.getSharedPreferences("app_settings", Application.MODE_PRIVATE)
        prefs.edit { putString("language", language.code) }
    }

    private fun updateAppLocale(languageCode: String) {
        (application as? PhotoCalorieApp)?.updateLocale(languageCode)
    }

    private val updateThemeModeUseCase: (ThemeMode) -> Unit = { themeMode ->
        val prefs = application.getSharedPreferences("app_settings", Application.MODE_PRIVATE)
        prefs.edit { putString("theme_mode", themeMode.name) }
        _state.update { currentState ->
            when (currentState) {
                is SettingsState.Configuration -> currentState.copy(themeMode = themeMode)
                SettingsState.Initial -> currentState
            }
        }
    }
}

sealed interface SettingsCommand {
    data class SelectLanguage(val language: Language) : SettingsCommand
    data class SelectInterval(val interval: Interval) : SettingsCommand
    data class SetNotificationEnabled(val enabled: Boolean) : SettingsCommand
    data class SeWifiOnly(val wifiOnly: Boolean) : SettingsCommand
    data class SetThemeMode(val themeMode: ThemeMode) : SettingsCommand
    data class SetTrainingFrequencyHours(val hours: Int) : SettingsCommand
    data class SetMinTrainingExamples(val count: Int) : SettingsCommand
}

sealed interface SettingsState {
    data object Initial : SettingsState
    data class Configuration(
        val language: Language,
        val interval: Interval,
        val wifiOnly: Boolean,
        val notificationsEnabled: Boolean,
        val trainingFrequencyHours: Int,
        val minTrainingExamples: Int,
        val themeMode: ThemeMode,
        val languages: List<Language> = Language.entries,
        val intervals: List<Interval> = Interval.entries,
        val themeModes: List<ThemeMode> = ThemeMode.entries,
        val trainingFrequencyOptions: List<TrainingFrequencyOption> = TrainingFrequencyOption.entries,
        val minTrainingExamplesOptions: List<MinTrainingExamplesOption> = MinTrainingExamplesOption.entries
    ) : SettingsState
}