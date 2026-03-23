package org.pakicek.monoforecast.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.domain.model.dto.enums.AppTheme
import org.pakicek.monoforecast.domain.model.dto.enums.CacheDuration
import org.pakicek.monoforecast.domain.model.dto.enums.GnssInterval
import org.pakicek.monoforecast.domain.model.dto.enums.UserActivity
import org.pakicek.monoforecast.domain.model.dto.enums.WeatherApi
import org.pakicek.monoforecast.domain.repository.ILogsRepository
import org.pakicek.monoforecast.domain.repository.ISettingsRepository

class SettingsViewModel(
    private val repository: ISettingsRepository,
    private val logsRepository: ILogsRepository
) : ViewModel() {

    private fun logSettingChange(key: String, value: String) {
        viewModelScope.launch {
            logsRepository.insertSetting(key, value)
        }
    }

    fun getTheme(): AppTheme = repository.getTheme()

    fun saveTheme(theme: AppTheme) {
        repository.saveTheme(theme)
        logSettingChange("Theme", theme.name)
    }

    fun getApi(): WeatherApi = repository.getApi()

    fun saveApi(api: WeatherApi) {
        repository.saveApi(api)
        logSettingChange("API", api.name)
    }

    fun getActivity(): UserActivity = repository.getActivity()

    fun saveActivity(activity: UserActivity) {
        repository.saveActivity(activity)
        logSettingChange("Activity", activity.name)
    }

    fun getCacheDuration(): CacheDuration = repository.getCacheDuration()

    fun saveCacheDuration(duration: CacheDuration) {
        repository.saveCacheDuration(duration)
        logSettingChange("Cache Duration", duration.name)
    }

    fun getGnssInterval() = repository.getGnssInterval()

    fun saveGnssInterval(interval: GnssInterval) {
        repository.saveGnssInterval(interval)
        logSettingChange("GNSS Interval", interval.displayName)
    }
}