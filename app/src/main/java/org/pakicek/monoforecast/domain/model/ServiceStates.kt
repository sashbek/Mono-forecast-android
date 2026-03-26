package org.pakicek.monoforecast.domain.model

enum class GnssState {
    STOPPED,
    RUNNING,
    LOST
}

enum class BleState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}