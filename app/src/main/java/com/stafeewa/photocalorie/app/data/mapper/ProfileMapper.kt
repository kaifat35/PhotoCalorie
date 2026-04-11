package com.stafeewa.photocalorie.app.data.mapper

import com.stafeewa.photocalorie.app.data.local.User
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
        userId = this.id
    )
}