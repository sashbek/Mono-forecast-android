package org.pakicek.monoforecast.domain.repository

import org.pakicek.monoforecast.data.entities.SettingsBlockEntity
import org.pakicek.monoforecast.domain.model.dto.enums.AppTheme
import org.pakicek.monoforecast.domain.model.dto.enums.CacheDuration
import org.pakicek.monoforecast.domain.model.dto.enums.GnssInterval
import org.pakicek.monoforecast.domain.model.dto.enums.UserActivity
import org.pakicek.monoforecast.domain.model.dto.enums.WeatherApi

interface ISettingsRepository {
    fun isTracking(): Boolean
    fun getTrackingStartTime(): Long
    fun setTrackingStartTime(timestamp: Long)
    fun getTheme(): AppTheme
    fun saveTheme(theme: AppTheme)
    fun getApi(): WeatherApi
    fun saveApi(api: WeatherApi)
    fun getActivity(): UserActivity
    fun saveActivity(activity: UserActivity)
    fun getCacheDuration(): CacheDuration
    fun saveCacheDuration(duration: CacheDuration)
    fun getAllSettings(): List<SettingsBlockEntity>
    fun getGnssInterval(): GnssInterval
    fun saveGnssInterval(interval: GnssInterval)
    fun setTrackingState(isTracking: Boolean)
}