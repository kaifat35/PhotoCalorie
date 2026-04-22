package com.stafeewa.photocalorie.app.presentation.screens.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.stafeewa.photocalorie.app.domain.entity.MealType
import com.stafeewa.photocalorie.app.domain.entity.Product
import com.stafeewa.photocalorie.app.domain.repository.ProductRepository
import com.stafeewa.photocalorie.app.domain.repository.TrainingRepository
import com.stafeewa.photocalorie.app.domain.usecase.foodrecognition.AddRecognizedFoodToDatabaseUseCase
import com.stafeewa.photocalorie.app.domain.usecase.foodrecognition.RecognizeFoodUseCase
import com.stafeewa.photocalorie.app.presentation.workers.OnDeviceTrainingWorker
import com.stafeewa.photocalorie.app.utils.EnglishToRussianMap
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val recognizeFoodUseCase: RecognizeFoodUseCase,
    private val addRecognizedFoodToDatabaseUseCase: AddRecognizedFoodToDatabaseUseCase,
    private val trainingRepository: TrainingRepository,
    private val workManager: WorkManager,
    @ApplicationContext private val appContext: Context,
    val productRepository: ProductRepository
) : ViewModel() {

    private val _recognitionResult = MutableStateFlow<RecognitionResult?>(null)
    val recognitionResult: StateFlow<RecognitionResult?> = _recognitionResult.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
    private var lastRecognizedBitmap: Bitmap? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            recognizeFoodUseCase.restoreTrainedWeights()
        }
    }

    fun captureAndRecognize(
        imageCapture: ImageCapture,
        cameraExecutor: ExecutorService,
        context: Context
    ) {
        val photoFile = File(context.cacheDir, "temp_photo.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    if (bitmap == null) {
                        _recognitionResult.value = RecognitionResult.Error("Ошибка чтения фото")
                        return
                    }
                    val cropped = cropToCaptureArea(bitmap)
                    lastRecognizedBitmap = cropped
                    recognizeFood(cropped)
                }

                override fun onError(exception: ImageCaptureException) {
                    _recognitionResult.value = RecognitionResult.Error(exception.message ?: "Ошибка фото")
                }
            }
        )
    }

    private fun cropToCaptureArea(bitmap: Bitmap): Bitmap {
        val cropWidth = (bitmap.width * 0.72f).toInt().coerceAtLeast(1)
        val cropHeight = (bitmap.height * 0.52f).toInt().coerceAtLeast(1)
        val left = ((bitmap.width - cropWidth) / 2).coerceAtLeast(0)
        val top = ((bitmap.height - cropHeight) / 2).coerceAtLeast(0)
        return Bitmap.createBitmap(bitmap, left, top, cropWidth, cropHeight)
    }

    private suspend fun translateProduct(product: Product): Product {
        val englishName = product.name
        val russianName = EnglishToRussianMap.map[englishName]?.trim()
        return if (russianName != null) {
            productRepository.getProductByName(russianName) ?: product
        } else {
            product
        }
    }
    fun onRecognitionConfirmed(foodName: String) {
        val bitmap = lastRecognizedBitmap ?: return
        val englishLabel = resolveEnglishLabel(foodName)
        saveTrainingExample(bitmap, englishLabel)
    }

    private fun saveTrainingExample(bitmap: Bitmap, label: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val trainingDir = File(appContext.filesDir, "training_examples").apply { mkdirs() }
                val file = File(trainingDir, "train_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                }
                trainingRepository.saveTrainingExample(file.absolutePath, label)
                enqueueTraining()
            } catch (e: Exception) {
                // no-op
            }
        }
    }

    private fun resolveEnglishLabel(foodName: String): String {
        val normalized = foodName.trim()
        return EnglishToRussianMap.map.entries
            .firstOrNull { (_, ru) -> ru.equals(normalized, ignoreCase = true) }
            ?.key
            ?: normalized.lowercase().replace(' ', '_')
    }

    private fun enqueueTraining() {
        val request = OneTimeWorkRequestBuilder<OnDeviceTrainingWorker>().build()
        workManager.enqueueUniqueWork(
            OnDeviceTrainingWorker.UNIQUE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun recognizeFood(bitmap: Bitmap) {
        viewModelScope.launch {
            _isProcessing.value = true

            val result = recognizeFoodUseCase(bitmap)

            _recognitionResult.value = when (result) {
                is RecognizeFoodUseCase.Result.Success -> {
                    val translatedProduct = translateProduct(result.product)
                    RecognitionResult.Success(
                        product = translatedProduct,
                        confidence = result.confidence
                    )
                }
                is RecognizeFoodUseCase.Result.MultipleMatches -> {
                    val translatedMatches = result.products.map { match ->
                        val translatedProduct = translateProduct(match.product)
                        match.copy(product = translatedProduct)
                    }
                    RecognitionResult.MultipleMatches(
                        matches = translatedMatches
                    )
                }
                is RecognizeFoodUseCase.Result.NotFound -> {
                    val translatedName = EnglishToRussianMap.map[result.suggestedName] ?: result.suggestedName
                    RecognitionResult.NotFound(suggestedName = translatedName)
                }
                is RecognizeFoodUseCase.Result.LowConfidence -> {
                    val translatedName = EnglishToRussianMap.map[result.suggestedName] ?: result.suggestedName
                    RecognitionResult.LowConfidence(suggestedName = translatedName)
                }
                is RecognizeFoodUseCase.Result.Error -> {
                    RecognitionResult.Error(result.message)
                }
            }

            _isProcessing.value = false
        }
    }

    fun addNewFoodToDatabase(
        name: String,
        mealType: MealType,
        protein: Double,
        fat: Double,
        carbs: Double
    ) {
        viewModelScope.launch {
            try {
                val product = addRecognizedFoodToDatabaseUseCase(
                    name = name,
                    mealType = mealType,
                    proteinPer100g = protein,
                    fatPer100g = fat,
                    carbsPer100g = carbs
                )

                val expectedCalories = protein * 4 + fat * 9 + carbs * 4
                val caloriesDelta = kotlin.math.abs(product.caloriesPer100g - expectedCalories)
                if (caloriesDelta > 0.01) {
                    _recognitionResult.value = RecognitionResult.Error("Ошибка расчёта калорий по БЖУ")
                    return@launch
                }

                _recognitionResult.value = RecognitionResult.Success(
                    product = product,
                    confidence = 1.0f
                )
            } catch (e: Exception) {
                _recognitionResult.value = RecognitionResult.Error(e.message ?: "Ошибка добавления")
            }
        }
    }

    fun clearResult() {
        _recognitionResult.value = null
        lastRecognizedBitmap = null
    }

    override fun onCleared() {
        recognizeFoodUseCase.close()
        super.onCleared()
    }
}

sealed class RecognitionResult {
    data class Success(val product: Product, val confidence: Float) : RecognitionResult()
    data class MultipleMatches(val matches: List<RecognizeFoodUseCase.ProductMatch>) : RecognitionResult()
    data class NotFound(val suggestedName: String) : RecognitionResult()
    data class LowConfidence(val suggestedName: String) : RecognitionResult()
    data class Error(val message: String) : RecognitionResult()
}