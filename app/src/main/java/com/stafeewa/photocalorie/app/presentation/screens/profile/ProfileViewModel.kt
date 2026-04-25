package com.stafeewa.photocalorie.app.presentation.screens.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stafeewa.photocalorie.app.domain.entity.ActivityLevel
import com.stafeewa.photocalorie.app.domain.usecase.userprofile.CalculateDailyCaloriesUseCase
import com.stafeewa.photocalorie.app.domain.usecase.userprofile.DeleteUserUseCase
import com.stafeewa.photocalorie.app.domain.usecase.userprofile.ObserveUserProfileUseCase
import com.stafeewa.photocalorie.app.domain.usecase.userprofile.UpdateAgeUseCase
import com.stafeewa.photocalorie.app.domain.usecase.userprofile.UpdateDailyCaloriesUseCase
import com.stafeewa.photocalorie.app.domain.usecase.userprofile.UpdateGenderUseCase
import com.stafeewa.photocalorie.app.domain.usecase.userprofile.UpdateHeightUseCase
import com.stafeewa.photocalorie.app.domain.usecase.userprofile.UpdateImageUseCase
import com.stafeewa.photocalorie.app.domain.usecase.userprofile.UpdateLoginUseCase
import com.stafeewa.photocalorie.app.domain.usecase.userprofile.UpdateMailUseCase
import com.stafeewa.photocalorie.app.domain.usecase.userprofile.UpdatePasswordUseCase
import com.stafeewa.photocalorie.app.domain.usecase.userprofile.UpdateUserProfileUseCase
import com.stafeewa.photocalorie.app.domain.usecase.userprofile.UpdateWeightUseCase
import com.stafeewa.photocalorie.app.utils.FileHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val observeUserProfileUseCase: ObserveUserProfileUseCase,
    private val calculateDailyCaloriesUseCase: CalculateDailyCaloriesUseCase,
    private val updateDailyCaloriesUseCase: UpdateDailyCaloriesUseCase,
    private val updateGenderUseCase: UpdateGenderUseCase,
    private val updateImageUseCase: UpdateImageUseCase,
    private val updateLoginUseCase: UpdateLoginUseCase,
    private val updateHeightUseCase: UpdateHeightUseCase,
    private val updateAgeUseCase: UpdateAgeUseCase,
    private val updateMailUseCase: UpdateMailUseCase,
    private val updatePasswordUseCase: UpdatePasswordUseCase,
    private val updateWeightUseCase: UpdateWeightUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _stateProfile = MutableStateFlow<ProfileState>(ProfileState.Initial)
    val stateProfile: StateFlow<ProfileState> = _stateProfile.asStateFlow()

    // Храним исходные данные из БД (не редактируем напрямую)
    private val _originalProfile = MutableStateFlow(EditableProfile())

    // Храним редактируемые данные (локальные изменения)
    private val _editableProfile = MutableStateFlow(EditableProfile())
    val editableProfile: StateFlow<EditableProfile> = _editableProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val fileHelper = FileHelper(context)

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true

            observeUserProfileUseCase()
                .collect { profile ->
                    // Сохраняем исходные данные
                    val loadedProfile = EditableProfile(
                        userId = profile.userId,
                        login = profile.login,
                        email = profile.email,
                        password = profile.password,
                        gender = profile.gender,
                        heightStr = profile.height?.toString() ?: "",
                        weightStr = profile.weight?.toString() ?: "",
                        ageStr = profile.age?.toString() ?: "",
                        imageUri = profile.imageUri,
                        dailyCalories = profile.dailyCalories
                    )
                    _originalProfile.value = loadedProfile

                    // Обновляем редактируемый профиль ТОЛЬКО если он не был изменён пользователем
                    if (!_editableProfile.value.isUserEdited) {
                        _editableProfile.value = loadedProfile
                    }

                    _stateProfile.update {
                        ProfileState.Configuration(
                            userId = profile.userId,
                            login = profile.login,
                            email = profile.email,
                            password = profile.password,
                            gender = profile.gender,
                            height = profile.height,
                            weight = profile.weight,
                            age = profile.age,
                            imageUri = profile.imageUri,
                            dailyCalories = profile.dailyCalories
                        )
                    }
                    _isLoading.value = false
                }
        }
    }

    fun processCommand(command: ProfileCommand) {
        when (command) {
            is ProfileCommand.UpdateLogin -> {
                // Обновляем только локальное состояние, не трогаем БД
                _editableProfile.update {
                    it.copy(
                        login = command.login,
                        isUserEdited = true
                    )
                }
            }

            is ProfileCommand.UpdateMail -> {
                _editableProfile.update {
                    it.copy(
                        email = command.email,
                        isUserEdited = true
                    )
                }
            }

            is ProfileCommand.UpdateGender -> {
                _editableProfile.update {
                    it.copy(
                        gender = command.gender,
                        isUserEdited = true
                    )
                }
                // Обновляем БД сразу для пола (так как это не вызывает проблем с курсором)
                viewModelScope.launch {
                    try {
                        updateGenderUseCase(command.gender)
                        _stateProfile.value = ProfileState.Success("Пол обновлен")
                    } catch (e: Exception) {
                        _stateProfile.value = ProfileState.Error(e.message ?: "Ошибка обновления пола")
                    }
                }
            }

            is ProfileCommand.UpdateImage -> {
                viewModelScope.launch {
                    try {
                        val localImagePath = if (command.imageUri != null) {
                            val uri = Uri.parse(command.imageUri)
                            fileHelper.copyImageToInternalStorage(uri)
                        } else {
                            null
                        }
                        val oldImagePath = _editableProfile.value.imageUri
                        if (oldImagePath != localImagePath) {
                            fileHelper.deleteOldProfileImage(oldImagePath)
                        }
                        updateImageUseCase(localImagePath)
                        _editableProfile.update {
                            it.copy(
                                imageUri = localImagePath,
                                isUserEdited = true
                            )
                        }
                        _stateProfile.value = ProfileState.Success("Изображение обновлено")
                    } catch (e: Exception) {
                        _stateProfile.value = ProfileState.Error(e.message ?: "Ошибка обновления изображения")
                    }
                }
            }

            is ProfileCommand.Calculate -> {
                viewModelScope.launch {
                    try {
                        val profile = _editableProfile.value
                        val height = profile.heightStr.toDoubleOrNull() ?: 0.0
                        val weight = profile.weightStr.toDoubleOrNull() ?: 0.0
                        val age = profile.ageStr.toIntOrNull() ?: 0

                        val calories = calculateDailyCaloriesUseCase(
                            gender = command.gender,
                            height = height,
                            weight = weight,
                            age = age,
                            activityLevel = command.activityLevel
                        )
                        _stateProfile.value = ProfileState.Success("Норма калорий рассчитана: ${calories.toInt()}")
                    } catch (e: Exception) {
                        _stateProfile.value = ProfileState.Error(e.message ?: "Ошибка расчета нормы калорий")
                    }
                }
            }

            ProfileCommand.SaveProfile -> {
                viewModelScope.launch {
                    try {
                        val profile = _editableProfile.value
                        val height = profile.heightStr.toDoubleOrNull()
                        val weight = profile.weightStr.toDoubleOrNull()
                        val age = profile.ageStr.toIntOrNull()

                        updateUserProfileUseCase(
                            login = profile.login,
                            email = profile.email,
                            password = profile.password,
                            gender = profile.gender,
                            height = height,
                            weight = weight,
                            age = age,
                            imageUri = profile.imageUri,
                            dailyCalories = profile.dailyCalories
                        )
                        // После сохранения сбрасываем флаг редактирования
                        _editableProfile.update { it.copy(isUserEdited = false) }
                        _stateProfile.value = ProfileState.Success("Профиль сохранен")
                    } catch (e: Exception) {
                        _stateProfile.value = ProfileState.Error(e.message ?: "Ошибка сохранения профиля")
                    }
                }
            }

            ProfileCommand.DeleteProfile -> {
                viewModelScope.launch {
                    try {
                        val userId = _editableProfile.value.userId
                        deleteUserUseCase(userId)
                        _stateProfile.value = ProfileState.Initial
                        _editableProfile.value = EditableProfile()
                        _stateProfile.value = ProfileState.Success("Профиль удален")
                    } catch (e: Exception) {
                        _stateProfile.value = ProfileState.Error(e.message ?: "Ошибка удаления профиля")
                    }
                }
            }

            is ProfileCommand.UpdateHeightStr -> {
                _editableProfile.update {
                    it.copy(
                        heightStr = command.heightStr,
                        isUserEdited = true
                    )
                }
            }

            is ProfileCommand.UpdateWeightStr -> {
                _editableProfile.update {
                    it.copy(
                        weightStr = command.weightStr,
                        isUserEdited = true
                    )
                }
            }

            is ProfileCommand.UpdateAgeStr -> {
                _editableProfile.update {
                    it.copy(
                        ageStr = command.ageStr,
                        isUserEdited = true
                    )
                }
            }

            is ProfileCommand.UpdatePassword -> {
                viewModelScope.launch {
                    try {
                        updatePasswordUseCase(command.password)
                        _stateProfile.value = ProfileState.Success("Пароль обновлен")
                    } catch (e: Exception) {
                        _stateProfile.value = ProfileState.Error(e.message ?: "Ошибка обновления пароля")
                    }
                }
            }

            else -> {
                // Остальные команды (UpdateAge, UpdateDailyCalories, UpdateHeight, UpdateWeight)
                // не используются напрямую, так как мы работаем через Str версии
            }
        }
    }
}

