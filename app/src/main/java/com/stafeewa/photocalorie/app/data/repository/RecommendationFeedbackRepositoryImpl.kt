package com.stafeewa.photocalorie.app.data.repository

import com.stafeewa.photocalorie.app.data.local.RecommendationFeedbackDao
import com.stafeewa.photocalorie.app.data.local.RecommendationFeedbackEntity
import com.stafeewa.photocalorie.app.domain.repository.RecommendationFeedback
import com.stafeewa.photocalorie.app.domain.repository.RecommendationFeedbackRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecommendationFeedbackRepositoryImpl @Inject constructor(
    private val dao: RecommendationFeedbackDao
) : RecommendationFeedbackRepository {

    override suspend fun addFeedback(userId: Int, productName: String, isLiked: Boolean) {
        dao.insertFeedback(
            RecommendationFeedbackEntity(
                userId = userId,
                productName = productName,
                isLiked = isLiked,
                createdAtMillis = System.currentTimeMillis()
            )
        )
    }

    override suspend fun getFeedback(userId: Int): List<RecommendationFeedback> {
        return dao.getFeedbackForUser(userId).map {
            RecommendationFeedback(
                productName = it.productName,
                isLiked = it.isLiked,
                createdAtMillis = it.createdAtMillis
            )
        }
    }
}
