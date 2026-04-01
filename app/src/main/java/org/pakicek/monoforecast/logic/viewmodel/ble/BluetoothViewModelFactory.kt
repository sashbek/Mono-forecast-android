package org.pakicek.monoforecast.logic.viewmodel.ble

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BluetoothViewModelFactory() : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BluetoothViewModel::class.java)) {
            return BluetoothViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}