package com.stafeewa.photocalorie.app.domain.usecase.foodintake

import com.stafeewa.photocalorie.app.domain.repository.FoodIntakeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCalorieGoalUseCase @Inject constructor(
    private val repository: FoodIntakeRepository
) {
    operator fun invoke(): Flow<Double> {
        return repository.dailyCalorieGoalFlow
    }
}