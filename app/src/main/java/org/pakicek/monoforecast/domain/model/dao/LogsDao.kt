package org.pakicek.monoforecast.domain.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import org.pakicek.monoforecast.domain.model.dto.enums.LogType
import org.pakicek.monoforecast.domain.model.dto.logs.FileEntity
import org.pakicek.monoforecast.domain.model.dto.logs.LogFrameEntity
import org.pakicek.monoforecast.domain.model.dto.logs.SettingsBlockEntity

@Dao
interface LogsDao {
    // Logs
    @Insert
    suspend fun insertLog(log: LogFrameEntity)

    @Query("SELECT * FROM logs ORDER BY timestamp DESC")
    suspend fun getAllLogs(): List<LogFrameEntity>

    @Query("SELECT * FROM logs WHERE type = :type ORDER BY timestamp DESC")
    suspend fun getLogsByType(type: LogType): List<LogFrameEntity>

    @Query("DELETE FROM logs")
    suspend fun clearLogs()

    // Files
    @Insert
    suspend fun insertFile(log: FileEntity)

    @Query("SELECT * FROM files")
    suspend fun getAllFiles(): List<FileEntity>

    // Settings
    @Insert
    suspend fun insertSettings(log: SettingsBlockEntity)

    @Query("SELECT * FROM settings")
    suspend fun getAllSettings(): List<SettingsBlockEntity>

    @Query("SELECT s.logId, s.setting, s.value FROM settings AS s " +
            "JOIN logs AS l ON s.logId = l.id " +
            "WHERE l.timestamp BETWEEN :start AND :end")
    suspend fun getSettingsByTime(start: Long, end: Long): List<SettingsBlockEntity>
}