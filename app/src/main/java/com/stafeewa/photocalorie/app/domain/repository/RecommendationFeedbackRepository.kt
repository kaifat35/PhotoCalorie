package com.stafeewa.photocalorie.app.domain.repository

data class RecommendationFeedback(
    val productName: String,
    val isLiked: Boolean,
    val createdAtMillis: Long
)

interface RecommendationFeedbackRepository {
    suspend fun addFeedback(userId: Int, productName: String, isLiked: Boolean)
    suspend fun getFeedback(userId: Int): List<RecommendationFeedback>
}
