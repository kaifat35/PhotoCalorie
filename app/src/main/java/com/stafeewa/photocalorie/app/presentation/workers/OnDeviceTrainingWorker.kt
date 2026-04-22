package com.stafeewa.photocalorie.app.presentation.workers

import android.content.Context
import android.graphics.BitmapFactory
import androidx.work.Worker
import androidx.work.WorkerParameters
import coil3.Bitmap
import com.stafeewa.photocalorie.app.domain.repository.TrainingRepository
import com.stafeewa.photocalorie.app.utils.EnglishToRussianMap
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import org.tensorflow.lite.Interpreter
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

@AndroidEntryPoint
class OnDeviceTrainingWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    @Inject lateinit var trainingRepository: TrainingRepository

    companion object {
        private const val MODEL_FILE = "food_model_ondevice.tflite"
        private const val WEIGHTS_FILE = "trained_weights.ckpt"
        private const val IMG_SIZE = 224
        private const val BATCH_SIZE = 8
    }

    override fun doWork(): Result {
        return try {
            val examples = runBlocking { trainingRepository.getUnusedExamples() }
            if (examples.isEmpty()) return Result.success()

            // Загружаем модель
            val model = loadModelFile()
            val interpreter = Interpreter(model)

            // Загружаем сохранённые веса (если есть)
            val weightsFile = File(applicationContext.filesDir, WEIGHTS_FILE)
            if (weightsFile.exists()) {
                val restore = interpreter.getSignatureRunner("restore")
                restore.inputs["checkpoint_path"] = weightsFile.absolutePath
                restore.run()
            }

            val train = interpreter.getSignatureRunner("train")
            val save = interpreter.getSignatureRunner("save")

            // Список всех возможных меток (101 класс)
            val allLabels = EnglishToRussianMap.map.keys.toList()
            val numClasses = allLabels.size

            // Подготовка данных
            val images = mutableListOf<ByteBuffer>()
            val labels = mutableListOf<FloatArray>()

            for (ex in examples) {
                val bitmap = BitmapFactory.decodeFile(ex.imagePath) ?: continue
                val resized = Bitmap.createScaledBitmap(bitmap, IMG_SIZE, IMG_SIZE, true)
                val byteBuffer = convertBitmapToByteBuffer(resized)
                images.add(byteBuffer)

                // one-hot encoding
                val labelIndex = allLabels.indexOf(ex.label)
                val oneHot = FloatArray(numClasses) { 0f }
                if (labelIndex >= 0) oneHot[labelIndex] = 1f
                labels.add(oneHot)
            }

            // Обучаем батчами
            for (i in 0 until images.size step BATCH_SIZE) {
                val end = min(i + BATCH_SIZE, images.size)
                val batchImages = images.subList(i, end).toTypedArray()
                val batchLabels = labels.subList(i, end).toTypedArray()

                train.inputs["x"] = batchImages
                train.inputs["y"] = batchLabels
                train.run()
            }

            // Сохраняем обновлённые веса
            val checkpointPath = File(applicationContext.filesDir, WEIGHTS_FILE).absolutePath
            save.inputs["checkpoint_path"] = checkpointPath
            save.run()

            // Помечаем примеры как использованные
            runBlocking {
                trainingRepository.markAsUsed(examples.map { it.id })
                trainingRepository.deleteUsed()
            }

            interpreter.close()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun loadModelFile(): ByteBuffer {
        val assetFile = applicationContext.assets.openFd(MODEL_FILE)
        val inputStream = assetFile.createInputStream()
        val bytes = inputStream.readBytes()
        return ByteBuffer.wrap(bytes)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * IMG_SIZE * IMG_SIZE * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        val pixels = IntArray(IMG_SIZE * IMG_SIZE)
        bitmap.getPixels(pixels, 0, IMG_SIZE, 0, 0, IMG_SIZE, IMG_SIZE)
        for (pixel in pixels) {
            val r = ((pixel shr 16) and 0xFF) / 255.0f
            val g = ((pixel shr 8) and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f
            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }
        return byteBuffer
    }
}