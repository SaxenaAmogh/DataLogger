package com.example.datalogger.views

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.datalogger.viewmodel.PermissionViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@SuppressLint("PermissionLaunchedDuringComposition")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomePage(navController: NavController) {

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
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }
    val multiplePermissionsState = rememberMultiplePermissionsState(permissionsToRequest)
    val showRationalDialog = remember { mutableStateOf(false) }
    val allGranted = multiplePermissionsState.permissions.all { it.status.isGranted }
    val permanentlyDenied = multiplePermissionsState.permissions.any { !it.status.isGranted && !it.status.shouldShowRationale }
    val shouldShowRationale = multiplePermissionsState.permissions.any { it.status.shouldShowRationale }

    LaunchedEffect(Unit) {
        if (permanentlyDenied) {
            showRationalDialog.value = true
        }
    }
    if (showRationalDialog.value) {
        AlertDialog(
            containerColor = Color(0xFFF5F5F5),
            onDismissRequest = {
                showRationalDialog.value = false
            },
            title = {
                Text(
                    text = "Allow Permissions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black
                )
            },
            text = {
                Text(
                    text = "For app's proper functionality, Nearby Devices and Location is needed. Please allow them in settings.",
                    fontSize = 16.sp
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
                    Text("Allow", style = TextStyle(color = Color.Black), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRationalDialog.value = false
                    }) {
                    Text("Cancel", style = TextStyle(color = Color.Black), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
        )
    }

    Scaffold{
        Box(
            modifier = Modifier.fillMaxSize().padding(it),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = {
                    multiplePermissionsState.launchMultiplePermissionRequest()
                }) {
                    Text(text = "Ask for permission")
                }
                Text(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp),
                    text = when {
                        allGranted -> "All permissions granted ✅"
                        shouldShowRationale -> "Please allow permissions for Bluetooth access."
                        permanentlyDenied -> "Permissions denied forever. Enable them from settings."
                        else -> "Permissions not yet granted."
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }


    //Checking and turning on Bluetooth
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

}

@Preview(showBackground = true)
@Composable
fun HomePagePreview() {
    HomePage(rememberNavController())
}