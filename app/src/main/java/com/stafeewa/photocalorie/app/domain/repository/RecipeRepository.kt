package com.stafeewa.photocalorie.app.domain.repository

import com.stafeewa.photocalorie.app.domain.entity.Language
import com.stafeewa.photocalorie.app.domain.entity.Recipe
import com.stafeewa.photocalorie.app.domain.entity.RefreshConfig
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {

    fun getAllSubscription(): Flow<List<String>>

    fun startBackgroundRefresh(refreshConfig: RefreshConfig)

    suspend fun addSubscription(topic: String)

    suspend fun updateRecipesForTopic(topic: String, language: Language): Boolean

    suspend fun removeSubscription(topic: String)

    suspend fun updateRecipesForAllSubscriptions(language: Language): List<String>

    fun getRecipesByTopics(topics: List<String>): Flow<List<Recipe>>

    suspend fun clearAllRecipes(topics: List<String>)
}