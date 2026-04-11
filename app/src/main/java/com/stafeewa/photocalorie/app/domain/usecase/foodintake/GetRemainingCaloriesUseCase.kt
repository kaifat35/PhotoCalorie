package com.stafeewa.photocalorie.app.domain.usecase.foodintake

import com.stafeewa.photocalorie.app.domain.repository.FoodIntakeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetRemainingCaloriesUseCase @Inject constructor(
    private val repository: FoodIntakeRepository
) {
    operator fun invoke(): Flow<Double> {
        return combine(
            repository.dailyCalorieGoalFlow,
            repository.dailyIntakeFlow
        ) { goal, intake ->
            goal - intake.totalCalories
        }
    }
}