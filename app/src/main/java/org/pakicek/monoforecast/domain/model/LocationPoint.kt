package org.pakicek.monoforecast.domain.model

class LocationPoint {
    var lat: Double
    var lon: Double
    var timestamp: Long

    constructor(lat: Double, lon: Double) {
        this.lat = lat
        this.lon = lon
        this.timestamp = System.currentTimeMillis()
    }

    constructor(nmeaString: String) {
        val parts = nmeaString.split(",")
        if (parts.size >= 2) {
            this.lat = parts[0].toDoubleOrNull() ?: 0.0
            this.lon = parts[1].toDoubleOrNull() ?: 0.0
        } else {
            this.lat = 0.0
            this.lon = 0.0
        }
        this.timestamp = System.currentTimeMillis()
    }
}