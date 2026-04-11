package com.stafeewa.photocalorie.app.domain.usecase.foodintake

import com.stafeewa.photocalorie.app.domain.entity.DailyIntake
import com.stafeewa.photocalorie.app.domain.repository.FoodIntakeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDailyIntakeUseCase @Inject constructor(
    private val repository: FoodIntakeRepository
) {
    operator fun invoke(): Flow<DailyIntake> {
        return repository.dailyIntakeFlow
    }
}