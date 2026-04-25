package com.stafeewa.photocalorie.app.domain.usecase.userprofile

import com.stafeewa.photocalorie.app.domain.entity.FoodEntry
import com.stafeewa.photocalorie.app.domain.entity.Product
import com.stafeewa.photocalorie.app.domain.repository.ProductRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetRecommendationsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {

    private val targetProteinPercent = 0.30
    private val targetFatPercent = 0.30
    private val targetCarbsPercent = 0.40

    suspend operator fun invoke(
        tdee: Double,
        consumedEntries: List<FoodEntry>
    ): RecommendationResult {
        val totalCalories = consumedEntries.sumOf { it.calories }
        val totalProtein = consumedEntries.sumOf { it.protein }
        val totalFat = consumedEntries.sumOf { it.fat }
        val totalCarbs = consumedEntries.sumOf { it.carbs }

        val remainingCalories = tdee - totalCalories
        // Целевые макро на день
        val targetProtein = tdee * targetProteinPercent / 4  // 4 ккал на 1г белка
        val targetFat = tdee * targetFatPercent / 9
        val targetCarbs = tdee * targetCarbsPercent / 4

        val proteinDeficit = targetProtein - totalProtein
        val fatDeficit = targetFat - totalFat
        val carbsDeficit = targetCarbs - totalCarbs

        val suggestedProducts = mutableListOf<Product>()

        // Приоритет: сначала белок, затем жиры, затем углеводы
        if (proteinDeficit > 0) {
            suggestedProducts.addAll(
                productRepository.searchProducts("").first()
                    .filter { it.proteinPer100g > 15 }
                    .sortedByDescending { it.proteinPer100g / it.caloriesPer100g }
                    .take(3)
            )
        }
        if (fatDeficit > 0 && suggestedProducts.size < 5) {
            suggestedProducts.addAll(
                productRepository.searchProducts("").first()
                    .filter { it.fatPer100g > 10 }
                    .sortedByDescending { it.fatPer100g / it.caloriesPer100g }
                    .take(3 - suggestedProducts.size)
            )
        }
        if (carbsDeficit > 0 && suggestedProducts.size < 5) {
            suggestedProducts.addAll(
                productRepository.searchProducts("").first()
                    .filter { it.carbsPer100g > 20 }
                    .sortedByDescending { it.carbsPer100g / it.caloriesPer100g }
                    .take(5 - suggestedProducts.size)
            )
        }

        return RecommendationResult(
            totalCaloriesConsumed = totalCalories,
            remainingCalories = remainingCalories,
            proteinConsumed = totalProtein,
            fatConsumed = totalFat,
            carbsConsumed = totalCarbs,
            targetProtein = targetProtein,
            targetFat = targetFat,
            targetCarbs = targetCarbs,
            proteinDeficit = proteinDeficit,
            fatDeficit = fatDeficit,
            carbsDeficit = carbsDeficit,
            suggestedProducts = suggestedProducts.distinctBy { it.id }
        )
    }
}

data class RecommendationResult(
    val totalCaloriesConsumed: Double,
    val remainingCalories: Double,
    val proteinConsumed: Double,
    val fatConsumed: Double,
    val carbsConsumed: Double,
    val targetProtein: Double,
    val targetFat: Double,
    val targetCarbs: Double,
    val proteinDeficit: Double,
    val fatDeficit: Double,
    val carbsDeficit: Double,
    val suggestedProducts: List<Product>
)