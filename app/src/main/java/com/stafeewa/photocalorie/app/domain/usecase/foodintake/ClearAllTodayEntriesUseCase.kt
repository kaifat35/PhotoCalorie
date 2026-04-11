package com.stafeewa.photocalorie.app.domain.usecase.foodintake

import com.stafeewa.photocalorie.app.domain.repository.FoodIntakeRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ClearAllTodayEntriesUseCase @Inject constructor(
    private val repository: FoodIntakeRepository
) {
    suspend operator fun invoke() {
        val entries = repository.getTodayEntries().first()
        entries.forEach { entry ->
            repository.removeFoodEntry(entry.id)
        }
    }
}