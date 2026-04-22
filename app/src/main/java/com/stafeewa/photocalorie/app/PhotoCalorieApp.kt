package com.stafeewa.photocalorie.app

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.stafeewa.photocalorie.app.presentation.workers.AppStartupManager
import com.stafeewa.photocalorie.app.presentation.workers.OnDeviceTrainingWorker
import com.stafeewa.photocalorie.app.utils.LocaleManager
import dagger.hilt.android.HiltAndroidApp
import org.tensorflow.lite.Interpreter
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class PhotoCalorieApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var workManager: WorkManager

    @Inject
    lateinit var appStartupManager: AppStartupManager

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun attachBaseContext(base: Context) {
        // Получаем сохраненный язык из SharedPreferences
        val prefs = base.getSharedPreferences("app_settings", MODE_PRIVATE)
        val languageCode = prefs.getString("language", "en") ?: "en"
        super.attachBaseContext(LocaleManager.setLocale(base, languageCode))
    }
    fun getInterpreter(): Interpreter = interpreter

    override fun onCreate() {
        super.onCreate()
        appStartupManager.startRefreshData()
        scheduleTraining()
        getInterpreter()
    }
    private fun scheduleTraining() {
        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .setRequiredNetworkType(NetworkType.UNMETERED) // Wi-Fi
            .build()
        val trainingRequest = PeriodicWorkRequestBuilder<OnDeviceTrainingWorker>(
            1, TimeUnit.DAYS
        ).setConstraints(constraints).build()
        workManager.enqueueUniquePeriodicWork(
            "on_device_training",
            ExistingPeriodicWorkPolicy.KEEP,
            trainingRequest
        )
    }

}