// Обновлённый EditableProfile с флагом редактирования
data class EditableProfile(
    val userId: Int? = null,
    val login: String = "",
    val email: String = "",
    val password: String = "",
    val gender: String? = null,
    val heightStr: String = "",
    val weightStr: String = "",
    val ageStr: String = "",
    val imageUri: String? = null,
    val dailyCalories: Double? = null,
    val isUserEdited: Boolean = false  // ← флаг, что пользователь редактирует
) {
    fun getHeight(): Double? = heightStr.toDoubleOrNull()
    fun getWeight(): Double? = weightStr.toDoubleOrNull()
    fun getAge(): Int? = ageStr.toIntOrNull()
}

// Обновлённые команды (убираем ненужные)
sealed interface ProfileCommand {
    data class UpdateLogin(val login: String) : ProfileCommand
    data class UpdateMail(val email: String) : ProfileCommand
    data class UpdateGender(val gender: String?) : ProfileCommand
    data class UpdateImage(val imageUri: String?) : ProfileCommand
    data class UpdatePassword(val password: String) : ProfileCommand

    data class UpdateHeightStr(val heightStr: String) : ProfileCommand
    data class UpdateWeightStr(val weightStr: String) : ProfileCommand
    data class UpdateAgeStr(val ageStr: String) : ProfileCommand

    data class Calculate(
        val gender: String,
        val height: Double,
        val weight: Double,
        val age: Int,
        val activityLevel: ActivityLevel = ActivityLevel.MODERATE
    ) : ProfileCommand

    data object SaveProfile : ProfileCommand
    data object DeleteProfile : ProfileCommand
}

sealed interface ProfileState {
    data object Initial : ProfileState
    data class Success(val message: String) : ProfileState
    data class Error(val message: String) : ProfileState
    data class Configuration(
        val userId: Int?,
        val login: String,
        val email: String,
        val password: String,
        val gender: String?,
        val height: Double?,
        val weight: Double?,
        val age: Int?,
        val imageUri: String?,
        val dailyCalories: Double?
    ) : ProfileState
}
