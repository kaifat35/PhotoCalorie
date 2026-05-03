package com.stafeewa.photocalorie.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecommendationFeedbackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(entity: RecommendationFeedbackEntity)

    @Query("SELECT * FROM recommendation_feedback WHERE userId = :userId")
    suspend fun getFeedbackForUser(userId: Int): List<RecommendationFeedbackEntity>
}
