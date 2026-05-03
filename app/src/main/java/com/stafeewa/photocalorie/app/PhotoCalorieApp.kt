package com.stafeewa.photocalorie.app

import android.app.Application
import android.content.Context
import android.content.res.Configuration as ResConfiguration
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.stafeewa.photocalorie.app.presentation.workers.AppStartupManager
import com.stafeewa.photocalorie.app.utils.LocaleManager
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale
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
        val prefs = base.getSharedPreferences("app_settings", MODE_PRIVATE)
        val languageCode = prefs.getString("language", "en") ?: "en"
        super.attachBaseContext(LocaleManager.setLocale(base, languageCode))
    }

    override fun onCreate() {
        super.onCreate()
        appStartupManager.startRefreshData()
    }

    /**
     * Обновляет локаль приложения (ресурсы) без перезапуска всего процесса.
     * Вызывается перед recreate() Activity, чтобы синглтоны получили обновлённые строки.
     */
    fun updateLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = ResConfiguration(resources.configuration) // используем псевдоним
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}