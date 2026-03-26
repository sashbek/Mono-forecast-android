package org.pakicek.monoforecast.domain.model.dto.ble

import java.util.Date
import android.os.Parcel
import android.os.Parcelable

data class WheelMetrics(
    val timestamp: Date = Date(),
    val speed: Float = 0f,           // км/ч
    val batteryLevel: Int = 0,        // %
    val voltage: Float = 0f,          // В
    val current: Float = 0f,          // А
    val temperature: Float = 0f,      // °C
    val distance: Float = 0f,         // км
    val odometer: Float = 0f,         // км
    val errorCode: Int = 0,
    val rawData: ByteArray? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        Date(parcel.readLong()),
        parcel.readFloat(),
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readInt(),
        parcel.createByteArray()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(timestamp.time)
        parcel.writeFloat(speed)
        parcel.writeInt(batteryLevel)
        parcel.writeFloat(voltage)
        parcel.writeFloat(current)
        parcel.writeFloat(temperature)
        parcel.writeFloat(distance)
        parcel.writeFloat(odometer)
        parcel.writeInt(errorCode)
        parcel.writeByteArray(rawData)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<WheelMetrics> {
        override fun createFromParcel(parcel: Parcel): WheelMetrics {
            return WheelMetrics(parcel)
        }

        override fun newArray(size: Int): Array<WheelMetrics?> {
            return arrayOfNulls(size)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WheelMetrics

        if (timestamp != other.timestamp) return false
        if (speed != other.speed) return false
        if (batteryLevel != other.batteryLevel) return false
        if (voltage != other.voltage) return false
        if (current != other.current) return false
        if (temperature != other.temperature) return false
        if (distance != other.distance) return false
        if (odometer != other.odometer) return false
        if (errorCode != other.errorCode) return false
        if (rawData != null) {
            if (other.rawData == null) return false
            if (!rawData.contentEquals(other.rawData)) return false
        } else if (other.rawData != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + speed.hashCode()
        result = 31 * result + batteryLevel
        result = 31 * result + voltage.hashCode()
        result = 31 * result + current.hashCode()
        result = 31 * result + temperature.hashCode()
        result = 31 * result + distance.hashCode()
        result = 31 * result + odometer.hashCode()
        result = 31 * result + errorCode
        result = 31 * result + (rawData?.contentHashCode() ?: 0)
        return result
    }
}