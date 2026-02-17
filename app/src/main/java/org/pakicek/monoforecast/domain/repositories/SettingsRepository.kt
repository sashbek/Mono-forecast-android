package org.pakicek.monoforecast.domain.repositories

import android.content.Context
import org.pakicek.monoforecast.domain.model.AppTheme
import org.pakicek.monoforecast.domain.model.UserActivity
import org.pakicek.monoforecast.domain.model.WeatherApi
import androidx.core.content.edit

class SettingsRepository(context: Context) {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun getTheme(): AppTheme {
        val stored = prefs.getString("theme_pref", "SYSTEM")
        val value = AppTheme.valueOf(stored ?: "SYSTEM")
        return value
    }

    fun saveTheme(theme: AppTheme) {
        val value = theme.name
        prefs.edit {
            putString("theme_pref", value)
        }
    }

    fun getApi(): WeatherApi {
        val stored = prefs.getString("api_pref", "NINJA_API")
        val value = WeatherApi.valueOf(stored ?: "NINJA_API")
        return value
    }

    fun saveApi(api: WeatherApi) {
        val value = api.name
        prefs.edit {
            putString("api_pref", value)
        }
    }

    fun getActivity(): UserActivity {
        val stored = prefs.getString("activity_pref", "BIKE")
        val value = UserActivity.valueOf(stored ?: "BIKE")
        return value
    }

    fun saveActivity(activity: UserActivity) {
        val value = activity.name
        prefs.edit {
            putString("activity_pref", value)
        }
    }
}