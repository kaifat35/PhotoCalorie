package com.stafeewa.photocalorie.app.data.repository

import com.stafeewa.photocalorie.app.data.local.TrainingExampleDao
import com.stafeewa.photocalorie.app.domain.entity.TrainingExample
import com.stafeewa.photocalorie.app.domain.repository.TrainingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrainingRepositoryImpl @Inject constructor(
    private val dao: TrainingExampleDao
) : TrainingRepository {
    override suspend fun saveTrainingExample(imagePath: String, label: String) {
        dao.insert(TrainingExample(imagePath = imagePath, label = label))
    }

    override suspend fun getUnusedExamples(): List<TrainingExample> = dao.getUnusedExamples()

    override suspend fun markAsUsed(ids: List<Long>) = dao.markAsUsed(ids)

    override suspend fun deleteUsed() = dao.deleteUsed()
}