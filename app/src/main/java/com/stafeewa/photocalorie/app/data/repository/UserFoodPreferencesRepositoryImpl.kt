package com.stafeewa.photocalorie.app.data.repository

import com.stafeewa.photocalorie.app.data.local.UserFoodPreferencesDao
import com.stafeewa.photocalorie.app.data.local.UserFoodPreferencesEntity
import com.stafeewa.photocalorie.app.domain.repository.UserFoodPreferences
import com.stafeewa.photocalorie.app.domain.repository.UserFoodPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserFoodPreferencesRepositoryImpl @Inject constructor(
    private val dao: UserFoodPreferencesDao
) : UserFoodPreferencesRepository {

    override fun observePreferences(userId: Int): Flow<UserFoodPreferences?> {
        return dao.observePreferences(userId).map { it?.toDomain() }
    }

    override suspend fun getPreferences(userId: Int): UserFoodPreferences? {
        return dao.getPreferences(userId)?.toDomain()
    }

    override suspend fun upsertPreferences(userId: Int, preferences: UserFoodPreferences) {
        dao.upsertPreferences(
            UserFoodPreferencesEntity(
                userId = userId,
                preferredProducts = preferences.preferredProducts.joinToString("|"),
                excludedProducts = preferences.excludedProducts.joinToString("|"),
                preferredCategories = preferences.preferredCategories.joinToString("|"),
                excludedCategories = preferences.excludedCategories.joinToString("|")
            )
        )
    }

    private fun UserFoodPreferencesEntity.toDomain(): UserFoodPreferences = UserFoodPreferences(
        preferredProducts = preferredProducts.splitToSet(),
        excludedProducts = excludedProducts.splitToSet(),
        preferredCategories = preferredCategories.splitToSet(),
        excludedCategories = excludedCategories.splitToSet()
    )

    private fun String.splitToSet(): Set<String> =
        split("|").map { it.trim().lowercase() }.filter { it.isNotBlank() }.toSet()
}
