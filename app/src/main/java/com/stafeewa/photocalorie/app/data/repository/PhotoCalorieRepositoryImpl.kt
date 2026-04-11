package com.stafeewa.photocalorie.app.data.repository

import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.stafeewa.photocalorie.app.data.local.PhotoCalorieDao
import com.stafeewa.photocalorie.app.data.local.RecipeDbModel
import com.stafeewa.photocalorie.app.data.local.SubscriptionDbModel
import com.stafeewa.photocalorie.app.data.mapper.toDbModel
import com.stafeewa.photocalorie.app.data.mapper.toEntities
import com.stafeewa.photocalorie.app.data.mapper.toQueryParam
import com.stafeewa.photocalorie.app.domain.entity.Language
import com.stafeewa.photocalorie.app.domain.entity.RefreshConfig
import com.stafeewa.photocalorie.app.domain.repository.RecipeRepository
import com.stafeewa.photocalorie.app.data.background.RefreshDataWorker
import com.stafeewa.photocalorie.app.data.remote.RecipesApiService
import com.stafeewa.photocalorie.app.domain.entity.Recipe
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PhotoCalorieRepositoryImpl @Inject constructor(
    private val photoCalorieDao: PhotoCalorieDao,
    private val recipesApiService: RecipesApiService,
    private val workManager: WorkManager
) : RecipeRepository {

    override fun getAllSubscription(): Flow<List<String>> {
        return photoCalorieDao.getAllSubscriptions().map { subscriptions ->
            subscriptions.map { it.topic }
        }
    }

    override suspend fun addSubscription(topic: String) {
        photoCalorieDao.addSubscription(SubscriptionDbModel(topic))
    }

    override suspend fun updateRecipesForTopic(topic: String, language: Language): Boolean {
        val recipes = loadRecipes(topic, language)
        val ids = photoCalorieDao.addRecipes(recipes)
        return ids.any { it != -1L }
    }

    private suspend fun loadRecipes(topic: String, language: Language): List<RecipeDbModel> {
        return try {
            recipesApiService.loadRecipes(topic, language.toQueryParam()).toDbModel(topic)
        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }
            Log.e("RecipeRepository", e.stackTraceToString())
            listOf()
        }
    }

    override suspend fun removeSubscription(topic: String) {
        photoCalorieDao.deleteSubscription(SubscriptionDbModel(topic))
    }

    override suspend fun updateRecipesForAllSubscriptions(language: Language): List<String> {
        val updatesTopics = mutableListOf<String>()
        val subscriptions = photoCalorieDao.getAllSubscriptions().first()
        coroutineScope {
            subscriptions.forEach {
                launch {
                    val updated = updateRecipesForTopic(it.topic, language)
                    if (updated) {
                        updatesTopics.add(it.topic)
                    }
                }
            }
        }
        return updatesTopics
    }

    override fun getRecipesByTopics(topics: List<String>): Flow<List<Recipe>> {
        return photoCalorieDao.getAllRecipesByTopics(topics).map {
            it.toEntities()
        }
    }

    override fun startBackgroundRefresh(refreshConfig: RefreshConfig) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                if (refreshConfig.wifiOnly) {
                    NetworkType.UNMETERED
                } else {
                    NetworkType.CONNECTED
                }
            )
            .setRequiresBatteryNotLow(true)
            .build()

        val request = PeriodicWorkRequestBuilder<RefreshDataWorker>(
            refreshConfig.interval.minutes.toLong(),
            TimeUnit.MINUTES
        ).setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            uniqueWorkName = "Refresh data",
            existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request = request
        )
    }

    override suspend fun clearAllRecipes(topics: List<String>) {
        photoCalorieDao.deleteRecipesByTopics(topics)
    }
}