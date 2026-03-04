package org.pakicek.monoforecast.domain.repositories

import android.content.Context
import kotlinx.coroutines.flow.Flow
import org.pakicek.monoforecast.domain.model.dao.LogsDb
import org.pakicek.monoforecast.domain.model.dto.enums.LogType
import org.pakicek.monoforecast.domain.model.dto.logs.FileEntity
import org.pakicek.monoforecast.domain.model.dto.logs.LogFrameEntity
import org.pakicek.monoforecast.domain.model.dto.logs.SettingsBlockEntity

class LogsRepository(context: Context) {

    private val dao = LogsDb.getInstance(context).logsDao()

    suspend fun isLoggingActive(): Boolean {
        val last = dao.getLastFile() ?: return false
        return last.end == null
    }

    suspend fun insertSetting(setting: String, value: String) {
        if (!isLoggingActive()) return
        val log = LogFrameEntity(type = LogType.SETTINGS, message = "$setting = $value")
        val settingBlock = SettingsBlockEntity(null, setting, value)
        dao.insertLogWithSettings(log, settingBlock)
    }

    suspend fun addLog(type: LogType, message: String) {
        val log = LogFrameEntity(type = type, message = message)
        dao.insertLog(log)
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

    fun getAllLogs(): Flow<List<LogFrameEntity>> = dao.getAllLogs()

    fun getAllFiles(): Flow<List<FileEntity>> = dao.getAllFilesFlow()

    suspend fun clearAll() {
        dao.clearAllData()
    }
}