package com.example.datalogger.viewmodel

import android.bluetooth.BluetoothDevice
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class BleViewModel : ViewModel() {
    var selectedDevice: BluetoothDevice? = null

    // This will be observed in your Composable
    var receivedText by mutableStateOf("Waiting for data...")
        private set

    fun updateReceivedText(text: String) {
        receivedText = text
    }
}
