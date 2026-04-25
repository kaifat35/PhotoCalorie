package com.stafeewa.photocalorie.app.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TFLiteClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private val inputSize = 224
    private val modelFileName = "food_model.tflite"

    init {
        loadModel()
    }

    private fun loadModel() {
        val modelFile = copyModelToCache()
        val options = Interpreter.Options()
        // Используем только CPU (GPU delegate отключён для избежания ошибок)
        interpreter = Interpreter(modelFile, options)
    }

    private fun copyModelToCache(): File {
        val cacheFile = File(context.cacheDir, modelFileName)
        if (cacheFile.exists()) {
            return cacheFile
        }

        context.assets.open(modelFileName).use { input ->
            cacheFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return cacheFile
    }

    suspend fun recognizeFood(bitmap: Bitmap): List<LabelResult> {
        return try {
            val outputSize = getNumClasses()
            if (outputSize <= 1) {
                return emptyList()
            }

            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
            val inputBuffer = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4)
            inputBuffer.order(ByteOrder.nativeOrder())

            val pixels = IntArray(inputSize * inputSize)
            scaledBitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)

            for (pixel in pixels) {
                val r = ((pixel shr 16 and 0xFF) / 255.0f)
                val g = ((pixel shr 8 and 0xFF) / 255.0f)
                val b = ((pixel and 0xFF) / 255.0f)
                inputBuffer.putFloat(r)
                inputBuffer.putFloat(g)
                inputBuffer.putFloat(b)
            }
            inputBuffer.rewind()

            val outputBuffer = Array(1) { FloatArray(outputSize) }
            interpreter?.run(inputBuffer, outputBuffer)

            outputBuffer[0]
                .mapIndexed { index, score -> LabelResult(getLabelForIndex(index), score) }
                .filter { it.confidence > 0.1f }
                .sortedByDescending { it.confidence }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun getNumClasses(): Int {
        return try {
            val outputShape = interpreter?.getOutputTensor(0)?.shape() ?: return 0
            outputShape[1]
        } catch (_: Exception) {
            0
        }
    }

    private fun getLabelForIndex(index: Int): String {
        return try {
            val labels = context.assets.open("labels.txt").bufferedReader().readLines()
            if (index < labels.size) labels[index] else "Класс $index"
        } catch (_: Exception) {
            "Класс $index"
        }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }

    fun restore(checkpointPath: String) {
        val checkpointFile = File(checkpointPath)
        if (!checkpointFile.exists()) return

        runCatching {
            interpreter?.runSignature(
                mapOf("checkpoint_path" to arrayOf(checkpointPath)),
                mutableMapOf(),
                "restore"
            )
        }
    }
}

data class LabelResult(val label: String, val confidence: Float)
