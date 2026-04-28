package com.stafeewa.photocalorie.app.presentation.screens.profile

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stafeewa.photocalorie.app.R
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UiMessage {
    data class Resource(@StringRes val resId: Int, val args: Array<out Any> = emptyArray()) : UiMessage()
    data class Plain(val text: String) : UiMessage()
}

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

    private companion object {
        const val MAX_HEIGHT_CM = 300.0
        const val MAX_WEIGHT_KG = 300.0
        const val MAX_AGE_YEARS = 100
    }

    private val _stateProfile = MutableStateFlow<ProfileState>(ProfileState.Initial)
    val stateProfile: StateFlow<ProfileState> = _stateProfile.asStateFlow()

    private val _originalProfile = MutableStateFlow(EditableProfile())
    private val _editableProfile = MutableStateFlow(EditableProfile())
    val editableProfile: StateFlow<EditableProfile> = _editableProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiMessages = MutableSharedFlow<UiMessage>()
    val uiMessages: SharedFlow<UiMessage> = _uiMessages.asSharedFlow()

    private var lastConfig: ProfileState.Configuration? = null
    private val fileHelper = FileHelper(context)

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            observeUserProfileUseCase().collect { profile ->
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
                if (!_editableProfile.value.isUserEdited) {
                    _editableProfile.value = loadedProfile
                }
                val configState = ProfileState.Configuration(
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
                lastConfig = configState
                _stateProfile.update { configState }
                _isLoading.value = false
            }
        }
    }

    fun processCommand(command: ProfileCommand) {
        when (command) {
            is ProfileCommand.UpdateLogin -> {
                _editableProfile.update { it.copy(login = command.login, isUserEdited = true) }
            }
            is ProfileCommand.UpdateMail -> {
                _editableProfile.update { it.copy(email = command.email, isUserEdited = true) }
            }
            is ProfileCommand.UpdateGender -> {
                _editableProfile.update { it.copy(gender = command.gender, isUserEdited = true) }
                viewModelScope.launch {
                    try {
                        updateGenderUseCase(command.gender)
                        _stateProfile.value = ProfileState.Success(R.string.gender_updated)
                        _uiMessages.emit(UiMessage.Resource(R.string.gender_updated))
                    } catch (e: Exception) {
                        _stateProfile.value = ProfileState.Error(R.string.error_updating_gender)
                        _uiMessages.emit(UiMessage.Resource(R.string.error_updating_gender))
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
                        _editableProfile.update { it.copy(imageUri = localImagePath, isUserEdited = true) }
                        _stateProfile.value = ProfileState.Success(R.string.image_updated)
                        _uiMessages.emit(UiMessage.Resource(R.string.image_updated))
                    } catch (e: Exception) {
                        _stateProfile.value = ProfileState.Error(R.string.error_updating_image)
                        _uiMessages.emit(UiMessage.Resource(R.string.error_updating_image))
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
                        validatePhysicalParams(height, weight, age)?.let { errorResId ->
                            _stateProfile.value = ProfileState.Error(errorResId)
                            _uiMessages.emit(UiMessage.Resource(errorResId))
                            return@launch
                        }
                        val calories = calculateDailyCaloriesUseCase(
                            gender = command.gender,
                            height = height,
                            weight = weight,
                            age = age,
                            activityLevel = command.activityLevel
                        )
                        updateDailyCaloriesUseCase(calories)
                        _editableProfile.update { it.copy(dailyCalories = calories, isUserEdited = true) }
                        val caloriesInt = calories.toInt()
                        _stateProfile.value = ProfileState.Success(R.string.The_calorie_rate_is_calculated, arrayOf(caloriesInt))
                        _uiMessages.emit(UiMessage.Resource(R.string.The_calorie_rate_is_calculated, arrayOf(caloriesInt)))
                    } catch (e: Exception) {
                        _stateProfile.value = ProfileState.Error(R.string.error_calculating_calories)
                        _uiMessages.emit(UiMessage.Resource(R.string.error_calculating_calories))
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
                        validatePhysicalParams(height, weight, age)?.let { errorResId ->
                            _stateProfile.value = ProfileState.Error(errorResId)
                            _uiMessages.emit(UiMessage.Resource(errorResId))
                            return@launch
                        }
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
                        _editableProfile.update { it.copy(isUserEdited = false) }
                        _stateProfile.value = ProfileState.Success(R.string.save_profile)
                        _uiMessages.emit(UiMessage.Resource(R.string.save_profile))
                    } catch (e: Exception) {
                        _stateProfile.value = ProfileState.Error(R.string.error_saving_profile)
                        _uiMessages.emit(UiMessage.Resource(R.string.error_saving_profile))
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
                        _stateProfile.value = ProfileState.Success(R.string.profile_deleted)
                        _uiMessages.emit(UiMessage.Resource(R.string.profile_deleted))
                    } catch (e: Exception) {
                        _stateProfile.value = ProfileState.Error(R.string.error_deleting_profile)
                        _uiMessages.emit(UiMessage.Resource(R.string.error_deleting_profile))
                    }
                }
            }
            is ProfileCommand.UpdateHeightStr -> {
                _editableProfile.update { it.copy(heightStr = command.heightStr, isUserEdited = true) }
            }
            is ProfileCommand.UpdateWeightStr -> {
                _editableProfile.update { it.copy(weightStr = command.weightStr, isUserEdited = true) }
            }
            is ProfileCommand.UpdateAgeStr -> {
                _editableProfile.update { it.copy(ageStr = command.ageStr, isUserEdited = true) }
            }
            is ProfileCommand.UpdatePassword -> {
                viewModelScope.launch {
                    try {
                        updatePasswordUseCase(command.password)
                        _stateProfile.value = ProfileState.Success(R.string.password_updated)
                        _uiMessages.emit(UiMessage.Resource(R.string.password_updated))
                    } catch (e: Exception) {
                        _stateProfile.value = ProfileState.Error(R.string.error_updating_password)
                        _uiMessages.emit(UiMessage.Resource(R.string.error_updating_password))
                    }
                }
            }
            else -> {}
        }
    }

    @StringRes
    private fun validatePhysicalParams(height: Double?, weight: Double?, age: Int?): Int? {
        if (height != null && height > MAX_HEIGHT_CM) {
            return R.string.height_should_not_exceed_300_cm
        }
        if (weight != null && weight > MAX_WEIGHT_KG) {
            return R.string.The_weight_should_not_exceed_300_kg
        }
        if (age != null && age > MAX_AGE_YEARS) {
            return R.string.The_age_should_not_exceed_100_years
        }
        return null
    }
}


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
    val isUserEdited: Boolean = false
) {
    fun getHeight(): Double? = heightStr.toDoubleOrNull()
    fun getWeight(): Double? = weightStr.toDoubleOrNull()
    fun getAge(): Int? = ageStr.toIntOrNull()
}

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
    data class Success(@StringRes val messageResId: Int, val args: Array<out Any> = emptyArray()) : ProfileState
    data class Error(@StringRes val messageResId: Int, val args: Array<out Any> = emptyArray()) : ProfileState
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
