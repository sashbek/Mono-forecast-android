package org.pakicek.monoforecast.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.pakicek.monoforecast.data.local.dao.LogsDao
import org.pakicek.monoforecast.data.local.entity.FileEntity
import org.pakicek.monoforecast.data.local.entity.LocationBlockEntity
import org.pakicek.monoforecast.data.local.entity.LogFrameEntity
import org.pakicek.monoforecast.data.local.entity.SettingsBlockEntity
import org.pakicek.monoforecast.data.local.entity.WeatherBlockEntity
import org.pakicek.monoforecast.data.remote.dto.WeatherResponseDto
import org.pakicek.monoforecast.domain.model.settings.LogType
import org.pakicek.monoforecast.domain.repository.LogsRepository
import org.pakicek.monoforecast.data.local.entity.LogWithDetails

class LogsRepositoryImpl(private val dao: LogsDao) : LogsRepository {

    override suspend fun isLoggingActive(): Boolean {
        val last = dao.getLastFile() ?: return false
        return last.end == null
    }

    override suspend fun getLogsForSession(fileId: Long): Flow<List<LogWithDetails>> {
        val file = dao.getFileById(fileId) ?: return emptyFlow()
        return dao.getLogsByTimeRange(file.start, file.end)
    }

    override suspend fun insertSetting(setting: String, value: String) {
        if (!isLoggingActive()) return
        val log = LogFrameEntity(type = LogType.SETTINGS, message = "$setting = $value")
        val settingBlock = SettingsBlockEntity(null, setting, value)
        dao.insertLogWithSettings(log, settingBlock)
    }

    override suspend fun saveLocationLog(lat: Double, lon: Double) {
        if (!isLoggingActive()) return

        val logFrame = LogFrameEntity(type = LogType.LOCATION, message = "")
        val locationBlock = LocationBlockEntity(logId = 0, latitude = lat, longitude = lon)
        dao.insertLocationLog(logFrame, locationBlock)
    }

    override suspend fun saveWeatherLog(dto: WeatherResponseDto) {
        if (!isLoggingActive()) return

        val logFrame = LogFrameEntity(type = LogType.WEATHER, message = "")
        val weatherBlock = WeatherBlockEntity(
            logId = 0,
            tempC = dto.main.temp,
            visibilityMeters = 10000,
            windSpeedMs = dto.wind.speed,
            windDir = dto.wind.direction,
            rainMm = 0.0
        )
        dao.insertWeatherLog(logFrame, weatherBlock)
    }

    override suspend fun startNewFile() {
        val file = FileEntity(start = System.currentTimeMillis())
        dao.insertFile(file)
    }

    override suspend fun endLastFile() {
        val file = dao.getLastFile() ?: return
        if (file.end == null) {
            file.end = System.currentTimeMillis()
            dao.updateFile(file)
        }
    }

    override fun getAllLogs(): Flow<List<LogFrameEntity>> = dao.getAllLogs()

    override fun getAllFiles(): Flow<List<FileEntity>> = dao.getAllFilesFlow()

    override suspend fun clearAll() {
        dao.clearAllData()
    }
}