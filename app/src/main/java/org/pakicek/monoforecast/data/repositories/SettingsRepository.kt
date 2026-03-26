package org.pakicek.monoforecast.data.repositories

import android.content.Context
import androidx.core.content.edit
import org.pakicek.monoforecast.data.entities.SettingsBlockEntity
import org.pakicek.monoforecast.domain.model.dto.enums.AppTheme
import org.pakicek.monoforecast.domain.model.dto.enums.CacheDuration
import org.pakicek.monoforecast.domain.model.dto.enums.GnssInterval
import org.pakicek.monoforecast.domain.model.dto.enums.UserActivity
import org.pakicek.monoforecast.domain.model.dto.enums.WeatherApi
import org.pakicek.monoforecast.domain.repository.ISettingsRepository

class SettingsRepository(context: Context) : ISettingsRepository {

    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME = "KEY_THEME"
        private const val KEY_API = "KEY_API"
        private const val KEY_ACTIVITY = "KEY_ACTIVITY"
        private const val KEY_CACHE = "KEY_CACHE"
        private const val KEY_GNSS_INTERVAL = "KEY_GNSS_INTERVAL"
        private const val KEY_IS_TRACKING = "KEY_IS_TRACKING"
        private const val KEY_TRACKING_START_TIME = "KEY_TRACKING_START_TIME"
    }

    override fun getTheme(): AppTheme {
        val stored = prefs.getString(KEY_THEME, "SYSTEM")
        return AppTheme.valueOf(stored ?: "SYSTEM")
    }

    override fun saveTheme(theme: AppTheme) = prefs.edit { putString(KEY_THEME, theme.name) }

    override fun getApi(): WeatherApi {
        val stored = prefs.getString(KEY_API, "NINJA_API")
        return WeatherApi.valueOf(stored ?: "NINJA_API")
    }

    override fun saveApi(api: WeatherApi) = prefs.edit { putString(KEY_API, api.name) }

    override fun getActivity(): UserActivity {
        val stored = prefs.getString(KEY_ACTIVITY, "BIKE")
        return UserActivity.valueOf(stored ?: "BIKE")
    }

    override fun saveActivity(activity: UserActivity) = prefs.edit { putString(KEY_ACTIVITY, activity.name) }

    override fun getCacheDuration(): CacheDuration {
        val stored = prefs.getString(KEY_CACHE, CacheDuration.ALWAYS_UPDATE.name)
        return try {
            CacheDuration.valueOf(stored ?: CacheDuration.ALWAYS_UPDATE.name)
        } catch (e: Exception) {
            CacheDuration.ALWAYS_UPDATE
        }
    }

    override fun saveCacheDuration(duration: CacheDuration) = prefs.edit { putString(KEY_CACHE, duration.name) }

    override fun getGnssInterval(): GnssInterval {
        val name = prefs.getString(KEY_GNSS_INTERVAL, GnssInterval.NORMAL.name)
        return try {
            GnssInterval.valueOf(name ?: GnssInterval.NORMAL.name)
        } catch (e: Exception) {
            GnssInterval.NORMAL
        }
    }

    override fun saveGnssInterval(interval: GnssInterval) = prefs.edit { putString(KEY_GNSS_INTERVAL, interval.name) }

    override fun setTrackingState(isTracking: Boolean) = prefs.edit { putBoolean(KEY_IS_TRACKING, isTracking) }

    override fun isTracking(): Boolean = prefs.getBoolean(KEY_IS_TRACKING, false)

    override fun setTrackingStartTime(timestamp: Long) = prefs.edit { putLong(KEY_TRACKING_START_TIME, timestamp) }

    override fun getTrackingStartTime(): Long = prefs.getLong(KEY_TRACKING_START_TIME, 0L)

    override fun getAllSettings(): List<SettingsBlockEntity> {
        return listOf(
            SettingsBlockEntity(null, KEY_THEME, getTheme().name),
            SettingsBlockEntity(null, KEY_API, getApi().name),
            SettingsBlockEntity(null, KEY_ACTIVITY, getActivity().name)
        )
    }
}