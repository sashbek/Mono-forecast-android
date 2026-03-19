package org.pakicek.monoforecast.domain.repositories

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.pakicek.monoforecast.domain.model.dao.LogsDb
import org.pakicek.monoforecast.domain.model.dto.WeatherResponseDto
import org.pakicek.monoforecast.domain.model.dto.enums.LogType
import org.pakicek.monoforecast.domain.model.dto.logs.FileEntity
import org.pakicek.monoforecast.domain.model.dto.logs.LogFrameEntity
import org.pakicek.monoforecast.domain.model.dto.logs.LogWithDetails
import org.pakicek.monoforecast.domain.model.dto.logs.SettingsBlockEntity
import org.pakicek.monoforecast.domain.model.dto.logs.WeatherBlockEntity

class LogsRepository(context: Context) {

    private val dao = LogsDb.getInstance(context).logsDao()

    suspend fun isLoggingActive(): Boolean {
        val last = dao.getLastFile() ?: return false
        return last.end == null
    }

    suspend fun getLogsForSession(fileId: Long): Flow<List<LogWithDetails>> {
        val file = dao.getFileById(fileId) ?: return emptyFlow()
        return dao.getLogsByTimeRange(file.start, file.end)
    }

    suspend fun insertSetting(setting: String, value: String) {
        if (!isLoggingActive()) return
        val log = LogFrameEntity(type = LogType.SETTINGS, message = "$setting = $value")
        val settingBlock = SettingsBlockEntity(null, setting, value)
        dao.insertLogWithSettings(log, settingBlock)
    }

    suspend fun saveLocationLog(lat: Double, lon: Double) {
        if (!isLoggingActive()) return

        val logFrame = LogFrameEntity(
            type = LogType.LOCATION,
            message = ""
        )

        val locationBlock = org.pakicek.monoforecast.domain.model.dto.logs.LocationBlockEntity(
            logId = 0,
            latitude = lat,
            longitude = lon
        )

        dao.insertLocationLog(logFrame, locationBlock)
    }

    suspend fun saveWeatherLog(dto: WeatherResponseDto) {
        if (!isLoggingActive()) return

        val logFrame = LogFrameEntity(
            type = LogType.WEATHER,
            message = ""
        )

        val weatherBlock = WeatherBlockEntity(
            logId = 0,
            tempC = dto.main.temp,
            windSpeedMs = dto.wind.speed,
            windDir = dto.wind.direction,
            rainMm = 0.0,
            visibilityMeters = 10000
        )

        dao.insertWeatherLog(logFrame, weatherBlock)
    }
    suspend fun startNewFile() {
        val file = FileEntity(start = System.currentTimeMillis())
        dao.insertFile(file)
    }

    suspend fun endLastFile() {
        val file = dao.getLastFile() ?: return
        if (file.end == null) {
            file.end = System.currentTimeMillis()
            dao.updateFile(file)
        }
    }

    fun getAllFiles(): Flow<List<FileEntity>> = dao.getAllFilesFlow()

    suspend fun clearAll() {
        dao.clearAllData()
    }
}