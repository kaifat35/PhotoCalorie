package com.stafeewa.photocalorie.app.data.repository

import com.stafeewa.photocalorie.app.data.local.PhotoCalorieDao
import com.stafeewa.photocalorie.app.data.mapper.toDbModel
import com.stafeewa.photocalorie.app.data.mapper.toEntity
import com.stafeewa.photocalorie.app.domain.entity.DailyIntake
import com.stafeewa.photocalorie.app.domain.entity.FoodEntry
import com.stafeewa.photocalorie.app.domain.repository.FoodIntakeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class FoodIntakeRepositoryImpl @Inject constructor(
    private val dao: PhotoCalorieDao
) : FoodIntakeRepository {

    private val _dailyCalorieGoal = MutableStateFlow(2100.0)
    override val dailyCalorieGoalFlow: Flow<Double> = _dailyCalorieGoal

    override suspend fun setDailyCalorieGoal(calories: Double) {
        _dailyCalorieGoal.value = calories
    }

    override val dailyIntakeFlow: Flow<DailyIntake>
        get() = dao.getTodayEntries().map { entries ->
            val domainEntries = entries.map { it.toEntity() }
            val meals = domainEntries.groupBy { it.mealType }

            DailyIntake(
                date = LocalDate.now().toString(),
                meals = meals,
                totalCalories = domainEntries.sumOf { it.calories },
                totalProtein = domainEntries.sumOf { it.protein },
                totalFat = domainEntries.sumOf { it.fat },
                totalCarbs = domainEntries.sumOf { it.carbs }
            )
        }

    override suspend fun addFoodEntry(foodEntry: FoodEntry) {
        dao.insertFoodEntry(foodEntry.toDbModel())
    }

    override suspend fun removeFoodEntry(entryId: String) {
        dao.deleteFoodEntry(entryId)
    }

    override suspend fun updateFoodEntry(entryId: String, portion: Double) {
        dao.updatePortion(entryId, portion)
    }

    override suspend fun getTotalCaloriesForToday(): Double {
        return dao.getTodayEntries().first().sumOf { it.calories }
    }
}