package com.example.datalogger.views

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.datalogger.R
import com.example.datalogger.ui.theme.AccentColor
import com.example.datalogger.ui.theme.Primary
import com.example.datalogger.ui.theme.Background
import com.example.datalogger.ui.theme.Primary
import com.example.datalogger.ui.theme.latoFontFamily
import com.example.datalogger.viewmodel.PermissionViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomePage(navController: NavController) {

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
                            containerColor = AccentColor,
                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp,
                                focusedElevation = 0.dp,
                                hoveredElevation = 0.dp
                            )
                        ) {
                            Row(
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

                    Row(
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(
                                horizontal = 0.04 * screenWidth
                            )
                            .background(
                                shape = RoundedCornerShape(40),
                                color = Primary
                            ),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {},
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .size(55.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.home_d),
                                contentDescription = "home",
                                Modifier.size(32.dp),
                                tint = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.size(12.dp))
                        IconButton(
                            onClick = {
                                navController.navigate("connect")
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .size(55.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.bluetooth_d),
                                contentDescription = "cart_na",
                                Modifier.size(32.dp),
                                tint = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.size(12.dp))
                        IconButton(
                            onClick = {
                                navController.navigate("security")
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .size(55.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.user_d),
                                contentDescription = "explore",
                                Modifier.size(32.dp),
                                tint = Color.Black
                            )
                        }
                    }

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