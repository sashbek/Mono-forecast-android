package org.pakicek.monoforecast.domain.model.ble

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class WheelMetrics(
    val timestamp: Long = Date().time,
    val speed: Float = 0f,
    val batteryLevel: Int = 0,
    val voltage: Float = 0f,
    val current: Float = 0f,
    val temperature: Float = 0f,
    val distance: Float = 0f,
    val odometer: Float = 0f,
    val errorCode: Int = 0,
    val rawData: ByteArray? = null
) : Parcelable