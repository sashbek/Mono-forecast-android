package org.pakicek.monoforecast.domain.repository

import kotlinx.coroutines.flow.Flow
import org.pakicek.monoforecast.data.local.entity.FileEntity
import org.pakicek.monoforecast.data.local.entity.LogFrameEntity
import org.pakicek.monoforecast.data.local.entity.LogWithDetails
import org.pakicek.monoforecast.data.remote.dto.WeatherResponseDto

interface LogsRepository {
    suspend fun isLoggingActive(): Boolean
    suspend fun insertSetting(setting: String, value: String)
    suspend fun startNewFile()
    suspend fun endLastFile()

    fun getAllLogs(): Flow<List<LogFrameEntity>>
    fun getAllFiles(): Flow<List<FileEntity>>
    suspend fun getLogsForSession(fileId: Long): Flow<List<LogWithDetails>>

    suspend fun saveLocationLog(lat: Double, lon: Double)
    suspend fun saveWeatherLog(dto: WeatherResponseDto)
    suspend fun clearAll()
}