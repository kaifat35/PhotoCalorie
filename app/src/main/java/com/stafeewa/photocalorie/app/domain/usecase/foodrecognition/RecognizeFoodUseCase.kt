package com.stafeewa.photocalorie.app.domain.usecase.foodrecognition

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.stafeewa.photocalorie.app.domain.entity.Product
import com.stafeewa.photocalorie.app.domain.repository.FoodRecognitionRepository
import com.stafeewa.photocalorie.app.domain.repository.ProductRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RecognizeFoodUseCase @Inject constructor(
    private val foodRecognitionRepository: FoodRecognitionRepository,
    private val productRepository: ProductRepository
) {
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

    private val enToRuKeywordMap = mapOf(
        "oatmeal" to "овсян",
        "porridge" to "каша",
        "buckwheat" to "греч",
        "rice" to "рис",
        "egg" to "яич",
        "omelette" to "омлет",
        "omelet" to "омлет",
        "chicken" to "кур",
        "fish" to "рыб",
        "salad" to "салат",
        "soup" to "суп",
        "borscht" to "борщ",
        "pasta" to "макарон",
        "potato" to "карто",
        "cutlet" to "котлет",
        "pilaf" to "плов",
        "cottage cheese" to "творог",
        "yogurt" to "йогурт",
        "pancake" to "блин"
    )

    suspend operator fun invoke(bitmap: Bitmap): Result {
        return try {
            val recognizedLabels = recognizeViaLogMeal(bitmap)
                .ifEmpty { recognizeViaMlKit(bitmap) }

            if (recognizedLabels.isEmpty()) {
                return Result.NotFound("Не удалось распознать блюдо")
            }

            val bestLabel = recognizedLabels.first()
            val variants = buildSearchVariants(bestLabel.text)

            val allProducts = variants.flatMap { query ->
                productRepository.searchProducts(query).first()
            }.distinctBy { it.id }

            if (allProducts.isEmpty()) {
                return Result.NotFound(bestLabel.text)
            }

            val scoredMatches = allProducts.map { product ->
                val score = variants.maxOf { variant ->
                    val allTokens = listOf(product.name) + product.keywords
                    allTokens.maxOf { token ->
                        calculateMatchScore(normalize(variant), normalize(token))
                    }
                }
                ProductMatch(product, bestLabel.confidence, score)
            }.sortedByDescending { it.matchScore }

            val exactMatch = scoredMatches.firstOrNull { it.matchScore >= 95 }
            if (exactMatch != null) {
                return Result.Success(exactMatch.product, bestLabel.confidence)
            }

            if (scoredMatches.size > 1) {
                return Result.MultipleMatches(scoredMatches.take(5))
            }

            Result.Success(scoredMatches.first().product, bestLabel.confidence)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка распознавания")
        }
    }

    private suspend fun recognizeViaLogMeal(bitmap: Bitmap): List<RecognizedLabel> {
        return foodRecognitionRepository.recognizeFoodCandidates(bitmap)
            .map { RecognizedLabel(text = it.name, confidence = it.confidence) }
            .sortedByDescending { it.confidence }
    }

    private suspend fun recognizeViaMlKit(bitmap: Bitmap): List<RecognizedLabel> {
        val image = InputImage.fromBitmap(bitmap, 0)
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        return try {
            labeler.process(image).await()
                .map { RecognizedLabel(text = it.text, confidence = it.confidence) }
                .sortedByDescending { it.confidence }
        } finally {
            labeler.close()
        }
    }

    private data class RecognizedLabel(
        val text: String,
        val confidence: Float
    )

    private fun buildSearchVariants(label: String): Set<String> {
        val normalized = normalize(label)
        val variants = mutableSetOf(normalized)
        enToRuKeywordMap.forEach { (en, ru) ->
            if (normalized.contains(en)) {
                variants += normalized.replace(en, ru)
                variants += ru
            }
        }
        normalized.split(" ")
            .filter { it.length >= 3 }
            .forEach { token ->
                variants += token
                enToRuKeywordMap[token]?.let { variants += it }
            }
        return variants.filter { it.isNotBlank() }.toSet()
    }

    private fun normalize(value: String): String {
        return value.lowercase()
            .replace(Regex("[^a-zа-я0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun calculateMatchScore(recognized: String, dbName: String): Int {
        return when {
            recognized == dbName -> 100
            dbName.contains(recognized) -> 80
            recognized.contains(dbName) -> 70
            recognized.split(" ").any { dbName.contains(it) && it.length > 2 } -> 50
            else -> 20
        }
    }
}
