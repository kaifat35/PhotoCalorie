package com.stafeewa.photocalorie.app.domain.usecase.foodrecognition

import android.graphics.Bitmap
import com.stafeewa.photocalorie.app.BuildConfig
import com.stafeewa.photocalorie.app.data.remote.LogMealApiService
import com.stafeewa.photocalorie.app.domain.entity.Product
import com.stafeewa.photocalorie.app.domain.repository.ProductRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.contentOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class RecognizeFoodUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    private val logMealApiService: LogMealApiService
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
        if (BuildConfig.LOGMEAL_API_TOKEN.isBlank()) {
            return Result.Error("LOGMEAL_API_TOKEN не настроен")
        }

        return try {
            val labels = recognizeLabelsWithLogMeal(bitmap)
            if (labels.isEmpty()) {
                return Result.NotFound("Не удалось распознать блюдо")
            }

            val bestLabel = labels.first()
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

    private suspend fun recognizeLabelsWithLogMeal(bitmap: Bitmap): List<RecognizedLabel> {
        val imageBytes = bitmap.toJpegByteArray()
        val requestBody = imageBytes.toRequestBody("image/jpeg".toMediaType())
        val imagePart = MultipartBody.Part.createFormData(
            name = "image",
            filename = "capture.jpg",
            body = requestBody
        )

        val response = logMealApiService.completeSegmentation(
            authorization = "Bearer ${BuildConfig.LOGMEAL_API_TOKEN}",
            image = imagePart
        )

        return extractRecognizedLabels(response)
            .sortedByDescending { it.confidence }
            .distinctBy { it.text }
    }

    private fun Bitmap.toJpegByteArray(): ByteArray {
        val stream = java.io.ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 92, stream)
        return stream.toByteArray()
    }

    private fun extractRecognizedLabels(root: JsonObject): List<RecognizedLabel> {
        val labels = mutableListOf<RecognizedLabel>()

        fun traverse(element: JsonElement) {
            when (element) {
                is JsonObject -> {
                    val name = findTextValue(element, listOf("name", "foodName", "label", "dish"))
                    val confidence = findFloatValue(element, listOf("confidence", "prob", "score", "probability"))
                    if (!name.isNullOrBlank()) {
                        labels += RecognizedLabel(name, confidence ?: 0.65f)
                    }
                    element.values.forEach(::traverse)
                }

                is JsonArray -> element.forEach(::traverse)
                else -> Unit
            }
        }

        traverse(root)
        return labels
    }

    private fun findTextValue(obj: JsonObject, keys: List<String>): String? {
        return keys.asSequence()
            .mapNotNull { key -> (obj[key] as? JsonPrimitive)?.contentOrNull }
            .firstOrNull { it.isNotBlank() }
    }

    private fun findFloatValue(obj: JsonObject, keys: List<String>): Float? {
        return keys.asSequence()
            .mapNotNull { key -> (obj[key] as? JsonPrimitive)?.floatOrNull }
            .firstOrNull()
    }

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

    private data class RecognizedLabel(
        val text: String,
        val confidence: Float
    )
}
