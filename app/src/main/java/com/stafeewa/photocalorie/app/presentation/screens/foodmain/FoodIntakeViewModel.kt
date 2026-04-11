package com.stafeewa.photocalorie.app.presentation.screens.foodmain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stafeewa.photocalorie.app.domain.entity.FoodEntry
import com.stafeewa.photocalorie.app.domain.entity.MealType
import com.stafeewa.photocalorie.app.domain.usecase.foodintake.*
import com.stafeewa.photocalorie.app.domain.usecase.userprofile.ObserveUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoodIntakeViewModel @Inject constructor(
    private val observeUserProfileUseCase: ObserveUserProfileUseCase,
    private val addFoodEntryUseCase: AddFoodEntryUseCase,
    private val addFoodEntryWithValidationUseCase: AddFoodEntryWithValidationUseCase,
    private val removeFoodEntryUseCase: RemoveFoodEntryUseCase,
    private val updateFoodEntryUseCase: UpdateFoodEntryUseCase,
    private val getTodayEntriesUseCase: GetTodayEntriesUseCase,
    private val getDailyIntakeUseCase: GetDailyIntakeUseCase
) : ViewModel() {

    private val _calorieGoal = MutableStateFlow(2000.0)
    val calorieGoal: StateFlow<Double> = _calorieGoal.asStateFlow()

    private val _totalCalories = MutableStateFlow(0.0)
    val totalCalories: StateFlow<Double> = _totalCalories.asStateFlow()

    private val _remainingCalories = MutableStateFlow(2000.0)
    val remainingCalories: StateFlow<Double> = _remainingCalories.asStateFlow()

    private val _breakfastEntries = MutableStateFlow<List<FoodEntry>>(emptyList())
    val breakfastEntries: StateFlow<List<FoodEntry>> = _breakfastEntries.asStateFlow()

    private val _lunchEntries = MutableStateFlow<List<FoodEntry>>(emptyList())
    val lunchEntries: StateFlow<List<FoodEntry>> = _lunchEntries.asStateFlow()

    private val _dinnerEntries = MutableStateFlow<List<FoodEntry>>(emptyList())
    val dinnerEntries: StateFlow<List<FoodEntry>> = _dinnerEntries.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        // Подписываемся на обновления профиля (калории будут обновляться автоматически)
        observeUserProfileUseCase()
            .onEach { profile ->
                val goal = profile.dailyCalories ?: 2000.0
                _calorieGoal.value = goal
                updateRemainingCalories()
            }
            .launchIn(viewModelScope)

        // Подписываемся на дневное потребление
        getDailyIntakeUseCase()
            .onEach { dailyIntake ->
                _totalCalories.value = dailyIntake.totalCalories
                updateRemainingCalories()
            }
            .launchIn(viewModelScope)

        // Подписываемся на записи для разделения по приёмам пищи
        getTodayEntriesUseCase()
            .onEach { entries ->
                _breakfastEntries.value = entries.filter { it.mealType == MealType.BREAKFAST }
                _lunchEntries.value = entries.filter { it.mealType == MealType.LUNCH }
                _dinnerEntries.value = entries.filter { it.mealType == MealType.DINNER }
            }
            .launchIn(viewModelScope)
    }

    private fun updateRemainingCalories() {
        _remainingCalories.value = _calorieGoal.value - _totalCalories.value
    }

    fun addFoodEntry(
        name: String,
        mealType: MealType,
        portion: Double,
        protein: Double,
        fat: Double,
        carbs: Double
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = addFoodEntryWithValidationUseCase(
                name = name,
                mealType = mealType,
                portion = portion,
                protein = protein,
                fat = fat,
                carbs = carbs
            )
            _isLoading.value = false

            when (result) {
                is AddFoodEntryWithValidationUseCase.Result.Success -> {
                    _successMessage.value = "${result.entry.name} добавлен"
                    clearSuccessMessageAfterDelay()
                }
                is AddFoodEntryWithValidationUseCase.Result.Error -> {
                    _errorMessage.value = result.message
                    clearErrorMessageAfterDelay()
                }
            }
        }
    }

    fun removeFoodEntry(entryId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            removeFoodEntryUseCase(entryId)
            _isLoading.value = false
            _successMessage.value = "Запись удалена"
            clearSuccessMessageAfterDelay()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccess() {
        _successMessage.value = null
    }

    private fun clearErrorMessageAfterDelay() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            _errorMessage.value = null
        }
    }

    private fun clearSuccessMessageAfterDelay() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            _successMessage.value = null
        }
    }
}