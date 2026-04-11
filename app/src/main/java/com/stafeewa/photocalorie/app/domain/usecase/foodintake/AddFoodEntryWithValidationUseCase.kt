package com.stafeewa.photocalorie.app.domain.usecase.foodintake

import com.stafeewa.photocalorie.app.domain.entity.FoodEntry
import com.stafeewa.photocalorie.app.domain.repository.FoodIntakeRepository
import javax.inject.Inject

class AddFoodEntryWithValidationUseCase @Inject constructor(
    private val repository: FoodIntakeRepository
) {
    sealed class Result {
        data class Success(val entry: FoodEntry) : Result()
        data class Error(val message: String) : Result()
    }

    suspend operator fun invoke(
        name: String,
        mealType: com.stafeewa.photocalorie.app.domain.entity.MealType,
        portion: Double,
        protein: Double,
        fat: Double,
        carbs: Double
    ): Result {
        return when {
            name.isBlank() -> Result.Error("Название блюда не может быть пустым")
            portion <= 0 -> Result.Error("Порция должна быть больше 0")
            protein < 0 || fat < 0 || carbs < 0 -> Result.Error("КБЖУ не может быть отрицательным")
            else -> {
                val entry = FoodEntry(
                    name = name,
                    mealType = mealType,
                    portion = portion,
                    protein = protein,
                    fat = fat,
                    carbs = carbs
                )
                repository.addFoodEntry(entry)
                Result.Success(entry)
            }
        }
    }
}