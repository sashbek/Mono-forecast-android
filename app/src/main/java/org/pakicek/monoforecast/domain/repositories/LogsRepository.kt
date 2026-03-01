package org.pakicek.monoforecast.domain.repositories

import org.pakicek.monoforecast.domain.model.dao.LogsDao
import org.pakicek.monoforecast.domain.model.dto.enums.LogType
import org.pakicek.monoforecast.domain.model.dto.logs.FileEntity
import org.pakicek.monoforecast.domain.model.dto.logs.LogFrameEntity
import org.pakicek.monoforecast.domain.model.dto.logs.SettingsBlockEntity
import kotlin.math.log

class LogsRepository private constructor(private val dao: LogsDao) {

    suspend fun isLoggingActive(): Boolean {
        val last = dao.getLastFile() ?: return false
        return last.end == null
    }

    suspend fun insertSetting(setting: String, value: String) {
        if (!isLoggingActive()) {
            return
        }

        val log = LogFrameEntity(type = LogType.SETTINGS)
        val settingBlock = SettingsBlockEntity(null, setting, value)
        dao.insertLogWithSettings(log, settingBlock)
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

    suspend fun getAllLogs(): List<LogFrameEntity> {
        return dao.getAllLogs()
    }

    suspend fun clearLogs() {
        dao.clearLogs()
    }

    companion object {
        @Volatile
        private var INSTANCE: LogsRepository? = null

        fun getInstance(dao: LogsDao): LogsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LogsRepository(dao).also { INSTANCE = it }
            }
        }
    }
}