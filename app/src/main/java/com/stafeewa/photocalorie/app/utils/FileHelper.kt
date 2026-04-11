package com.stafeewa.photocalorie.app.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class FileHelper(private val context: Context) {

    companion object {
        private const val TAG = "FileHelper"
        private const val PROFILE_IMAGES_DIR = "profile_images"
    }

    /**
     * Копирует файл из content URI в постоянное хранилище приложения
     */
    suspend fun copyImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                // Получаем имя файла из URI
                val fileName = getFileNameFromUri(uri) ?: "profile_${System.currentTimeMillis()}.jpg"

                // Создаем директорию для изображений профиля, если ее нет
                val imagesDir = File(context.filesDir, PROFILE_IMAGES_DIR)
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs()
                }

                // Создаем файл в постоянном хранилище
                val outputFile = File(imagesDir, fileName)
                FileOutputStream(outputFile).use { outputStream ->
                    stream.copyTo(outputStream)
                }

                // Сохраняем путь к файлу (не URI)
                outputFile.absolutePath
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error copying image to internal storage", e)
            null
        }
    }

    /**
     * Получает имя файла из content URI
     */
    private fun getFileNameFromUri(uri: Uri): String? {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex >= 0) {
                        it.getString(displayNameIndex)
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file name from URI", e)
            null
        }
    }

    /**
     * Получает файл по сохраненному пути
     */
    fun getProfileImageFile(imagePath: String?): File? {
        return if (!imagePath.isNullOrEmpty()) {
            val file = File(imagePath)
            if (file.exists()) file else null
        } else {
            null
        }
    }

    /**
     * Удаляет старое изображение профиля
     */
    fun deleteOldProfileImage(oldImagePath: String?) {
        if (!oldImagePath.isNullOrEmpty()) {
            try {
                val oldFile = File(oldImagePath)
                if (oldFile.exists()) {
                    oldFile.delete()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting old profile image", e)
            }
        }
    }
}