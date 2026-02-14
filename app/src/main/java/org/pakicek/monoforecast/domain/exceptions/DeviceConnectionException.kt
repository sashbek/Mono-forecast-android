package org.pakicek.monoforecast.domain.exceptions

class DeviceConnectionException(message: String, val errorCode: Int) : Exception(message)