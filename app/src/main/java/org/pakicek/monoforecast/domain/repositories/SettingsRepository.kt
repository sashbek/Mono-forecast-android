package org.pakicek.monoforecast.domain.repositories

import android.content.Context
import org.pakicek.monoforecast.domain.model.dto.enums.AppTheme
import org.pakicek.monoforecast.domain.model.dto.enums.UserActivity
import org.pakicek.monoforecast.domain.model.dto.enums.WeatherApi
import androidx.core.content.edit
import org.pakicek.monoforecast.domain.model.dto.enums.CacheDuration
import org.pakicek.monoforecast.domain.model.dto.logs.SettingsBlockEntity

class SettingsRepository(context: Context) {

    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME = "KEY_THEME"
        private const val KEY_API = "KEY_API"
        private const val KEY_ACTIVITY = "KEY_ACTIVITY"
        private const val KEY_CACHE = "KEY_CACHE"
    }

    fun getTheme(): AppTheme {
        val stored = prefs.getString(KEY_THEME, "SYSTEM")
        return AppTheme.valueOf(stored ?: "SYSTEM")
    }

    fun saveTheme(theme: AppTheme) {
        prefs.edit {
            putString(KEY_THEME, theme.name)
        }
    }

    fun getApi(): WeatherApi {
        val stored = prefs.getString(KEY_API, "NINJA_API")
        return WeatherApi.valueOf(stored ?: "NINJA_API")
    }

    fun saveApi(api: WeatherApi) {
        prefs.edit {
            putString(KEY_API, api.name)
        }
    }

    fun getActivity(): UserActivity {
        val stored = prefs.getString(KEY_ACTIVITY, "BIKE")
        return UserActivity.valueOf(stored ?: "BIKE")
    }

    fun saveActivity(activity: UserActivity) {
        prefs.edit {
            putString(KEY_ACTIVITY, activity.name)
        }
    }

    fun getCacheDuration(): CacheDuration {
        val stored = prefs.getString(KEY_CACHE, CacheDuration.MIN_15.name)
        return try {
            CacheDuration.valueOf(stored ?: CacheDuration.MIN_15.name)
        } catch (e: Exception) {
            CacheDuration.MIN_15
        }
    }

    fun saveCacheDuration(duration: CacheDuration) {
        prefs.edit {
            putString(KEY_CACHE, duration.name)
        }
    }

    fun getAllSettings(): List<SettingsBlockEntity> {
        val theme = SettingsBlockEntity(null, KEY_THEME, getTheme().name)
        val api = SettingsBlockEntity(null, KEY_API, getApi().name)
        val activity = SettingsBlockEntity(null, KEY_ACTIVITY, getActivity().name)

        return listOf(theme, api, activity)
    }
}