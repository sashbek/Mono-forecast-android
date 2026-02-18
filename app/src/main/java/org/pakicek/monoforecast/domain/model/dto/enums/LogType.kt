package org.pakicek.monoforecast.domain.model.dto.enums

enum class LogType(val code: Int) {
    DEVICE_METRICS(0),
    LOCATION(1),
    WEATHER(2),
    SETTINGS(3);

    companion object {
        fun fromCode(code: Int): LogType
        {
            return entries.first { it.code == code }
        }
    }
}