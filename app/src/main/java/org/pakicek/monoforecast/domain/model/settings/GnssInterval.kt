package org.pakicek.monoforecast.domain.model.settings

enum class GnssInterval(val ms: Long, val displayName: String) {
    FAST(2000, "Fast (2 sec)"),
    NORMAL(5000, "Normal (5 sec)"),
    ECO(15000, "Eco (15 sec)"),
    SLOW(60000, "Slow (1 min)")
}