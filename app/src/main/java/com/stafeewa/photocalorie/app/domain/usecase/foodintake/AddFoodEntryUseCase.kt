package com.stafeewa.photocalorie.app.domain.usecase.foodintake


import com.stafeewa.photocalorie.app.domain.entity.FoodEntry
import com.stafeewa.photocalorie.app.domain.repository.FoodIntakeRepository
import javax.inject.Inject

class AddFoodEntryUseCase @Inject constructor(
    private val repository: FoodIntakeRepository
) {
    suspend operator fun invoke(foodEntry: FoodEntry) {
        repository.addFoodEntry(foodEntry)
    }
}