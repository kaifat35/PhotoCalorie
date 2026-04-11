package com.stafeewa.photocalorie.app.domain.repository

import com.stafeewa.photocalorie.app.domain.entity.DailyIntake
import com.stafeewa.photocalorie.app.domain.entity.FoodEntry
import kotlinx.coroutines.flow.Flow

interface FoodIntakeRepository {

    val dailyIntakeFlow: Flow<DailyIntake>

    val dailyCalorieGoalFlow: Flow<Double>

    suspend fun setDailyCalorieGoal(calories: Double)

    suspend fun addFoodEntry(foodEntry: FoodEntry)

    suspend fun removeFoodEntry(entryId: String)

    suspend fun updateFoodEntry(entryId: String, portion: Double)

    suspend fun getTotalCaloriesForToday(): Double

}