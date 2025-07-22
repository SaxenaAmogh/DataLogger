package com.example.datalogger.views

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.datalogger.ui.theme.AccentColor
import com.example.datalogger.ui.theme.Background
import com.example.datalogger.ui.theme.Primary
import com.example.datalogger.ui.theme.Secondary
import com.example.datalogger.ui.theme.latoFontFamily
import com.example.datalogger.viewmodel.BleViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ConnectionScreen(navController: NavController, bleViewModel: BleViewModel) {

    val contextZ = LocalContext.current
    var devices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    var connectedDevice by remember { mutableStateOf("") }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val view = LocalView.current
    val window = (view.context as? Activity)?.window
    val windowInsetsController = window?.let { WindowCompat.getInsetsController(it, view) }

    if (windowInsetsController != null) {
        windowInsetsController.isAppearanceLightStatusBars = true
    }

    //Checking and turning on Bluetooth
    val bluetoothManager: BluetoothManager? = getSystemService(contextZ, BluetoothManager::class.java)
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    var bluetoothGatt by remember { mutableStateOf<BluetoothGatt?>(null) }


    val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (ActivityCompat.checkSelfPermission(contextZ, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                super.onConnectionStateChange(gatt, status, newState)
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.d("BLE", "Connected to GATT server.")
                        gatt.discoverServices()
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(
                                contextZ,
                                "Connected to ${gatt.device.name}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    BluetoothProfile.STATE_DISCONNECTED -> {
                        Log.d("BLE", "Disconnected from GATT server.")
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(
                                contextZ,
                                "Disconnected from ${gatt.device.name}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BLE", "Services discovered: ${gatt.services}")
            } else {
                Log.w("BLE", "onServicesDiscovered received: $status")
            }
        }
    }

    val pairingReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (ActivityCompat.checkSelfPermission(contextZ, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                if (intent?.action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)

                    if (bondState == BluetoothDevice.BOND_BONDED) {
                        Log.d("BLE", "Paired with ${device?.name}")
                        // Connect immediately after pairing
                        bluetoothGatt?.close()
                        bluetoothGatt = device?.connectGatt(context, false, gattCallback)
                        connectedDevice = device?.name ?: "Unknown"
                        devices = emptyList()
                        navController.navigate("connected/$connectedDevice")
                    }
                }
                    }
            }
        }
    }

    DisposableEffect(Unit) {
        val bondFilter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        contextZ.registerReceiver(pairingReceiver, bondFilter)

        onDispose {
            contextZ.unregisterReceiver(pairingReceiver)
        }
    }

    val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.d("@@BLE", "onScanResult triggered")
            if (ActivityCompat.checkSelfPermission(contextZ, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                val device = result.device
                Log.d("@@BLE", "Found device: ${device.name} - ${device.address}")
                devices = if (devices.none { it.address == device.address }) {
                    devices + device
                } else devices
            }else {
                Log.e("BLE", "Bluetooth scan permission not granted")
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BLE", "Scan failed with error code $errorCode")
        }
    }

    val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
    var scanning by remember { mutableStateOf(false) }
    val handler = Handler(Looper.getMainLooper())
    val SCAN_PERIOD = 10000L

    fun scanLeDevice(enable: Boolean) {
        if (enable) {
            if (ActivityCompat.checkSelfPermission(contextZ, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                if (!scanning) {
                    handler.postDelayed({
                        scanning = false
                        bluetoothLeScanner?.stopScan(leScanCallback)
                        Log.d("@@BLE", "Scan stopped after timeout")
                    }, SCAN_PERIOD)
                    scanning = true
                    bluetoothLeScanner?.startScan(leScanCallback)
                    Log.d("@@BLE", "Scan started")
                }
            }
        } else {
            scanning = false
            bluetoothLeScanner?.stopScan(leScanCallback)
            Log.d("@@BLE", "Scan stopped manually")
            handler.removeCallbacksAndMessages(null) // Optional: stops timeout if canceling early
        }
    }
    var isBluetoothEnabled by remember { mutableStateOf(bluetoothAdapter?.isEnabled == true) }
    val bluetoothStateReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    isBluetoothEnabled = state == BluetoothAdapter.STATE_ON
                }
            }
        }
    }
    DisposableEffect(Unit) {
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        contextZ.registerReceiver(bluetoothStateReceiver, filter)

        onDispose {
            contextZ.unregisterReceiver(bluetoothStateReceiver)
        }
    }

    Scaffold(
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                        .padding(
                            horizontal = 0.04 * screenWidth
                        )
                ) {
                    LazyColumn {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ){
                                Text(
                                    text = "Available Devices",
                                    fontFamily = latoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = Color.Black,
                                )
                                Spacer(modifier = Modifier.height(0.02 * screenHeight))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(25))
                                            .background(Primary)
                                            .padding(horizontal = 3.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Bluetooth Status:  ${if (isBluetoothEnabled) "On" else "Off"}",
                                            fontFamily = latoFontFamily,
                                            fontSize = 20.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(vertical = 24.dp, horizontal = 8.dp)
                                        )
                                    }
                                    FloatingActionButton(
                                        onClick = {
                                            if (isBluetoothEnabled) {
                                                devices = emptyList()
                                                scanLeDevice(true)
                                            }else{
                                                Toast.makeText(contextZ, "Bluetooth is off", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier
                                            .border(
                                                width = 2.dp,
                                                color = AccentColor,
                                                shape = RoundedCornerShape(16.dp)
                                            ),
                                        containerColor = if (isBluetoothEnabled) Secondary else Color.Gray,
                                    ) {
                                        Text(
                                            text = "Scan Device",
                                            fontFamily = latoFontFamily,
                                            color = Color.Black,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(0.03 * screenHeight))
                            }
                        }
                        item {
                            Column(
                                modifier = Modifier
                                    .padding(vertical = 12.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
                                    .border(
                                        width = 1.5.dp,
                                        color = Primary,
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ){
                                Spacer(modifier = Modifier.height(0.015 * screenHeight))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 0.06 * screenWidth),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ){
                                    Text(
                                        text = "Available Devices",
                                        fontFamily = latoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp,
                                        color = Primary,
                                    )
                                    Text(
                                        text = if (scanning) "Scanning..." else "Not Scanning",
                                        fontFamily = latoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = if (scanning) Color.Gray else AccentColor,
                                    )
                                }
                                Spacer(modifier = Modifier.height(0.01 * screenHeight))
                                if (devices.isEmpty()) {
                                    Text(
                                        text = "No Devices found :(",
                                        fontFamily = latoFontFamily,
                                        fontSize = 18.sp,
                                        color = Color.Gray
                                    )
                                } else {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val espDevices = devices.filter { it.name?.contains("ESP") == true }
                                        if (espDevices.isEmpty()) {
                                            Text(
                                                text = "No ESP devices found :(",
                                                fontFamily = latoFontFamily,
                                                fontSize = 18.sp,
                                                color = Color.Gray,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                            )
                                        } else {
                                            espDevices.forEach { device ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 0.03 * screenWidth),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = device.name ?: "Unknown Device",
                                                        fontFamily = latoFontFamily,
                                                        fontSize = 18.sp,
                                                        color = Color.Black,
                                                        overflow = TextOverflow.Ellipsis,
                                                        maxLines = 1,
                                                        modifier = Modifier.weight(0.45f)
                                                    )

                                                    FloatingActionButton(
                                                        onClick = {
                                                            Log.d("@@BLE", "Connecting to device: ${device.name} (${device.address})")
                                                            if (ActivityCompat.checkSelfPermission(contextZ, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                                                                if (device.bondState == BluetoothDevice.BOND_NONE) {
                                                                    Log.d("@@BLE", "Device not bonded, starting pairing...")
                                                                    device.createBond() // Start pairing process
                                                                } else {
                                                                    Log.d("@@BLE", "Device already bonded, connecting...")
                                                                    bluetoothGatt?.close()
                                                                    bluetoothGatt = device.connectGatt(contextZ, false, gattCallback)
                                                                    connectedDevice = device.name ?: "Unknown"
                                                                    devices = emptyList()
                                                                    bleViewModel.selectedDevice = device
                                                                    Log.d("@@@", bleViewModel.selectedDevice!!.name.toString())
                                                                    navController.navigate("connected")
                                                                }
                                                            } else {
                                                                Toast.makeText(contextZ, "Bluetooth connect permission not granted.", Toast.LENGTH_SHORT).show()
                                                            }

                                                        },
                                                        modifier = Modifier
                                                            .weight(0.5f)
                                                            .border(
                                                                width = 2.dp,
                                                                color = AccentColor,
                                                                shape = RoundedCornerShape(16.dp)
                                                            ),
                                                        containerColor = Secondary,
                                                    ) {
                                                        Text(
                                                            text = "Connect",
                                                            fontFamily = latoFontFamily,
                                                            color = Color.Black,
                                                            fontSize = 18.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 12.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ConnectionScreenPreview() {
    ConnectionScreen(rememberNavController(), viewModel())
}