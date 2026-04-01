package org.pakicek.monoforecast.data.ble.protocol

import org.pakicek.monoforecast.domain.model.ble.WheelMetrics
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TestWheelAdapter : WheelProtocolAdapter() {

    override fun getAdapterType(): String = "test"

    override fun parseData(data: ByteArray): WheelMetrics? {
        if (data.size < 20) return null

        return try {
            val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

            val speed = buffer.getFloat(0)
            val voltage = buffer.getFloat(4)
            val current = buffer.getFloat(8)
            val batteryLevel = data[12].toInt() and 0xFF
            val temperature = buffer.getFloat(13)
            val errorCode = (data[17].toInt() and 0xFF) or ((data[18].toInt() and 0xFF) shl 8)

            var checksum = 0
            for (i in 0 until 19) {
                checksum = (checksum + data[i]) and 0xFF
            }

            if (checksum == (data[19].toInt() and 0xFF)) {
                WheelMetrics(
                    speed = speed,
                    batteryLevel = batteryLevel,
                    voltage = voltage,
                    current = current,
                    temperature = temperature,
                    errorCode = errorCode
                )
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}