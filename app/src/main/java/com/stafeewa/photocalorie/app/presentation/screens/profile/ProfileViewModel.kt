package com.stafeewa.photocalorie.app.presentation.screens.profile

import android.content.Context
import android.net.Uri
import android.util.Log
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
            Log.d("ProfileViewModel", "loadUserProfile: START, setting isLoading=true")
            _isLoading.value = true

            observeUserProfileUseCase()
                .collect { profile ->
                    Log.d("ProfileViewModel", "loadUserProfile: Profile received: userId=${profile.userId}, login=${profile.login}, dailyCalories=${profile.dailyCalories}")

                    _editableProfile.update {
                        EditableProfile(
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
                    Log.d("ProfileViewModel", "loadUserProfile: Profile updated, isLoading=false")
                }
        }
    }

    fun processCommand(command: ProfileCommand) {
        when (command) {
            is ProfileCommand.UpdateAge -> {
                viewModelScope.launch {
                    try {
                        updateAgeUseCase(command.age)
                        _stateProfile.value = ProfileState.Success("Возраст обновлен")
                    } catch (e: Exception) {
                        _stateProfile.value = ProfileState.Error(e.message ?: "Ошибка обновления возраста")
                    }
                }
            }

            is ProfileCommand.UpdateDailyCalories -> {
                viewModelScope.launch {
                    try {
                        updateDailyCaloriesUseCase(command.dailyCalories)
                        _stateProfile.value = ProfileState.Success("Дневная норма калорий обновлена")
                    } catch (e: Exception) {
                        _stateProfile.value = ProfileState.Error(e.message ?: "Ошибка обновления нормы калорий")
                    }
                }
            }

            is ProfileCommand.UpdateGender -> {
                viewModelScope.launch {
                    try {
                        updateGenderUseCase(command.gender)
                        _stateProfile.value = ProfileState.Success("Пол обновлен")
                    } catch (e: Exception) {
                        _stateProfile.value = ProfileState.Error(e.message ?: "Ошибка обновления пола")
                    }
                }
            }

            is ProfileCommand.UpdateHeight -> {
                viewModelScope.launch {
                    try {
                        updateHeightUseCase(command.height)
                        _stateProfile.value = ProfileState.Success("Рост обновлен")
                    } catch (e: Exception) {
                        _stateProfile.value = ProfileState.Error(e.message ?: "Ошибка обновления роста")
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
                        _stateProfile.value = ProfileState.Success("Изображение обновлено")
                    } catch (e: Exception) {
                        Log.e("ProfileViewModel", "Error updating image", e)
                        _stateProfile.value = ProfileState.Error(e.message ?: "Ошибка обновления изображения")
                    }
                }
            }

            is ProfileCommand.UpdateLogin -> {
                viewModelScope.launch {
                    try {
                        updateLoginUseCase(command.login)
                        _stateProfile.value = ProfileState.Success("Логин обновлен")
                    } catch (e: Exception) {
                        _stateProfile.value = ProfileState.Error(e.message ?: "Ошибка обновления логина")
                    }
                }
            }

            is ProfileCommand.UpdateMail -> {
                viewModelScope.launch {
                    try {
                        updateMailUseCase(command.email)
                        _stateProfile.value = ProfileState.Success("Почта обновлена")
                    } catch (e: Exception) {
                        _stateProfile.value = ProfileState.Error(e.message ?: "Ошибка обновления почты")
                    }
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

            is ProfileCommand.UpdateWeight -> {
                viewModelScope.launch {
                    try {
                        updateWeightUseCase(command.weight)
                        _stateProfile.value = ProfileState.Success("Вес обновлен")
                    } catch (e: Exception) {
                        _stateProfile.value = ProfileState.Error(e.message ?: "Ошибка обновления веса")
                    }
                }
            }

            is ProfileCommand.Calculate -> {
                viewModelScope.launch {
                    try {
                        val calories = calculateDailyCaloriesUseCase(
                            gender = command.gender,
                            height = command.height,
                            weight = command.weight,
                            age = command.age,
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
                        updateUserProfileUseCase(
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

            is ProfileCommand.UpdateEditableProfile -> {
                _editableProfile.value = command.updates(_editableProfile.value)
            }
        }
    }
}

// Остальные классы остаются без изменений
sealed interface ProfileCommand {
    data class UpdateAge(val age: Int) : ProfileCommand
    data class UpdateDailyCalories(val dailyCalories: Double?) : ProfileCommand
    data class UpdateGender(val gender: String?) : ProfileCommand
    data class UpdateHeight(val height: Double?) : ProfileCommand
    data class UpdateImage(val imageUri: String?) : ProfileCommand
    data class UpdateLogin(val login: String) : ProfileCommand
    data class UpdateMail(val email: String) : ProfileCommand
    data class UpdatePassword(val password: String) : ProfileCommand
    data class UpdateWeight(val weight: Double?) : ProfileCommand
    data class UpdateEditableProfile(val updates: EditableProfile.() -> EditableProfile) : ProfileCommand
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

data class EditableProfile(
    val userId: Int? = null,
    val login: String = "",
    val email: String = "",
    val password: String = "",
    val gender: String? = null,
    val height: Double? = null,
    val weight: Double? = null,
    val age: Int? = null,
    val imageUri: String? = null,
    val dailyCalories: Double? = null
)

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