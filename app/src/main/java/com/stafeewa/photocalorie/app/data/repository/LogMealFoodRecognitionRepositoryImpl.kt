package com.stafeewa.photocalorie.app.data.repository

import android.graphics.Bitmap
import com.stafeewa.photocalorie.app.BuildConfig
import com.stafeewa.photocalorie.app.data.remote.LogMealApiService
import com.stafeewa.photocalorie.app.data.remote.LogMealImageRequest
import com.stafeewa.photocalorie.app.domain.entity.FoodRecognitionCandidate
import com.stafeewa.photocalorie.app.domain.repository.FoodRecognitionRepository
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class LogMealFoodRecognitionRepositoryImpl @Inject constructor(
    private val logMealApiService: LogMealApiService
) : FoodRecognitionRepository {

    override suspend fun recognizeFoodCandidates(bitmap: Bitmap): List<FoodRecognitionCandidate> {
        val imageId = uploadAndGetImageId(bitmap) ?: return emptyList()

        val ingredients = logMealApiService.recipeIngredients(
            authorization = authorizationHeader(),
            request = LogMealImageRequest(imageId)
        )
        val nutritionalInfo = logMealApiService.recipeNutritionalInfo(
            authorization = authorizationHeader(),
            request = LogMealImageRequest(imageId)
        )

        val names = (extractFoodNames(ingredients) + extractFoodNames(nutritionalInfo))
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        return names.map { FoodRecognitionCandidate(name = it, confidence = 1f) }
    }

    private suspend fun uploadAndGetImageId(bitmap: Bitmap): String? {
        val bytes = bitmapToJpegBytes(bitmap)
        if (bytes.isEmpty()) return null

        val requestBody = bytes.toRequestBody("image/jpeg".toMediaType())
        val imagePart = MultipartBody.Part.createFormData(
            name = "image",
            filename = "photo.jpg",
            body = requestBody
        )
        val response = logMealApiService.completeSegmentation(
            authorization = authorizationHeader(),
            image = imagePart
        )

        return findFirstStringValue(response, setOf("imageId", "image_id", "id"))
    }

    private fun authorizationHeader(): String {
        val token = BuildConfig.LOGMEAL_API_TOKEN.trim()
        return if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
    }

    private fun bitmapToJpegBytes(bitmap: Bitmap): ByteArray {
        val stream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
        return stream.toByteArray()
    }

    private fun extractFoodNames(json: JsonObject): List<String> {
        val keys = setOf("name", "label", "foodName", "food_name", "dishName", "dish_name")
        return findAllStringValues(json, keys)
    }

    private fun findFirstStringValue(element: JsonElement, keys: Set<String>): String? {
        return findAllStringValues(element, keys).firstOrNull()
    }

    private fun findAllStringValues(element: JsonElement, keys: Set<String>): List<String> {
        return when (element) {
            is JsonObject -> {
                element.entries.flatMap { (key, value) ->
                    val directValue = if (key in keys && value is JsonPrimitive && value.isString) {
                        listOf(value.content)
                    } else {
                        emptyList()
                    }
                    directValue + findAllStringValues(value, keys)
                }
            }
            is JsonArray -> element.flatMap { child -> findAllStringValues(child, keys) }
            else -> emptyList()
        }
    }
}
