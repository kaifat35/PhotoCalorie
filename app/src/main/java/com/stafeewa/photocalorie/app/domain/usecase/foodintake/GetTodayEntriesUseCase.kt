package com.stafeewa.photocalorie.app.domain.usecase.foodintake

import com.stafeewa.photocalorie.app.domain.entity.FoodEntry
import com.stafeewa.photocalorie.app.domain.repository.FoodIntakeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTodayEntriesUseCase @Inject constructor(
    private val repository: FoodIntakeRepository
) {
    operator fun invoke(): Flow<List<FoodEntry>> {
        return repository.getTodayEntries()
    }
}