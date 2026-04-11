package com.stafeewa.photocalorie.app.domain.usecase.foodintake

import com.stafeewa.photocalorie.app.domain.entity.FoodEntry
import com.stafeewa.photocalorie.app.domain.entity.MealType
import com.stafeewa.photocalorie.app.domain.repository.FoodIntakeRepository
import javax.inject.Inject

class GetEntriesByMealTypeUseCase @Inject constructor(
    private val repository: FoodIntakeRepository
) {
    suspend operator fun invoke(mealType: MealType): List<FoodEntry> {
        return repository.getEntriesByMealType(mealType)
    }
}