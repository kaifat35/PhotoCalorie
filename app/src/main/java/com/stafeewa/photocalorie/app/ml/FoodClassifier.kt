package com.stafeewa.photocalorie.app.ml

import android.content.Context
import android.graphics.Bitmap

class FoodClassifier(private val context: Context) {

    private val tfliteClassifier = TFLiteClassifier(context)

    suspend fun recognizeFood(bitmap: Bitmap): List<LabelResult> {
        return tfliteClassifier.recognizeFood(bitmap)
    }

    fun close() {
        tfliteClassifier.close()
    }

    fun restore(checkpointPath: String) {
        tfliteClassifier.restore(checkpointPath)
    }
}