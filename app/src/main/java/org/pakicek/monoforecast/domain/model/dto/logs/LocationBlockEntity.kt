package org.pakicek.monoforecast.domain.model.dto.logs

import java.time.ZonedDateTime
import java.time.Instant
import java.time.ZoneId

data class LocationBlockEntity(var lat: Double, var lon: Double) {
    var timestamp: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC"));

    constructor(nmeaString: String) : this(
        (nmeaString.split(",")[0]).toDoubleOrNull() ?: 0.0,
        (nmeaString.split(",")[2]).toDoubleOrNull() ?: 0.0) {}
}
