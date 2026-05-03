package com.stafeewa.photocalorie.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recommendation_feedback")
data class RecommendationFeedbackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Int,
    val productName: String,
    val isLiked: Boolean,
    val createdAtMillis: Long
)
