package com.stafeewa.photocalorie.app.domain.repository

import com.stafeewa.photocalorie.app.domain.entity.TrainingExample

interface TrainingRepository {
    suspend fun saveTrainingExample(imagePath: String, label: String)
    suspend fun getUnusedExamples(): List<TrainingExample>
    suspend fun markAsUsed(ids: List<Long>)
    suspend fun deleteUsed()
}