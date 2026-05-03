package com.stafeewa.photocalorie.app.domain.repository

import kotlinx.coroutines.flow.Flow

data class UserFoodPreferences(
    val preferredProducts: Set<String> = emptySet(),
    val excludedProducts: Set<String> = emptySet(),
    val preferredCategories: Set<String> = emptySet(),
    val excludedCategories: Set<String> = emptySet()
)

interface UserFoodPreferencesRepository {
    fun observePreferences(userId: Int): Flow<UserFoodPreferences?>
    suspend fun getPreferences(userId: Int): UserFoodPreferences?
    suspend fun upsertPreferences(userId: Int, preferences: UserFoodPreferences)
}
