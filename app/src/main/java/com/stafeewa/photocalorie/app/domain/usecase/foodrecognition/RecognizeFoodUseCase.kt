package com.stafeewa.photocalorie.app.domain.usecase.foodrecognition

import android.content.Context
import android.graphics.Bitmap
import com.stafeewa.photocalorie.app.domain.entity.MealType
import com.stafeewa.photocalorie.app.domain.entity.Product
import com.stafeewa.photocalorie.app.domain.repository.ProductRepository
import com.stafeewa.photocalorie.app.ml.FoodClassifier
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class RecognizeFoodUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    @ApplicationContext private val context: Context
) {
    private val foodClassifier by lazy { FoodClassifier(context) }

    sealed class Result {
        data class Success(val product: Product, val confidence: Float) : Result()
        data class MultipleMatches(val products: List<ProductMatch>) : Result()
        data class NotFound(val suggestedName: String) : Result()
        data class LowConfidence(val suggestedName: String) : Result()   // новый тип
        data class Error(val message: String) : Result()
    }

    data class ProductMatch(
        val product: Product,
        val confidence: Float,
        val matchScore: Int
    )

    suspend operator fun invoke(bitmap: Bitmap): Result {
        return try {
            val results = foodClassifier.recognizeFood(bitmap)
            if (results.isEmpty()) {
                return Result.NotFound("Не удалось распознать блюдо")
            }
            val best = results.first()
            val bestLabel = best.label.lowercase().trim()
            val confidence = best.confidence

            // Если уверенность ниже 40% – сразу предлагаем ручной ввод / поиск
            if (confidence < 0.4f) {
                return Result.LowConfidence(bestLabel)
            }

            // Загружаем все продукты из локальной БД
            val allProducts = MealType.entries.flatMap { mealType ->
                productRepository.getProductsByMealType(mealType).first()
            }.distinctBy { it.id }
            if (allProducts.isEmpty()) {
                return Result.NotFound(bestLabel)
            }

            // Оцениваем схожесть
            val scoredMatches = allProducts.map { product ->
                val score = calculateMatchScore(bestLabel, product.name.lowercase())
                ProductMatch(product, confidence, score)
            }.filter { it.matchScore > 30 }.sortedByDescending { it.matchScore }

            if (scoredMatches.isEmpty()) {
                return Result.NotFound(bestLabel)
            }

            val exactMatch = scoredMatches.firstOrNull { it.matchScore >= 95 }
            if (exactMatch != null) {
                return Result.Success(exactMatch.product, confidence)
            }

            if (scoredMatches.size > 1) {
                return Result.MultipleMatches(scoredMatches.take(5))
            }

            Result.Success(scoredMatches.first().product, confidence)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка распознавания")
        }
    }

    fun close() {
        foodClassifier.close()
    }

    private fun calculateMatchScore(recognized: String, dbName: String): Int {
        val synonyms = mapOf(
            "картошка фри" to listOf("картофель фри", "фри"),
            "картофель фри" to listOf("картошка фри", "фри"),
            "овсяная каша" to listOf("овсянка", "каша овсяная"),
            "гречневая каша" to listOf("гречка", "каша гречневая"),
            "яичница" to listOf("яйца", "яичница из двух яиц"),
            "борщ" to listOf("борщ украинский", "борщ со сметаной"),
            "салат цезарь" to listOf("цезарь")
        )

        for ((key, values) in synonyms) {
            if (recognized == key && values.any { dbName.contains(it) }) return 95
            if (values.contains(recognized) && dbName.contains(key)) return 95
        }

        if (recognized == dbName) return 100
        if (dbName.contains(recognized)) return 85
        if (recognized.contains(dbName)) return 70

        val recognizedWords = recognized.split(" ").filter { it.length > 2 }
        val dbWords = dbName.split(" ").filter { it.length > 2 }
        val commonWords = recognizedWords.intersect(dbWords.toSet()).size
        if (commonWords > 0) {
            val score = 40 + commonWords * 15
            return minOf(score, 90)
        }

        return 20
    }
}