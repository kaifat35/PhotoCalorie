package com.stafeewa.photocalorie.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val login: String,
    val email: String,
    val password: String,
    val gender: String? = null,
    val height: Double? = null,
    val weight: Double? = null,
    val age: Int? = null,
    val imageUri: String? = null,
    val dailyCalories: Double? = null
)