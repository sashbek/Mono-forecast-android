package org.pakicek.monoforecast.domain.model.settings

enum class CacheDuration(val milliseconds: Long) {
    ALWAYS_UPDATE(0),
    MIN_15(15 * 60 * 1000),
    HOUR_1(60 * 60 * 1000),
    HOUR_3(3 * 60 * 60 * 1000)
}