package com.stafeewa.photocalorie.app.ml

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import kotlinx.coroutines.tasks.await

class FoodClassifier(private val context: Context) {

    private val imageLabeler: ImageLabeler

    init {
        val localModel = LocalModel.Builder()
            .setAssetFilePath("food_model.tflite")
            .build()
        val options = CustomImageLabelerOptions.Builder(localModel)
            .setConfidenceThreshold(0.6f)
            .build()
        imageLabeler = ImageLabeling.getClient(options)
    }

    suspend fun recognizeFood(bitmap: Bitmap): List<LabelResult> {
        val image = InputImage.fromBitmap(bitmap, 0)
        return imageLabeler.process(image).await()
            .map { LabelResult(it.text, it.confidence) }
            .sortedByDescending { it.confidence }
    }

    fun close() = imageLabeler.close()
}

data class LabelResult(val label: String, val confidence: Float)