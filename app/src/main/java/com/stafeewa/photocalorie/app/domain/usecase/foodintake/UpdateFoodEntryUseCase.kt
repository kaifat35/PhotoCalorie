package com.stafeewa.photocalorie.app.domain.usecase.foodintake

import com.stafeewa.photocalorie.app.domain.repository.FoodIntakeRepository
import javax.inject.Inject

class UpdateFoodEntryUseCase @Inject constructor(
    private val repository: FoodIntakeRepository
) {
    suspend operator fun invoke(entryId: String, portion: Double) {
        repository.updateFoodEntry(entryId, portion)
    }
}