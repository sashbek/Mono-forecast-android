package org.pakicek.monoforecast.domain.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.pakicek.monoforecast.domain.model.dto.logs.FileEntity
import org.pakicek.monoforecast.domain.model.dto.logs.LogFrameEntity
import org.pakicek.monoforecast.domain.model.dto.logs.SettingsBlockEntity

@Dao
interface LogsDao {
    @Insert
    suspend fun insertLog(log: LogFrameEntity): Long

    @Query("SELECT * FROM logs ORDER BY timestamp ASC")
    fun getAllLogs(): Flow<List<LogFrameEntity>>

    @Query("DELETE FROM logs")
    suspend fun clearLogsTable()

    @Query("DELETE FROM files")
    suspend fun clearFilesTable()

    @Query("DELETE FROM settings")
    suspend fun clearSettingsTable()

    @Transaction
    suspend fun clearAllData() {
        clearSettingsTable()
        clearLogsTable()
        clearFilesTable()
    }

    @Insert
    suspend fun insertFile(file: FileEntity)

    @Query("SELECT * FROM files ORDER BY id DESC")
    fun getAllFilesFlow(): Flow<List<FileEntity>>

    @Query("SELECT * FROM files ORDER BY id DESC LIMIT 1")
    suspend fun getLastFile(): FileEntity?

    @Update
    suspend fun updateFile(file: FileEntity)

    @Insert
    suspend fun insertSettings(log: SettingsBlockEntity)

    @Transaction
    suspend fun insertLogWithSettings(
        log: LogFrameEntity,
        settings: SettingsBlockEntity
    ) {
        val logId = insertLog(log)
        settings.logId = logId
        insertSettings(settings)
    }

    @Query("SELECT * FROM settings")
    suspend fun getAllSettings(): List<SettingsBlockEntity>

    @Query("SELECT s.* FROM settings AS s " + "JOIN logs AS l ON s.logId = l.id " + "WHERE l.timestamp BETWEEN :start AND :end")
    suspend fun getSettingsByTime(start: Long, end: Long): List<SettingsBlockEntity>
}