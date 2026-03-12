package org.pakicek.monoforecast.domain.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.pakicek.monoforecast.domain.model.dto.logs.FileEntity
import org.pakicek.monoforecast.domain.model.dto.logs.LogFrameEntity
import org.pakicek.monoforecast.domain.model.dto.logs.LogWithDetails
import org.pakicek.monoforecast.domain.model.dto.logs.SettingsBlockEntity
import org.pakicek.monoforecast.domain.model.dto.logs.WeatherBlockEntity

@Dao
interface LogsDao {
    @Insert
    suspend fun insertLog(log: LogFrameEntity): Long

    @Query("SELECT * FROM logs ORDER BY timestamp ASC")
    fun getAllLogs(): Flow<List<LogFrameEntity>>

    @Transaction
    @Query("SELECT * FROM logs WHERE id IN (SELECT id FROM logs) ORDER BY timestamp ASC")
    fun getAllLogsWithDetails(): Flow<List<LogWithDetails>>

    @Transaction
    @Query("SELECT * FROM logs ORDER BY timestamp ASC")
    fun getLogsWithDetailsFlow(): Flow<List<LogWithDetails>>

    @Query("DELETE FROM logs")
    suspend fun clearLogsTable()

    @Insert
    suspend fun insertWeatherBlock(block: WeatherBlockEntity)

    @Transaction
    suspend fun insertWeatherLog(log: LogFrameEntity, weather: WeatherBlockEntity) {
        val id = insertLog(log)
        val blockWithId = weather.copy(logId = id)
        insertWeatherBlock(blockWithId)
    }

    @Insert
    suspend fun insertFile(file: FileEntity)

    @Update
    suspend fun updateFile(file: FileEntity)

    @Query("SELECT * FROM files ORDER BY id DESC LIMIT 1")
    suspend fun getLastFile(): FileEntity?

    @Query("SELECT * FROM files ORDER BY id DESC")
    fun getAllFilesFlow(): Flow<List<FileEntity>>

    @Query("DELETE FROM files")
    suspend fun clearFilesTable()

    @Insert
    suspend fun insertSettings(settings: SettingsBlockEntity)

    @Query("SELECT * FROM settings")
    suspend fun getAllSettings(): List<SettingsBlockEntity>

    @Query("SELECT s.* FROM settings AS s JOIN logs AS l ON s.logId = l.id WHERE l.timestamp BETWEEN :start AND :end")
    suspend fun getSettingsByTime(start: Long, end: Long): List<SettingsBlockEntity>

    @Query("DELETE FROM settings")
    suspend fun clearSettingsTable()

    @Transaction
    suspend fun insertLogWithSettings(log: LogFrameEntity, settings: SettingsBlockEntity) {
        val logId = insertLog(log)
        settings.logId = logId
        insertSettings(settings)
    }

    @Transaction
    suspend fun clearAllData() {
        clearSettingsTable()
        clearLogsTable()
        clearFilesTable()
    }
}