package com.stafeewa.photocalorie.app.domain.usecase.foodintake

import com.stafeewa.photocalorie.app.domain.repository.FoodIntakeRepository
import javax.inject.Inject

class SetDailyCalorieGoalUseCase @Inject constructor(
    private val repository: FoodIntakeRepository
) {
    suspend operator fun invoke(calories: Double) {
        repository.setDailyCalorieGoal(calories)
    }
}