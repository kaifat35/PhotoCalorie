package com.stafeewa.photocalorie.app.domain.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "training_examples")
data class TrainingExample(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imagePath: String,     // путь к сохранённому изображению
    val label: String,         // английская метка (например "borsh")
    val used: Boolean = false  // использовано для обучения
)