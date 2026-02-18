package org.pakicek.monoforecast.domain.model.dto.converters

import androidx.room.TypeConverter
import org.pakicek.monoforecast.domain.model.dto.enums.LogType

class LogTypeConverter {

    @TypeConverter
    fun fromLogType(type: LogType): Int {
        return type.code
    }

    @TypeConverter
    fun toLogType(code: Int): LogType {
        return LogType.fromCode(code)
    }
}
