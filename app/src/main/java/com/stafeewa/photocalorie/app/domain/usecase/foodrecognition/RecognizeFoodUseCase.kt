package com.stafeewa.photocalorie.app.domain.usecase.foodrecognition

import android.content.Context
import android.graphics.Bitmap
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
        data class Error(val message: String) : Result()
    }

    data class ProductMatch(
        val product: Product,
        val confidence: Float,
        val matchScore: Int
    )

    suspend operator fun invoke(bitmap: Bitmap): Result {
        return try {
            // 1. Распознаём метки
            val results = foodClassifier.recognizeFood(bitmap)
            if (results.isEmpty()) {
                return Result.NotFound("Не удалось распознать блюдо")
            }
            val best = results.first()
            val bestLabel = best.label.lowercase().trim()

            // 2. Ищем в локальной БД все продукты
            val allProducts = productRepository.searchProducts("").first() // получаем все продукты
            if (allProducts.isEmpty()) {
                return Result.NotFound(bestLabel)
            }

            // 3. Оцениваем каждый продукт на схожесть с распознанной меткой
            val scoredMatches = allProducts.map { product ->
                val score = calculateMatchScore(bestLabel, product.name.lowercase())
                ProductMatch(product, best.confidence, score)
            }.filter { it.matchScore > 30 }.sortedByDescending { it.matchScore }

            if (scoredMatches.isEmpty()) {
                return Result.NotFound(bestLabel)
            }

            // 4. Если есть точное совпадение (score >= 95) – возвращаем его
            val exactMatch = scoredMatches.firstOrNull { it.matchScore >= 95 }
            if (exactMatch != null) {
                return Result.Success(exactMatch.product, best.confidence)
            }

            // 5. Если несколько вариантов – показываем выбор
            if (scoredMatches.size > 1) {
                return Result.MultipleMatches(scoredMatches.take(5))
            }

            // 6. Иначе берём первый
            Result.Success(scoredMatches.first().product, best.confidence)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка распознавания")
        } finally {
            foodClassifier.close()
        }
    }

    private fun calculateMatchScore(recognized: String, dbName: String): Int {
        // Обработка синонимов
        val synonyms = mapOf(
            "картошка фри" to listOf("картофель фри", "фри"),
            "картофель фри" to listOf("картошка фри", "фри"),
            "овсяная каша" to listOf("овсянка", "каша овсяная"),
            "гречневая каша" to listOf("гречка", "каша гречневая"),
            "яичница" to listOf("яйца", "яичница из двух яиц"),
            "борщ" to listOf("борщ украинский", "борщ со сметаной"),
            "салат цезарь" to listOf("цезарь")
        )

        // Проверка на синонимы
        for ((key, values) in synonyms) {
            if (recognized == key && values.any { dbName.contains(it) }) return 95
            if (values.contains(recognized) && dbName.contains(key)) return 95
        }

        // Прямое совпадение
        if (recognized == dbName) return 100
        if (dbName.contains(recognized)) return 85
        if (recognized.contains(dbName)) return 70

        // По словам
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