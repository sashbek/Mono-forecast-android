package org.pakicek.monoforecast.domain.model.ble

import android.bluetooth.BluetoothDevice
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WheelDevice(
    val address: String,
    val name: String,
    val rssi: Int,
    val bondState: Int,
    var adapterType: String = "unknown",
    val bluetoothDevice: BluetoothDevice? = null
) : Parcelable