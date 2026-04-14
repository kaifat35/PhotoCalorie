package com.stafeewa.photocalorie.app.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TFLiteClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private val inputSize = 224
    private val modelFileName = "food_model.tflite"
    private val tag = "TFLiteClassifier"

    init {
        loadModel()
    }

    private fun loadModel() {
        try {
            val modelFile = copyModelToCache()
            val options = Interpreter.Options()
            try {
                val gpuDelegate = GpuDelegate()
                options.addDelegate(gpuDelegate)
                Log.d(tag, "GPU delegate added")
            } catch (e: Exception) {
                Log.w(tag, "GPU not supported, using CPU")
            }
            interpreter = Interpreter(modelFile, options)
            Log.d(tag, "Model loaded successfully")
            // Проверим выходную форму
            val outputShape = interpreter?.getOutputTensor(0)?.shape()
            Log.d(tag, "Output shape: ${outputShape?.joinToString()}")
        } catch (e: Exception) {
            Log.e(tag, "Failed to load model", e)
            throw e
        }
    }

    private fun copyModelToCache(): File {
        val cacheFile = File(context.cacheDir, modelFileName)
        if (cacheFile.exists()) {
            Log.d(tag, "Model already in cache: ${cacheFile.absolutePath}")
            return cacheFile
        }
        context.assets.open(modelFileName).use { input ->
            cacheFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        Log.d(tag, "Model copied to cache: ${cacheFile.absolutePath}")
        return cacheFile
    }

    suspend fun recognizeFood(bitmap: Bitmap): List<LabelResult> {
        return try {
            Log.d(tag, "Starting recognition, bitmap size: ${bitmap.width}x${bitmap.height}")
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
            Log.d(tag, "Bitmap scaled to ${scaledBitmap.width}x${scaledBitmap.height}")

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

            val outputSize = getNumClasses()
            Log.d(tag, "Number of classes: $outputSize")
            val outputBuffer = Array(1) { FloatArray(outputSize) }

            interpreter?.run(inputBuffer, outputBuffer)
            Log.d(tag, "Inference completed")

            val results = outputBuffer[0].mapIndexed { index, score ->
                LabelResult(getLabelForIndex(index), score)
            }.filter { it.confidence > 0.1 }.sortedByDescending { it.confidence }

            Log.d(tag, "Results: ${results.take(3)}")
            results
        } catch (e: Exception) {
            Log.e(tag, "Error during recognition", e)
            emptyList()
        }
    }

    private fun getNumClasses(): Int {
        return try {
            val outputShape = interpreter?.getOutputTensor(0)?.shape() ?: return 0
            outputShape[1]
        } catch (e: Exception) {
            Log.e(tag, "Failed to get number of classes", e)
            0
        }
    }

    private fun getLabelForIndex(index: Int): String {
        return try {
            val labels = context.assets.open("labels.txt").bufferedReader().readLines()
            if (index < labels.size) labels[index] else "Класс $index"
        } catch (e: Exception) {
            "Класс $index"
        }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}

data class LabelResult(val label: String, val confidence: Float)