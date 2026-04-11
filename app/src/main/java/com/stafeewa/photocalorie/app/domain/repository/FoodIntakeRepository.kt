package com.stafeewa.photocalorie.app.domain.repository

import com.stafeewa.photocalorie.app.domain.entity.DailyIntake
import com.stafeewa.photocalorie.app.domain.entity.FoodEntry
import com.stafeewa.photocalorie.app.domain.entity.MealType
import kotlinx.coroutines.flow.Flow

interface FoodIntakeRepository {

    val dailyIntakeFlow: Flow<DailyIntake>

    val dailyCalorieGoalFlow: Flow<Double>

    suspend fun setDailyCalorieGoal(calories: Double)

    suspend fun addFoodEntry(foodEntry: FoodEntry)

    suspend fun removeFoodEntry(entryId: Long)

    suspend fun updateFoodEntry(entryId: Long, portion: Double)

    suspend fun getTotalCaloriesForToday(): Double

    fun getTodayEntries(): Flow<List<FoodEntry>>

    suspend fun getEntriesByMealType(mealType: MealType): List<FoodEntry>

}