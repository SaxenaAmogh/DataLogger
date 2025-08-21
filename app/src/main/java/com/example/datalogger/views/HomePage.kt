package com.example.datalogger.views

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.datalogger.R
import com.example.datalogger.ui.theme.AccentColor
import com.example.datalogger.ui.theme.Background
import com.example.datalogger.ui.theme.Error
import com.example.datalogger.ui.theme.Primary
import com.example.datalogger.ui.theme.Success
import com.example.datalogger.ui.theme.latoFontFamily

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomePage(navController: NavController) {

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val context = LocalContext.current
    val showRationalDialog = remember { mutableStateOf(false) }

    val view = LocalView.current
    val window = (view.context as? Activity)?.window
    val windowInsetsController = window?.let { WindowCompat.getInsetsController(it, view) }

    if (windowInsetsController != null) {
        windowInsetsController.isAppearanceLightStatusBars = true
    }

// Bluetooth and Location managers
    val bluetoothManager = getSystemService(context, BluetoothManager::class.java)
    val bluetoothAdapter = bluetoothManager?.adapter
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

// State holders
    var isLocationEnabled by remember {
        mutableStateOf(
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        )
    }
    var isBluetoothEnabled by remember {
        mutableStateOf(bluetoothAdapter?.isEnabled == true)
    }

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

    val locationStateReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                    context?.let {
                        val locManager = it.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        val gpsEnabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                        val netEnabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                        isLocationEnabled = gpsEnabled || netEnabled
                    }
                }
            }
        }
    }

// ContentObserver for location changes
    DisposableEffect(Unit) {
        val locationObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val netEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                isLocationEnabled = gpsEnabled || netEnabled
            }
        }

        context.contentResolver.registerContentObserver(
            Settings.Secure.getUriFor(Settings.Secure.LOCATION_MODE),
            true,
            locationObserver
        )

        onDispose {
            context.contentResolver.unregisterContentObserver(locationObserver)
        }
    }

// Register Bluetooth receiver
    DisposableEffect(Unit) {
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(bluetoothStateReceiver, filter)

        onDispose {
            context.unregisterReceiver(bluetoothStateReceiver)
        }
    }

// Register Location receiver
    DisposableEffect(Unit) {
        val locationFilter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        context.registerReceiver(locationStateReceiver, locationFilter)

        onDispose {
            context.unregisterReceiver(locationStateReceiver)
        }
    }

