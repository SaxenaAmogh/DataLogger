package com.example.datalogger.views

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.pm.PackageManager
import android.util.Log
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.datalogger.R
import com.example.datalogger.ui.theme.AccentColor
import com.example.datalogger.ui.theme.Background
import com.example.datalogger.ui.theme.Primary
import com.example.datalogger.ui.theme.Success
import com.example.datalogger.ui.theme.latoFontFamily
import com.example.datalogger.viewmodel.BleViewModel
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ConnectedPage(navController: NavController, bleViewModel: BleViewModel){
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
//    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // Initialize working states for sensors. Must be changed according to hardware
    val workingS1 = remember { mutableStateOf(false) }
    val workingS2 = remember { mutableStateOf(false) }
    val workingS3 = remember { mutableStateOf(true) }
    val workingS4 = remember { mutableStateOf(false) }

    val view = LocalView.current
    val window = (view.context as? Activity)?.window
    val windowInsetsController = window?.let { WindowCompat.getInsetsController(it, view) }
    if (windowInsetsController != null) {
        windowInsetsController.isAppearanceLightStatusBars = true
    }

    // Get the selected device from the ViewModel
    val device: BluetoothDevice? = bleViewModel.selectedDevice  // <- from scan result
    val receivedText by remember { derivedStateOf { bleViewModel.receivedText } }

    // Initialize sensor values as mutable states of sensor data
    val sensorValue1 = remember { mutableStateOf(0.0f) }
    val sensorValue2 = remember { mutableStateOf(0.0f) }
    val sensorValue3 = remember { mutableStateOf(0.0f) }
    val sensorValue4 = remember { mutableStateOf(0.0f) }

// A state to hold the GATT connection
    val gattRef = remember { mutableStateOf<BluetoothGatt?>(null) }

// Use LaunchedEffect to safely manage the BLE connection lifecycle
    LaunchedEffect(key1 = device) {
        // If there's no device, do nothing
        if (device == null) return@LaunchedEffect

        // Define UUIDs here or access them from a constants file.
        // ** Must be exactly same as of hardware for BLE to work.
        val SENSOR_SERVICE_UUID = UUID.fromString("0000abcd-0000-1000-8000-00805f9b34fb")
        val SENSOR_CHARACTERISTIC_UUID = UUID.fromString("00001234-0000-1000-8000-00805f9b34fb")
        val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        // Define the GATT callback object
        val gattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.i("GattCallback", "Successfully connected to device.")
                        gatt.discoverServices()
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.i("GattCallback", "Disconnected from device.")
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_SCAN
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val service = gatt.getService(SENSOR_SERVICE_UUID)
                        val characteristic = service?.getCharacteristic(SENSOR_CHARACTERISTIC_UUID)
                        if (characteristic != null) {
                            gatt.setCharacteristicNotification(characteristic, true)
                            val descriptor = characteristic.getDescriptor(CCCD_UUID)
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt.writeDescriptor(descriptor)
                            Log.i("GattCallback", "Subscribed to notifications.")
                        }
                    }
                }
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                if (characteristic.uuid == SENSOR_CHARACTERISTIC_UUID) {
                    val data = characteristic.value
                    if (data.size == 16) {
                        val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

                        // Update the state variables. This will trigger recomposition.
                        sensorValue1.value = buffer.getFloat()
                        sensorValue2.value = buffer.getFloat()
                        sensorValue3.value = buffer.getFloat()
                        sensorValue4.value = buffer.getFloat()

                        // Log the decoded values for debugging
                        Log.i("GattCallback", "Decoded Floats: ${sensorValue1.value}, ${sensorValue2.value}, ...")
                    }
                }
            }
        }

        // --- This is where the connection is initiated ---
        Log.d("BleConnection", "Connecting to device...")
        gattRef.value = device.connectGatt(context, false, gattCallback)
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
                        item{
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 18.dp)
                            ){
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(25))
                                        .background(Primary)
                                        .padding(horizontal = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Connected Device:",
                                        fontFamily = latoFontFamily,
                                        fontSize = 22.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(
                                            vertical = 10.dp,
                                            horizontal = 8.dp
                                        )
                                    )
                                    // Log the selected device for debugging
                                    //Log.d("@@@Cd", bleViewModel.selectedDevice.toString())
                                    bleViewModel.selectedDevice?.let { it1 ->
                                        Text(
                                            text = it1.name,
                                            fontFamily = latoFontFamily,
                                            fontSize = 20.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.W500,
                                            modifier = Modifier.padding(
                                                vertical = 10.dp,
                                                horizontal = 8.dp
                                            )
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(0.02 * screenHeight))
                        }
                        //This section is fow showing what sensors are working.
                        // Currently hardcoded, needs to be updated by getting some value from hardware.
                        item {
                            Column(
                                modifier = Modifier
                                    .padding(vertical = 12.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .windowInsetsPadding(
                                        WindowInsets.systemBars.only(
                                            WindowInsetsSides.Bottom
                                        )
                                    )
                                    .border(
                                        width = 1.5.dp,
                                        color = Primary,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ){
                                Text(
                                    text = "Testing Sensors....",
                                    fontFamily = latoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Primary,
                                    modifier = Modifier
                                        .padding(top = 12.dp)
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 0.1 * screenWidth, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ){
                                    Text(
                                        text = "Temperature Sensor",
                                        fontFamily = latoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        fontSize = 19.sp,
                                        color = AccentColor,
                                        modifier = Modifier
                                            .weight(0.6f)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Icon(
                                        painter = painterResource(if (workingS1.value) R.drawable.checked else R.drawable.load),
                                        contentDescription = "loading",
                                        modifier = Modifier
                                            .weight(0.2f)
                                            .size(32.dp),
                                        tint = if (workingS1.value) Success else Color(0xFF838383)
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 0.1 * screenWidth, vertical = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ){
                                    Text(
                                        text = "Humidity Sensor",
                                        fontFamily = latoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        fontSize = 19.sp,
                                        color = AccentColor,
                                        modifier = Modifier
                                            .weight(0.6f)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Icon(
                                        painter = painterResource(if (workingS2.value) R.drawable.checked else R.drawable.load),
                                        contentDescription = "loading",
                                        modifier = Modifier
                                            .weight(0.2f)
                                            .size(32.dp),
                                        tint = if (workingS2.value) Success else Color(0xFF838383)
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 0.1 * screenWidth, vertical = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ){
                                    Text(
                                        text = "LDR Sensor",
                                        fontFamily = latoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        fontSize = 19.sp,
                                        color = AccentColor,
                                        modifier = Modifier
                                            .weight(0.6f)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Icon(
                                        painter = painterResource(if (workingS3.value) R.drawable.checked else R.drawable.load),
                                        contentDescription = "loading",
                                        modifier = Modifier
                                            .weight(0.2f)
                                            .size(32.dp),
                                        tint = if (workingS3.value) Success else Color(0xFF838383)
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 0.1 * screenWidth, vertical = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ){
                                    Text(
                                        text = "Current Sensor",
                                        fontFamily = latoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        fontSize = 19.sp,
                                        color = AccentColor,
                                        modifier = Modifier
                                            .weight(0.6f)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Icon(
                                        painter = painterResource(if (workingS4.value) R.drawable.checked else R.drawable.load),
                                        contentDescription = "loading",
                                        modifier = Modifier
                                            .weight(0.2f)
                                            .size(32.dp),
                                        tint = if (workingS4.value) Success else Color(0xFF838383)
                                    )
                                }
                            }
                        }
                        //This section is for showing the sensor values.
                        // The sensor names are hardcoded, but the values are dynamic and will change. Change the names according to your hardware.
                        item {
                            Column(
                                modifier = Modifier
                                    .padding(vertical = 12.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .windowInsetsPadding(
                                        WindowInsets.systemBars.only(
                                            WindowInsetsSides.Bottom
                                        )
                                    )
                                    .border(
                                        width = 1.5.dp,
                                        color = Primary,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = "Getting Data....",
                                    fontFamily = latoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Primary,
                                    modifier = Modifier
                                        .padding(top = 12.dp)
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = 0.1 * screenWidth,
                                            vertical = 2.dp
                                        ),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Sensor1",
                                        fontFamily = latoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        fontSize = 19.sp,
                                        color = AccentColor,
                                        modifier = Modifier
                                            .weight(0.6f)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = sensorValue1.value.toString(),
                                        fontFamily = latoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        fontSize = 18.sp,
                                        color = Color.Black,
                                        modifier = Modifier
                                            .weight(0.6f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(0.02 * screenHeight))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = 0.1 * screenWidth,
                                            vertical = 2.dp
                                        ),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Sensor2",
                                        fontFamily = latoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        fontSize = 19.sp,
                                        color = AccentColor,
                                        modifier = Modifier
                                            .weight(0.6f)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = sensorValue2.value.toString(),
                                        fontFamily = latoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        fontSize = 18.sp,
                                        color = Color.Black,
                                        modifier = Modifier
                                            .weight(0.6f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(0.02 * screenHeight))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = 0.1 * screenWidth,
                                            vertical = 2.dp
                                        ),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Sensor2",
                                        fontFamily = latoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        fontSize = 19.sp,
                                        color = AccentColor,
                                        modifier = Modifier
                                            .weight(0.6f)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = sensorValue3.value.toString(),
                                        fontFamily = latoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        fontSize = 18.sp,
                                        color = Color.Black,
                                        modifier = Modifier
                                            .weight(0.6f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(0.02 * screenHeight))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = 0.1 * screenWidth,
                                            vertical = 2.dp
                                        ),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Sensor2",
                                        fontFamily = latoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        fontSize = 19.sp,
                                        color = AccentColor,
                                        modifier = Modifier
                                            .weight(0.6f)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = sensorValue4.value.toString(),
                                        fontFamily = latoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        fontSize = 18.sp,
                                        color = Color.Black,
                                        modifier = Modifier
                                            .weight(0.6f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(0.02 * screenHeight))
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
fun ConnectedPagePreview(){
    ConnectedPage(rememberNavController(), viewModel())
}