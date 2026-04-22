package com.stafeewa.photocalorie.app.presentation.workers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.stafeewa.photocalorie.app.domain.repository.TrainingRepository
import com.stafeewa.photocalorie.app.utils.EnglishToRussianMap
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.tensorflow.lite.Interpreter
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min
import androidx.work.Data

@HiltWorker
class OnDeviceTrainingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val trainingRepository: TrainingRepository
) : CoroutineWorker(context, params) {

    companion object {
        const val UNIQUE_WORK_NAME = "on_device_training_periodic"
        const val KEY_MIN_EXAMPLES = "min_examples"
        private const val MODEL_FILE = "food_model.tflite"
        private const val WEIGHTS_FILE = "trained_weights.ckpt"
        private const val IMG_SIZE = 224
        private const val BATCH_SIZE = 8
        private const val EPOCHS = 3

        fun inputData(minExamples: Int): Data {
            return Data.Builder()
                .putInt(KEY_MIN_EXAMPLES, TrainingScheduleConfig.normalizeMinExamples(minExamples))
                .build()
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val examples = trainingRepository.getUnusedExamples()
            val minExamplesRequired = TrainingScheduleConfig.normalizeMinExamples(
                inputData.getInt(KEY_MIN_EXAMPLES, BATCH_SIZE)
            )
            if (examples.size < minExamplesRequired) return Result.success()
            if (examples.isEmpty()) return Result.success()

            val interpreter = Interpreter(loadModelBuffer())

            val checkpointPath = File(applicationContext.filesDir, WEIGHTS_FILE).absolutePath
            restore(interpreter, checkpointPath)

            val labelsVocabulary = EnglishToRussianMap.map.keys.toList()
            val prepared = examples.mapNotNull { example ->
                val bitmap = BitmapFactory.decodeFile(example.imagePath) ?: return@mapNotNull null
                val imageBuffer = convertBitmapToByteBuffer(bitmap)
                val labelIndex = labelsVocabulary.indexOf(example.label)
                if (labelIndex < 0) return@mapNotNull null

                val oneHot = FloatArray(labelsVocabulary.size)
                oneHot[labelIndex] = 1f
                imageBuffer to oneHot
            }

            if (prepared.isNotEmpty()) {
                repeat(EPOCHS) {
                    for (start in prepared.indices step BATCH_SIZE) {
                        val end = min(start + BATCH_SIZE, prepared.size)
                        val batch = prepared.subList(start, end)

                        val x = Array(batch.size) { index -> batch[index].first }
                        val y = Array(batch.size) { index -> batch[index].second }
                        train(interpreter, x, y)
                    }
                }

                save(interpreter, checkpointPath)
                trainingRepository.markAsUsed(examples.map { it.id })
                trainingRepository.deleteUsed()
            }

            interpreter.close()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun loadModelBuffer(): ByteBuffer {
        val bytes = applicationContext.assets.open(MODEL_FILE).use { it.readBytes() }
        return ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder()).apply {
            put(bytes)
            rewind()
        }
    }

    private fun train(interpreter: Interpreter, x: Array<ByteBuffer>, y: Array<FloatArray>) {
        interpreter.runSignature(
            mapOf("x" to x, "y" to y),
            mutableMapOf(),
            "train"
        )
    }

    private fun save(interpreter: Interpreter, checkpointPath: String) {
        interpreter.runSignature(
            mapOf("checkpoint_path" to arrayOf(checkpointPath)),
            mutableMapOf(),
            "save"
        )
    }

    private fun restore(interpreter: Interpreter, checkpointPath: String) {
        val checkpointFile = File(checkpointPath)
        if (!checkpointFile.exists()) return

        interpreter.runSignature(
            mapOf("checkpoint_path" to arrayOf(checkpointPath)),
            mutableMapOf(),
            "restore"
        )
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val resized = Bitmap.createScaledBitmap(bitmap, IMG_SIZE, IMG_SIZE, true)
        val byteBuffer = ByteBuffer.allocateDirect(4 * IMG_SIZE * IMG_SIZE * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(IMG_SIZE * IMG_SIZE)
        resized.getPixels(pixels, 0, IMG_SIZE, 0, 0, IMG_SIZE, IMG_SIZE)

        for (pixel in pixels) {
            byteBuffer.putFloat(((pixel shr 16) and 0xFF) / 255.0f)
            byteBuffer.putFloat(((pixel shr 8) and 0xFF) / 255.0f)
            byteBuffer.putFloat((pixel and 0xFF) / 255.0f)
        }

        byteBuffer.rewind()
        return byteBuffer
    }
}