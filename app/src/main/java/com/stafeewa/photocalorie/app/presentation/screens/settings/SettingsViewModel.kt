package com.stafeewa.photocalorie.app.presentation.screens.settings

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stafeewa.photocalorie.app.domain.entity.Interval
import com.stafeewa.photocalorie.app.domain.entity.Language
import com.stafeewa.photocalorie.app.domain.usecase.settings.GetSettingsUseCase
import com.stafeewa.photocalorie.app.domain.usecase.settings.UpdateIntervalUseCase
import com.stafeewa.photocalorie.app.domain.usecase.settings.UpdateLanguageUseCase
import com.stafeewa.photocalorie.app.domain.usecase.settings.UpdateNotificationsEnabledUseCase
import com.stafeewa.photocalorie.app.domain.usecase.settings.UpdateWifiOnlyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val updateWifiOnlyUseCase: UpdateWifiOnlyUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<SettingsState>(SettingsState.Initial)
    val state = _state.asStateFlow()

    init {
        getSettingsUseCase()
            .onEach { settings ->
                _state.update {
                    SettingsState.Configuration(
                        language = settings.language,
                        interval = settings.interval,
                        wifiOnly = settings.wifiOnly,
                        notificationsEnabled = settings.notificationsEnabled
                    )
                }
            }
            .launchIn(viewModelScope)
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
                }

                is SettingsCommand.SetNotificationEnabled -> {
                    updateNotificationsEnabledUseCase(command.enabled)
                }
            }
        }
    }
    private fun saveAndApplyLanguage(language: Language) {
        val prefs = application.getSharedPreferences("app_settings", Application.MODE_PRIVATE)
        prefs.edit { putString("language", language.code) }
    }
}

sealed interface SettingsCommand {

    data class SelectLanguage(val language: Language) : SettingsCommand

    data class SelectInterval(val interval: Interval) : SettingsCommand

    data class SetNotificationEnabled(val enabled: Boolean) : SettingsCommand

    data class SeWifiOnly(val wifiOnly: Boolean) : SettingsCommand
}


sealed interface SettingsState {

    data object Initial : SettingsState

    data class Configuration(
        val language: Language,
        val interval: Interval,
        val wifiOnly: Boolean,
        val notificationsEnabled: Boolean,
        val languages: List<Language> = Language.entries,
        val intervals: List<Interval> = Interval.entries
    ) : SettingsState
}