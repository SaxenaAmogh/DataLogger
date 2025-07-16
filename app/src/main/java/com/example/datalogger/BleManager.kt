package com.example.datalogger

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.annotation.RequiresPermission

class BleManager(private val context: Context) {
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private var bluetoothGatt: BluetoothGatt? = null

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun scanDevices(onDeviceFound: (BluetoothDevice) -> Unit) {
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                onDeviceFound(result.device)
            }
        }
        scanner?.startScan(scanCallback)
        // stop scan after 10 seconds or when desired
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToDevice(device: BluetoothDevice, context: Context, onConnected: () -> Unit) {
        bluetoothGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    onConnected()
                    gatt.discoverServices()
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                // Services available
            }

            @Deprecated("Deprecated in Java")
            override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
                val value = characteristic.value
                // Process value
            }
        })
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
    }
}
