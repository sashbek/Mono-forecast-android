package org.pakicek.monoforecast.domain.model.dto.enums

enum class GnssInterval(val ms: Long, val displayName: String) {
    FAST(10000, "Fast (10 sec)"),
    NORMAL(60000, "Normal (1 min)"),
    SLOW(300000, "Slow (5 min)")
}