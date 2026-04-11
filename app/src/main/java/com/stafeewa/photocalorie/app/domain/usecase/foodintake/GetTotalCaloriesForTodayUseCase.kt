package com.stafeewa.photocalorie.app.domain.usecase.foodintake

import com.stafeewa.photocalorie.app.domain.repository.FoodIntakeRepository
import javax.inject.Inject

class GetTotalCaloriesForTodayUseCase @Inject constructor(
    private val repository: FoodIntakeRepository
) {
    suspend operator fun invoke(): Double {
       return repository.getTotalCaloriesForToday()
    }
}