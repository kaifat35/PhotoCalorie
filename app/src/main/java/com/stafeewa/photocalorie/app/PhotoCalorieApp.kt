package com.stafeewa.photocalorie.app

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.stafeewa.photocalorie.app.presentation.startup.AppStartupManager
import com.stafeewa.photocalorie.app.utils.LocaleManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PhotoCalorieApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

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

    override fun onCreate() {
        super.onCreate()
        appStartupManager.startRefreshData()
    }
}