package org.pakicek.monoforecast.domain.repository

interface ISettingsRepository {
    fun isTracking(): Boolean
    fun getTrackingStartTime(): Long
    fun setTrackingStartTime(timestamp: Long)
}