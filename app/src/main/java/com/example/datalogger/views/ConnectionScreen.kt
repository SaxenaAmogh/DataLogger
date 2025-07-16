package com.example.datalogger.views

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.datalogger.BleManager
import com.example.datalogger.ui.theme.AccentColor
import com.example.datalogger.ui.theme.Background
import com.example.datalogger.ui.theme.Error
import com.example.datalogger.ui.theme.Primary
import com.example.datalogger.ui.theme.Success
import com.example.datalogger.ui.theme.latoFontFamily

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ConnectionScreen(navController: NavController) {

    val context = LocalContext.current
    var devices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val focusManager = LocalFocusManager.current

    val view = LocalView.current
    val window = (view.context as? Activity)?.window
    val windowInsetsController = window?.let { WindowCompat.getInsetsController(it, view) }

    if (windowInsetsController != null) {
        windowInsetsController.isAppearanceLightStatusBars = true
    }

    //Checking and turning on Bluetooth
    val bluetoothManager: BluetoothManager? = getSystemService(context, BluetoothManager::class.java)
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.d("@@BLE", "onScanResult triggered")
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
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
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
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
    val bluetoothStateReceiver = rememberUpdatedState(newValue = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                isBluetoothEnabled = state == BluetoothAdapter.STATE_ON
                if (state == BluetoothAdapter.STATE_ON) {
                    scanLeDevice(true)
                }
            }
        }
    })

    DisposableEffect(Unit) {
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(bluetoothStateReceiver.value, filter)
        onDispose {
            context.unregisterReceiver(bluetoothStateReceiver.value)
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
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Bluetooth Status:  ${if (isBluetoothEnabled) "On" else "Off"}",
                                    fontFamily = latoFontFamily,
                                    fontSize = 20.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 24.dp, horizontal = 14.dp)
                                )
                            }
                            if (!isBluetoothEnabled) {
                                Switch(
                                    checked = isBluetoothEnabled,
                                    onCheckedChange = {
                                        if (it) {
                                            val enableBtIntent =
                                                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                                            if (context is Activity) {
                                                startActivityForResult(
                                                    context,
                                                    enableBtIntent,
                                                    1,
                                                    null
                                                )
                                            }
                                        }
                                    },
                                    colors = SwitchColors(
                                        checkedThumbColor = Success,
                                        uncheckedThumbColor = Error,
                                        checkedTrackColor = Success.copy(alpha = 0.6f),
                                        uncheckedTrackColor = Error.copy(alpha = 0.5f),
                                        checkedBorderColor = Color.Transparent,
                                        checkedIconColor = Color.Transparent,
                                        uncheckedBorderColor = Color.Transparent,
                                        uncheckedIconColor = Color.Transparent,
                                        disabledCheckedThumbColor = Color.Transparent,
                                        disabledCheckedTrackColor = Color.Transparent,
                                        disabledCheckedBorderColor = Color.Transparent,
                                        disabledCheckedIconColor = Color.Transparent,
                                        disabledUncheckedThumbColor = Color.Transparent,
                                        disabledUncheckedTrackColor = Color.Transparent,
                                        disabledUncheckedBorderColor = Color.Transparent,
                                        disabledUncheckedIconColor = Color.Transparent,
                                    )
                                )
                            }else{
                                Switch(
                                    checked = isBluetoothEnabled,
                                    onCheckedChange = {
                                        focusManager.clearFocus()
                                    },
                                    colors = SwitchColors(
                                        checkedThumbColor = Success,
                                        uncheckedThumbColor = Error,
                                        checkedTrackColor = Success.copy(alpha = 0.6f),
                                        uncheckedTrackColor = Error.copy(alpha = 0.5f),
                                        checkedBorderColor = Color.Transparent,
                                        checkedIconColor = Color.Transparent,
                                        uncheckedBorderColor = Color.Transparent,
                                        uncheckedIconColor = Color.Transparent,
                                        disabledCheckedThumbColor = Color.Transparent,
                                        disabledCheckedTrackColor = Color.Transparent,
                                        disabledCheckedBorderColor = Color.Transparent,
                                        disabledCheckedIconColor = Color.Transparent,
                                        disabledUncheckedThumbColor = Color.Transparent,
                                        disabledUncheckedTrackColor = Color.Transparent,
                                        disabledUncheckedBorderColor = Color.Transparent,
                                        disabledUncheckedIconColor = Color.Transparent,
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(0.05 * screenHeight))
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = 1.5.dp,
                                color = Primary,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .height(0.5 * screenHeight),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ){
                        Spacer(modifier = Modifier.height(0.015 * screenHeight))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 0.07 * screenWidth),
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
                            Icon(
                                Icons.Rounded.Refresh,
                                contentDescription = "Refresh",
                                tint = if (scanning) Color.Gray else AccentColor,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable(enabled = !scanning) {
                                        scanLeDevice(true)
                                        Log.d("BLE", "Refresh clicked, scanning for devices")
                                    }
                            )
                        }
                        Spacer(modifier = Modifier.height(0.02 * screenHeight))
                        if (devices.isEmpty()) {
                            Text(
                                text = "No devices found",
                                fontFamily = latoFontFamily,
                                fontSize = 18.sp,
                                color = Color.Gray
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                devices.forEach { device ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White)
                                            .padding(16.dp)
                                            .border(
                                                width = 1.dp,
                                                color = Primary,
                                                shape = RoundedCornerShape(12.dp)
                                            ),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = device.name ?: "Unnamed Device",
                                            fontFamily = latoFontFamily,
                                            fontSize = 18.sp,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )



//    Column(modifier = Modifier.padding(16.dp)) {
//        Text("Found Devices:", fontWeight = FontWeight.Bold)
//f
//        devices.forEach { device ->
//            Button(
//                onClick = {
//                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
//                        bleManager.connectToDevice(device, context) {
//                            Toast.makeText(context, "Connected to ${device.name}", Toast.LENGTH_SHORT).show()
//                        }
//                    } else {
//                        Toast.makeText(context, "Bluetooth connect permission not granted", Toast.LENGTH_SHORT).show()
//                    }
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 4.dp)
//            ) {
//                Text(text = device.name ?: "Unnamed")
//            }
//        }
//    }
}

@Preview(showBackground = true)
@Composable
fun ConnectionScreenPreview() {
    ConnectionScreen(rememberNavController())
}