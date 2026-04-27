package com.stafeewa.photocalorie.app.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.collections.toFloatArray
import kotlin.math.exp

class TFLiteClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private val defaultInputSize = 224
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
            val tflite = interpreter ?: return emptyList()
            val inputSize = getInputSize(tflite)
            val inputType = tflite.getInputTensor(0).dataType()
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
            val inputBuffer = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * bytesPerChannel(inputType))
                .order(ByteOrder.nativeOrder())

            val pixels = IntArray(inputSize * inputSize)
            scaledBitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)

            for (pixel in pixels) {
                writeRgbToBuffer(
                    inputBuffer = inputBuffer,
                    red = (pixel shr 16) and 0xFF,
                    green = (pixel shr 8) and 0xFF,
                    blue = pixel and 0xFF,
                    type = inputType
                )
            }
            inputBuffer.rewind()

            val output = runInferSignature(inputBuffer) ?: runDefaultInference(inputBuffer)
            if (output.isEmpty()) {
                return emptyList()
            }

            val probabilities = normalizeToProbabilities(output)
            probabilities
                .mapIndexed { index, score -> LabelResult(getLabelForIndex(index), score) }
                .sortedByDescending { it.confidence }
                .take(5)
                .let { top ->
                    top.filter { it.confidence > 0.01f }.ifEmpty { top.take(1) }
                }
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
        val tflite = interpreter ?: return FloatArray(0)
        val outputTensor = tflite.getOutputTensor(0)
        val classes = outputTensor.shape().lastOrNull()?.takeIf { it > 1 } ?: return FloatArray(0)
        return when (outputTensor.dataType()) {
            DataType.FLOAT32 -> {
                val outputBuffer = Array(1) { FloatArray(classes) }
                tflite.run(inputBuffer, outputBuffer)
                outputBuffer[0]
            }
            DataType.UINT8, DataType.INT8 -> {
                val outputBuffer = Array(1) { ByteArray(classes) }
                tflite.run(inputBuffer, outputBuffer)
                // Преобразуем байты в FloatArray, нормализуя в диапазон [0,1]
                val byteArray = outputBuffer[0]
                FloatArray(classes) { (byteArray[it].toInt() and 0xFF).toFloat() / 255f }
            }
            else -> {
                // fallback: пробуем как Float
                val outputBuffer = Array(1) { FloatArray(classes) }
                tflite.run(inputBuffer, outputBuffer)
                outputBuffer[0]
            }
        }
    }

    private fun getInputSize(tflite: Interpreter): Int {
        return tflite.getInputTensor(0).shape().getOrNull(1)?.takeIf { it > 0 } ?: defaultInputSize
    }

    private fun bytesPerChannel(type: DataType): Int = when (type) {
        DataType.FLOAT32 -> 4
        DataType.UINT8, DataType.INT8 -> 1
        else -> 4
    }

    private fun writeRgbToBuffer(
        inputBuffer: ByteBuffer,
        red: Int,
        green: Int,
        blue: Int,
        type: DataType
    ) {
        when (type) {
            DataType.FLOAT32 -> {
                inputBuffer.putFloat(red / 255.0f)
                inputBuffer.putFloat(green / 255.0f)
                inputBuffer.putFloat(blue / 255.0f)
            }
            DataType.UINT8, DataType.INT8 -> {
                inputBuffer.put(red.toByte())
                inputBuffer.put(green.toByte())
                inputBuffer.put(blue.toByte())
            }
            else -> {
                inputBuffer.putFloat(red / 255.0f)
                inputBuffer.putFloat(green / 255.0f)
                inputBuffer.putFloat(blue / 255.0f)
            }
        }
    }

    private fun normalizeToProbabilities(rawScores: FloatArray): FloatArray {
        if (rawScores.isEmpty()) return rawScores
        val isProbabilityLike = rawScores.all { it in 0.0f..1.0f } &&
                rawScores.sum() in 0.90f..1.10f
        if (isProbabilityLike) return rawScores

        val maxScore = rawScores.maxOrNull() ?: return rawScores
        val expValues = rawScores.map { exp(it - maxScore) }
        val expSum = expValues.sum()
        if (expSum <= 0.0f) return rawScores
        return expValues.map { (it / expSum).toFloat() }.toFloatArray()
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
