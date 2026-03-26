package org.pakicek.monoforecast.domain.model.dto.ble

import android.bluetooth.BluetoothDevice
import android.os.Parcel
import android.os.Parcelable

data class WheelDevice(
    val address: String,
    val name: String,
    val rssi: Int,
    val bondState: Int,
    var adapterType: String = "unknown",
    var bluetoothDevice: BluetoothDevice? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString() ?: "unknown",
        parcel.readParcelable(BluetoothDevice::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(address)
        parcel.writeString(name)
        parcel.writeInt(rssi)
        parcel.writeInt(bondState)
        parcel.writeString(adapterType)
        parcel.writeParcelable(bluetoothDevice, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<WheelDevice> {
        override fun createFromParcel(parcel: Parcel): WheelDevice {
            return WheelDevice(parcel)
        }

        override fun newArray(size: Int): Array<WheelDevice?> {
            return arrayOfNulls(size)
        }
    }
}