package com.stafeewa.photocalorie.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.stafeewa.photocalorie.app.domain.entity.TrainingExample

@Dao
interface TrainingExampleDao {
    @Insert
    suspend fun insert(example: TrainingExample)

    @Query("SELECT * FROM training_examples WHERE used = 0")
    suspend fun getUnusedExamples(): List<TrainingExample>

    @Query("UPDATE training_examples SET used = 1 WHERE id IN (:ids)")
    suspend fun markAsUsed(ids: List<Long>)

    @Query("DELETE FROM training_examples WHERE used = 1")
    suspend fun deleteUsed()
}