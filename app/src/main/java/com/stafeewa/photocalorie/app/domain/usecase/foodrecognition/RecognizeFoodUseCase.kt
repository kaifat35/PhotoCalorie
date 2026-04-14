package com.stafeewa.photocalorie.app.domain.usecase.foodrecognition

import android.content.Context
import android.graphics.Bitmap
import com.stafeewa.photocalorie.app.domain.entity.Product
import com.stafeewa.photocalorie.app.domain.repository.ProductRepository
import com.stafeewa.photocalorie.app.ml.FoodClassifier
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class RecognizeFoodUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    private val context: Context
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
            // 1. Распознаём метки с помощью кастомной модели
            val results = foodClassifier.recognizeFood(bitmap)
            if (results.isEmpty()) {
                return Result.NotFound("Не удалось распознать блюдо")
            }
            val best = results.first()
            val bestLabel = best.label

            // 2. Ищем в локальной БД точное совпадение по названию
            val products = productRepository.searchProducts(bestLabel).first()
            if (products.isEmpty()) {
                return Result.NotFound(bestLabel)
            }

            // 3. Если нашлось одно – возвращаем его
            if (products.size == 1) {
                return Result.Success(products.first(), best.confidence)
            }

            // 4. Если несколько – показываем выбор (сортировка по релевантности)
            val matches = products.map { product ->
                ProductMatch(product, best.confidence, calculateMatchScore(bestLabel, product.name))
            }.sortedByDescending { it.matchScore }

            Result.MultipleMatches(matches.take(5))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка распознавания")
        } finally {
            foodClassifier.close()
        }
    }

    private fun calculateMatchScore(recognized: String, dbName: String): Int {
        val r = recognized.lowercase().trim()
        val d = dbName.lowercase().trim()
        return when {
            r == d -> 100
            d.contains(r) -> 85
            r.contains(d) -> 70
            else -> 30
        }
    }
}