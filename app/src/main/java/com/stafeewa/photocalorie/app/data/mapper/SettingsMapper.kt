package com.stafeewa.photocalorie.app.data.mapper

import com.stafeewa.photocalorie.app.domain.entity.RefreshConfig
import com.stafeewa.photocalorie.app.domain.entity.Settings

fun Settings.toRefreshConfig(): RefreshConfig {
    return RefreshConfig(language, interval, wifiOnly)
}