package com.stafeewa.photocalorie.app.data.mapper

import com.stafeewa.photocalorie.app.data.local.User
import com.stafeewa.photocalorie.app.domain.entity.ActivityLevel
import com.stafeewa.photocalorie.app.domain.entity.UserProfile

fun User.toUserProfile(): UserProfile {
    return UserProfile(
        login = this.login,
        email = this.email,
        password = this.password,
        gender = this.gender,
        height = this.height,
        weight = this.weight,
        age = this.age,
        imageUri = this.imageUri,
        dailyCalories = this.dailyCalories,
        userId = this.id,
        activityLevel = try {
            ActivityLevel.valueOf(activityLevel ?: ActivityLevel.MODERATELY_ACTIVE.name)
        } catch (e: Exception) {
            ActivityLevel.MODERATELY_ACTIVE
        }
    )
}

fun UserProfile.toUser(): User = User(
    id = 0,
    login = login,
    email = email,
    password = password,
    gender = gender,
    height = height,
    weight = weight,
    age = age,
    imageUri = imageUri,
    dailyCalories = dailyCalories,
    activityLevel = activityLevel.name
)