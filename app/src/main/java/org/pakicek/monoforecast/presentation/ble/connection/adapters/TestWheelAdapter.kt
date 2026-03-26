package org.pakicek.monoforecast.presentation.ble.connection.adapters

import org.pakicek.monoforecast.presentation.ble.connection.models.WheelMetrics
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TestWheelAdapter : WheelProtocolAdapter() {

    override fun getAdapterType(): String = "test"

    override fun parseData(data: ByteArray): WheelMetrics? {
        if (data.size < 20) return null

        return try {
            // Простой тестовый протокол:
            // bytes 0-3: speed (float)
            // bytes 4-7: voltage (float)
            // bytes 8-11: current (float)
            // bytes 12: battery level
            // bytes 13-16: temperature (float)
            // bytes 17-18: error code
            // bytes 19: checksum

            val buffer = ByteBuffer.wrap(data)
            buffer.order(ByteOrder.LITTLE_ENDIAN)

            val speed = buffer.getFloat(0)
            val voltage = buffer.getFloat(4)
            val current = buffer.getFloat(8)
            val batteryLevel = data[12].toInt() and 0xFF
            val temperature = buffer.getFloat(13)
            val errorCode = (data[17].toInt() and 0xFF) or ((data[18].toInt() and 0xFF) shl 8)

            // Проверка контрольной суммы
            var checksum = 0
            for (i in 0 until 19) {
                checksum = (checksum + data[i]) and 0xFF
            }

            if (checksum == data[19].toInt() and 0xFF) {
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
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}