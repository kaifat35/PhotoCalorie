package com.stafeewa.photocalorie.app.domain.usecase.foodrecognition

import android.content.Context
import android.graphics.Bitmap
import com.stafeewa.photocalorie.app.R
import com.stafeewa.photocalorie.app.domain.entity.MealType
import com.stafeewa.photocalorie.app.domain.entity.Product
import com.stafeewa.photocalorie.app.domain.repository.ProductRepository
import com.stafeewa.photocalorie.app.ml.FoodClassifier
import com.stafeewa.photocalorie.app.ml.LabelResult
import com.stafeewa.photocalorie.app.utils.EnglishToRussianMap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject

class RecognizeFoodUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    @ApplicationContext private val context: Context
) {
    private val foodClassifier by lazy { FoodClassifier(context) }

    sealed class Result {
        data class Success(
            val product: Product,
            val confidence: Float,
            val alternatives: List<ProductMatch> = emptyList()
        ) : Result()
        data class MultipleMatches(val products: List<ProductMatch>) : Result()
        data class NotFound(val suggestedName: String) : Result()
        data class LowConfidence(val suggestedName: String) : Result()
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
                return Result.NotFound(context.getString(R.string.Couldnt_recognize_the_dish))
            }
            val best = results.first()
            val bestLabel = best.label.lowercase().trim()
            val confidence = best.confidence
            //точность
            if (confidence < 0.15f) {
                return Result.LowConfidence(bestLabel)
            }

            val russianLabel = EnglishToRussianMap.map[bestLabel] ?: bestLabel

            val allProducts = MealType.entries.flatMap { mealType ->
                productRepository.getProductsByMealType(mealType).first()
            }.distinctBy { it.id }

            val topModelMatches = buildTopModelMatches(results, allProducts)

            // Сначала ищем точное совпадение в БД
            val exactProduct = productRepository.getProductByName(russianLabel)
            if (exactProduct != null) {
                return Result.Success(
                    product = exactProduct,
                    confidence = confidence,
                    alternatives = prioritizeAlternatives(exactProduct, topModelMatches, confidence)
                )
            }

            if (allProducts.isEmpty()) {
                return Result.NotFound(russianLabel)
            }

            val scoredMatches = allProducts.map { product ->
                val score = calculateMatchScore(russianLabel, product.name.lowercase())
                ProductMatch(product, confidence, score)
            }.filter { it.matchScore > MIN_MATCH_SCORE }.sortedByDescending { it.matchScore }

            if (scoredMatches.isEmpty()) {
                return Result.NotFound(russianLabel)
            }

            val exactMatch = scoredMatches.firstOrNull { it.matchScore >= EXACT_MATCH_THRESHOLD }
            if (exactMatch != null) {
                return Result.Success(
                    product = exactMatch.product,
                    confidence = confidence,
                    alternatives = prioritizeAlternatives(exactMatch.product, topModelMatches, confidence)
                )
            }

            if (scoredMatches.size > 1) {
                return Result.MultipleMatches(scoredMatches.take(3))
            }

            val matchedProduct = scoredMatches.first().product
            Result.Success(
                product = matchedProduct,
                confidence = confidence,
                alternatives = prioritizeAlternatives(matchedProduct, topModelMatches, confidence)
            )
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка распознавания")
        }
    }

    private suspend fun buildTopModelMatches(
        recognitionResults: List<LabelResult>,
        allProducts: List<Product>
    ): List<ProductMatch> {
        return recognitionResults
            .take(TOP_MODEL_OPTIONS_COUNT)
            .mapNotNull { result ->
                val recognizedLabel = result.label.lowercase().trim()
                val russianLabel = EnglishToRussianMap.map[recognizedLabel] ?: recognizedLabel
                val exactProduct = productRepository.getProductByName(russianLabel)
                if (exactProduct != null) {
                    ProductMatch(exactProduct, result.confidence, EXACT_MATCH_SCORE)
                } else {
                    allProducts
                        .map { product ->
                            val score = calculateMatchScore(russianLabel, product.name.lowercase())
                            ProductMatch(product, result.confidence, score)
                        }
                        .filter { it.matchScore > MIN_MATCH_SCORE }
                        .maxByOrNull { it.matchScore }
                }
            }
            .distinctBy { it.product.id }
            .take(TOP_MODEL_OPTIONS_COUNT)
    }

    private fun prioritizeAlternatives(
        mainProduct: Product,
        alternatives: List<ProductMatch>,
        mainConfidence: Float
    ): List<ProductMatch> {
        val mainMatch = ProductMatch(mainProduct, mainConfidence, EXACT_MATCH_SCORE)
        return (listOf(mainMatch) + alternatives)
            .distinctBy { it.product.id }
            .take(TOP_MODEL_OPTIONS_COUNT)
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
            if (recognized == key && values.any { dbName.contains(it) }) return EXACT_MATCH_THRESHOLD
            if (values.contains(recognized) && dbName.contains(key)) return EXACT_MATCH_THRESHOLD
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

    fun restoreTrainedWeights() {
        val weightsFile = File(context.filesDir, "trained_weights.ckpt")
        if (!weightsFile.exists()) return
        foodClassifier.restore(weightsFile.absolutePath)
    }

    private companion object {
        const val TOP_MODEL_OPTIONS_COUNT = 3
        const val EXACT_MATCH_SCORE = 100
        const val EXACT_MATCH_THRESHOLD = 95
        const val MIN_MATCH_SCORE = 30
    }
}
