package com.stafeewa.photocalorie.app.presentation.screens.foodmain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stafeewa.photocalorie.app.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class FoodIntakeViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _dailyCalories = MutableStateFlow<Double?>(null)
    val dailyCalories: StateFlow<Double?> = _dailyCalories.asStateFlow()

    init {
        loadDailyCalories()
    }

    private fun loadDailyCalories() {
        userProfileRepository.getUserProfile()
            .onEach { profile ->
                _dailyCalories.value = profile.dailyCalories
            }
            .launchIn(viewModelScope)
    }
}