// Hide dialog if both enabled
    LaunchedEffect(isBluetoothEnabled, isLocationEnabled) {
        if (isBluetoothEnabled && isLocationEnabled) {
            showRationalDialog.value = false
        }
    }


    if (showRationalDialog.value) {
        AlertDialog(
            containerColor = Color(0xFF1D3B5E),
            onDismissRequest = {
                showRationalDialog.value = false
            },
            title = {
                Text(
                    text = "Bluetooth and Location",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleLarge,
                    color = AccentColor
                )
            },
            text = {
                Text(
                    text = "Turn on ${if(!isBluetoothEnabled && !isLocationEnabled) "Bluetooth and Location" else if (!isBluetoothEnabled)"Bluetooth" else if(!isLocationEnabled) "Location" else ""} to continue.",
                    fontSize = 16.sp,
                    color = Color.White,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if(!isBluetoothEnabled){
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
                        }else if(!isLocationEnabled){
                            val enableLocIntent =
                                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            if (context is Activity) {
                                startActivityForResult(
                                    context,
                                    enableLocIntent,
                                    1,
                                    null
                                )
                            }
                        }
                    }) {
                    Text("Allow", style = TextStyle(color = Success), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {}
                ){
                    Text("Cancel", style = TextStyle(color = Error), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
        )
    }

    Scaffold(
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                        .padding(
                            horizontal = 0.04 * screenWidth
                        )
                ) {
                    Text(
                        text = "Data Logger",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        fontFamily = latoFontFamily,
                        color = Primary,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                        ){
                        Image(
                            painter = painterResource(id = R.drawable.diagram),
                            contentDescription = "App Logo"
                        )
                        Text(
                            modifier = Modifier
                                .padding(top = 0.04 * screenHeight),
                            text = "Hardware Diagram",
                            fontFamily = latoFontFamily,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = Primary
                        )
                        Spacer(modifier = Modifier.padding(0.03 * screenHeight))
                        FloatingActionButton(
                            onClick = {
                            },
                            modifier = Modifier
                                .padding(horizontal = 0.12 * screenWidth)
                                .fillMaxWidth(),
                            containerColor = Primary,
                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp,
                                focusedElevation = 0.dp,
                                hoveredElevation = 0.dp
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        if(!isBluetoothEnabled || !isLocationEnabled){
                                            showRationalDialog.value = true
                                        }else{
                                            Toast.makeText(context, "Bluetooth and Location are enabled", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ){
                                Text(
                                    text = "Turn on Bluetooth and Location",
                                    fontFamily = latoFontFamily,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.W500,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.padding(0.01 * screenHeight))
                        FloatingActionButton(
                            onClick = {
                            },
                            modifier = Modifier
                                .padding(horizontal = 0.12 * screenWidth)
                                .fillMaxWidth(),
                            containerColor = AccentColor,
                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp,
                                focusedElevation = 0.dp,
                                hoveredElevation = 0.dp
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        if(isBluetoothEnabled && isLocationEnabled) {
                                            navController.navigate("connect")
                                        }else{
                                            Toast.makeText(context, "Enable Bluetooth and Location.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ){
                                Text(
                                    text = "Connect to Device",
                                    fontFamily = latoFontFamily,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.W500,
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(
                                    Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = "Bluetooth Icon",
                                    modifier = Modifier.size(26.dp),
                                    tint = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.padding(0.04 * screenHeight))
                    }

                    //Bottom NavBar Code
//                    Row(
//                        modifier = Modifier
//                            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
//                            .align(Alignment.BottomCenter)
//                            .fillMaxWidth()
//                            .padding(
//                                horizontal = 0.04 * screenWidth
//                            )
//                            .background(
//                                shape = RoundedCornerShape(40),
//                                color = Primary
//                            ),
//                        horizontalArrangement = Arrangement.SpaceEvenly,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        IconButton(
//                            onClick = {},
//                            modifier = Modifier
//                                .clip(RoundedCornerShape(50))
//                                .size(55.dp)
//                        ) {
//                            Icon(
//                                painter = painterResource(R.drawable.home_d),
//                                contentDescription = "home",
//                                Modifier.size(32.dp),
//                                tint = Color.Black
//                            )
//                        }
//                        Spacer(modifier = Modifier.size(12.dp))
//                        IconButton(
//                            onClick = {
//                            },
//                            modifier = Modifier
//                                .clip(RoundedCornerShape(50))
//                                .size(55.dp)
//                        ) {
//                            Icon(
//                                painter = painterResource(R.drawable.bluetooth_d),
//                                contentDescription = "cart_na",
//                                Modifier.size(32.dp),
//                                tint = Color.Black
//                            )
//                        }
//                        Spacer(modifier = Modifier.size(12.dp))
//                        IconButton(
//                            onClick = {
//                                navController.navigate("security")
//                            },
//                            modifier = Modifier
//                                .clip(RoundedCornerShape(50))
//                                .size(55.dp)
//                        ) {
//                            Icon(
//                                painter = painterResource(R.drawable.user_d),
//                                contentDescription = "explore",
//                                Modifier.size(32.dp),
//                                tint = Color.Black
//                            )
//                        }
//                    }

                }
            }
        }
        )
    }


//    //Checking and turning on Bluetooth
//    val bluetoothManager: BluetoothManager? = getSystemService(context, BluetoothManager::class.java)
//    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
//    if (bluetoothAdapter == null) {
//        Toast.makeText(
//            context,
//            "Bluetooth is not supported on this device",
//            Toast.LENGTH_SHORT
//        ).show()
//    }else{
//        if (!bluetoothAdapter.isEnabled) {
//            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            startActivityForResult(context as androidx.activity.ComponentActivity, enableBtIntent, 1, null)
//        } else {
//            // Bluetooth is enabled, proceed with your logic
//            Toast.makeText(context, "Bluetooth is enabled", Toast.LENGTH_SHORT).show()
//        }
//    }

@Preview(showBackground = true)
@Composable
fun HomePagePreview() {
    HomePage(rememberNavController())
}