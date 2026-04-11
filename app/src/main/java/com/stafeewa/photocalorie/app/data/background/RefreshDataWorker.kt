package com.stafeewa.photocalorie.app.data.background

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.stafeewa.photocalorie.app.domain.usecase.recipe.UpdateSubscribedRecipesUseCase
import com.stafeewa.photocalorie.app.domain.usecase.settings.GetSettingsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class RefreshDataWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val updateSubscribedRecipesUseCase: UpdateSubscribedRecipesUseCase,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        Log.d("RefreshDataWorker", "Start")
        val settings = getSettingsUseCase().first()
        val updatedTopics = updateSubscribedRecipesUseCase()
        if (updatedTopics.isNotEmpty() && settings.notificationsEnabled) {
            notificationHelper.showNewRecipesNotification(updatedTopics)
        }
        Log.d("RefreshDataWorker", "Finish")
        return Result.success()
    }
}