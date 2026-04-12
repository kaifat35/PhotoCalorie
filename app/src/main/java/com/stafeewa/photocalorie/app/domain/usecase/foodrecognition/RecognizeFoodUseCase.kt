package com.stafeewa.photocalorie.app.domain.usecase.foodrecognition

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.stafeewa.photocalorie.app.domain.entity.Product
import com.stafeewa.photocalorie.app.domain.repository.ProductRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RecognizeFoodUseCase @Inject constructor(
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

    suspend operator fun invoke(bitmap: Bitmap): Result {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
            val labels = labeler.process(image).await()
            labeler.close()

            if (labels.isEmpty()) {
                return Result.NotFound("Не удалось распознать блюдо")
            }

            val bestLabel = labels.maxByOrNull { it.confidence } ?: return Result.NotFound("Не удалось распознать")

            val allProducts = productRepository.searchProducts(bestLabel.text).first()

            if (allProducts.isEmpty()) {
                return Result.NotFound(bestLabel.text)
            }

            val exactMatch = allProducts.find {
                it.name.equals(bestLabel.text, ignoreCase = true)
            }

            if (exactMatch != null) {
                return Result.Success(exactMatch, bestLabel.confidence)
            }

            if (allProducts.size > 1) {
                val matches = allProducts.map { product ->
                    ProductMatch(product, bestLabel.confidence, calculateMatchScore(bestLabel.text, product.name))
                }.sortedByDescending { it.matchScore }
                return Result.MultipleMatches(matches)  // ← matches
            }

            Result.Success(allProducts.first(), bestLabel.confidence)

        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка распознавания")
        }
    }

    private fun calculateMatchScore(recognized: String, dbName: String): Int {
        val r = recognized.lowercase()
        val d = dbName.lowercase()

        return when {
            r == d -> 100
            d.contains(r) -> 80
            r.contains(d) -> 70
            r.split(" ").any { d.contains(it) } -> 50
            else -> 30
        }
    }
}