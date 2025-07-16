package com.example.datalogger.views

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.core.content.ContextCompat.startActivity
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale

//@SuppressLint("PermissionLaunchedDuringComposition")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SplashScreen(navController: NavController){

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val view = LocalView.current
    val window = (view.context as? Activity)?.window
    val windowInsetsController = window?.let { WindowCompat.getInsetsController(it, view) }

    if (windowInsetsController != null) {
        windowInsetsController.isAppearanceLightStatusBars = true
    }
    val context = LocalContext.current

    val permissionsToRequest = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            listOf(
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        else -> {
            listOf(
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN,
            )
        }
    }

    val multiplePermissionsState = rememberMultiplePermissionsState(permissionsToRequest)
    val showRationalDialog = remember { mutableStateOf(false) }
    val allGranted = multiplePermissionsState.permissions.all { it.status.isGranted }

    LaunchedEffect(Unit) {
        if (!allGranted) {
            multiplePermissionsState.launchMultiplePermissionRequest()
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
                    text = "Allow Permissions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleLarge,
                    color = AccentColor
                )
            },
            text = {
                Text(
                    text = "For app's proper functionality, Nearby Devices and Location is needed. Please allow them in settings.",
                    fontSize = 16.sp,
                    color = Color.White,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRationalDialog.value = false
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(context, intent, null)
                    }) {
                    Text("Allow", style = TextStyle(color = Success), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRationalDialog.value = false
                    }) {
                    Text("Cancel", style = TextStyle(color = Error), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
        )
    }


    Scaffold(
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFfaf9fa))
                    .padding(
                        0.035 * screenWidth,
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(innerPadding)
                        .align(Alignment.Center)
                        .offset(y = -0.05 * screenHeight)
                    ,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Jetsons Robotics",
                        fontFamily = latoFontFamily,
                        color = Color(0xFF000000),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Image(
                        painter = painterResource(id = R.drawable.logo), // Replace with your image resource
                        contentDescription = "Splash Image",
                        modifier = Modifier.size(0.75 * screenWidth),
                        contentScale = ContentScale.Crop
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
                        .padding(
                            bottom = 0.04 * screenWidth
                        )
                        .align(Alignment.BottomCenter)
                ){
                    Spacer(modifier = Modifier.height(0.05 * screenHeight))
                    FloatingActionButton(
                        onClick = {
                            if (!allGranted) {
                                showRationalDialog.value = true
                            } else {
                                navController.navigate("signup")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        containerColor = Primary,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            focusedElevation = 0.dp,
                            hoveredElevation = 0.dp
                        )
                    ) {
                        Text(
                            text = "Get Started",
                            fontFamily = latoFontFamily,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.W500,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    FloatingActionButton(
                        onClick = {
                            if (!allGranted) {
                                showRationalDialog.value = true
                            } else {
                                navController.navigate("signin")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 2.dp,
                                color = Primary,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        containerColor = Background,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            focusedElevation = 0.dp,
                            hoveredElevation = 0.dp
                        )
                    ) {
                        Text(
                            text = "Sign in",
                            fontFamily = latoFontFamily,
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.W500,
                        )
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    // Preview of the SplashScreen
    SplashScreen(rememberNavController())
}