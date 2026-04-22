package com.stafeewa.photocalorie.app.presentation.workers

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrainingWorkScheduler @Inject constructor(
    private val workManager: WorkManager
) {

    fun schedulePeriodicTraining(frequencyHours: Int, minExamples: Int) {
        val normalizedFrequencyHours = TrainingScheduleConfig.normalizeFrequencyHours(frequencyHours)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresCharging(true)
            .build()

        val request = PeriodicWorkRequestBuilder<OnDeviceTrainingWorker>(
            normalizedFrequencyHours.toLong(),
            TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInputData(OnDeviceTrainingWorker.inputData(minExamples))
            .build()

        workManager.enqueueUniquePeriodicWork(
            OnDeviceTrainingWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}