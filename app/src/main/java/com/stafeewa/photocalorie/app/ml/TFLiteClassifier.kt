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
    private val labels: List<String> by lazy {
        runCatching { context.assets.open("labels.txt").bufferedReader().readLines() }
            .getOrDefault(emptyList())
    }

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
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
            val inputBuffer = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4)
                .order(ByteOrder.nativeOrder())

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

            val output = runInferSignature(inputBuffer) ?: runDefaultInference(inputBuffer)
            if (output.isEmpty()) {
                return emptyList()
            }

            output
                .mapIndexed { index, score -> LabelResult(getLabelForIndex(index), score) }
                .sortedByDescending { it.confidence }
                .take(5)
                .filter { it.confidence > 0.01f }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun runInferSignature(inputBuffer: ByteBuffer): FloatArray? {
        val tflite = interpreter ?: return null
        return runCatching {
            val classes = getNumClassesFromInferSignature().takeIf { it > 1 } ?: return null
            val outputs = mutableMapOf<String, Any>("output" to Array(1) { FloatArray(classes) })
            tflite.runSignature(mapOf("x" to inputBuffer), outputs, "infer")
            outputs["output"] as? Array<FloatArray>
        }.getOrNull()?.firstOrNull()
    }

    private fun runDefaultInference(inputBuffer: ByteBuffer): FloatArray {
        val classes = getNumClassesFromDefaultTensor().takeIf { it > 1 } ?: return FloatArray(0)
        val outputBuffer = Array(1) { FloatArray(classes) }
        interpreter?.run(inputBuffer, outputBuffer)
        return outputBuffer[0]
    }

    private fun getNumClassesFromInferSignature(): Int {
        return runCatching {
            interpreter?.getOutputTensorFromSignature("output", "infer")?.shape()?.lastOrNull() ?: 0
        }.getOrDefault(0)
    }

    private fun getNumClassesFromDefaultTensor(): Int {
        return runCatching {
            interpreter?.getOutputTensor(0)?.shape()?.lastOrNull() ?: 0
        }.getOrDefault(0)
    }

    private fun getLabelForIndex(index: Int): String {
        return labels.getOrNull(index) ?: "Класс $index"
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
