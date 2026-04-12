package com.stafeewa.photocalorie.app.domain.usecase.foodrecognition

import com.stafeewa.photocalorie.app.domain.entity.MealType
import com.stafeewa.photocalorie.app.domain.entity.Product
import com.stafeewa.photocalorie.app.domain.repository.ProductRepository
import javax.inject.Inject

class AddRecognizedFoodToDatabaseUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(
        name: String,
        mealType: MealType,
        proteinPer100g: Double,
        fatPer100g: Double,
        carbsPer100g: Double,
        recognitionKeyword: String? = null
    ): Product {
        val caloriesPer100g = proteinPer100g * 4 + fatPer100g * 9 + carbsPer100g * 4

        val product = Product(
            name = name,
            mealType = mealType,
            defaultPortion = 100.0,
            proteinPer100g = proteinPer100g,
            fatPer100g = fatPer100g,
            carbsPer100g = carbsPer100g,
            caloriesPer100g = caloriesPer100g,
            keywords = buildKeywords(name, recognitionKeyword)
        )

        productRepository.addProduct(product)
        return product
    }

    private fun buildKeywords(name: String, recognitionKeyword: String?): List<String> {
        val normalized = name.lowercase()
        val keywords = mutableSetOf(normalized)
        val normalizedRecognitionKeyword = recognitionKeyword
            ?.lowercase()
            ?.trim()
            ?.takeIf { it.isNotBlank() }

        if (normalizedRecognitionKeyword != null) {
            keywords += normalizedRecognitionKeyword
        }

        if ("салат" in normalized) keywords += "salad"
        if ("суп" in normalized || "борщ" in normalized) keywords += "soup"
        if ("кур" in normalized) keywords += "chicken"
        if ("рыб" in normalized) keywords += "fish"
        if ("рис" in normalized) keywords += "rice"
        if ("греч" in normalized) keywords += "buckwheat"
        if ("каша" in normalized) keywords += "porridge"

        return keywords.toList()
    }
}