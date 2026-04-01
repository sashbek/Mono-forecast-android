package org.pakicek.monoforecast.data.local.converter

import androidx.room.TypeConverter
import org.pakicek.monoforecast.domain.model.settings.LogType

class LogTypeConverter {

    @TypeConverter
    fun fromLogType(type: LogType): Int {
        return type.ordinal
    }

    @TypeConverter
    fun toLogType(code: Int): LogType {
        return LogType.entries.getOrElse(code) { LogType.DEVICE_METRICS }
    }
}