package org.pakicek.monoforecast.domain.repository

import org.pakicek.monoforecast.data.local.entity.SettingsBlockEntity
import org.pakicek.monoforecast.domain.model.settings.AppTheme
import org.pakicek.monoforecast.domain.model.settings.BleMode
import org.pakicek.monoforecast.domain.model.settings.CacheDuration
import org.pakicek.monoforecast.domain.model.settings.GnssInterval
import org.pakicek.monoforecast.domain.model.settings.UserActivity
import org.pakicek.monoforecast.domain.model.settings.WeatherApi

interface SettingsRepository {
    fun isTracking(): Boolean
    fun getTrackingStartTime(): Long
    fun setTrackingStartTime(timestamp: Long)
    fun setTrackingState(isTracking: Boolean)
    fun getTheme(): AppTheme
    fun saveTheme(theme: AppTheme)
    fun getApi(): WeatherApi
    fun saveApi(api: WeatherApi)
    fun getActivity(): UserActivity
    fun saveActivity(activity: UserActivity)
    fun getCacheDuration(): CacheDuration
    fun saveCacheDuration(duration: CacheDuration)
    fun getBleMode(): BleMode
    fun saveBleMode(mode: BleMode)
    fun getAllSettings(): List<SettingsBlockEntity>
    fun getGnssInterval(): GnssInterval
    fun saveGnssInterval(interval: GnssInterval)
}