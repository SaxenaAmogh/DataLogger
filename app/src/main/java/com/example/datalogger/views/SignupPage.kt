package com.example.datalogger.views

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.KeyboardArrowLeft
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.datalogger.ui.theme.Primary
import com.example.datalogger.ui.theme.Background
import com.example.datalogger.ui.theme.Primary
import com.example.datalogger.ui.theme.latoFontFamily

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SignUpPage(navController: NavController){

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val context = LocalView.current.context
    val view = LocalView.current
    val window = (view.context as? Activity)?.window
    val windowInsetsController = window?.let { WindowCompat.getInsetsController(it, view) }

//    val authViewModel: AuthViewModel = viewModel()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
//    val signupStatus by authViewModel.signupStatus

//    LaunchedEffect(signupStatus) {
//        if (signupStatus != null && signupStatus != "success") {
//            Toast.makeText(context, signupStatus, Toast.LENGTH_SHORT).show()
//            // Reset status if needed
//            authViewModel.clearStatus()
//        }
//    }

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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = 0.04 * screenWidth
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                focusManager.clearFocus()
                            })
                        }
                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top)),
                ) {
                    Box(
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = Color(0x66ABABAB),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ){
                        Icon(
                            Icons.AutoMirrored.TwoTone.KeyboardArrowLeft,
                            contentDescription = "Arrow Icon",
                            modifier = Modifier
                                .padding(8.dp)
                                .size(32.dp)
                                .align(Alignment.CenterStart)
                                .clickable {
                                    navController.popBackStack()
                                },
                            tint = Primary
                        )
                    }
                    Spacer(modifier = Modifier.height(0.04 * screenHeight))
                    Text(
                        text = "Sign up",
                        color = Color.Black,
                        fontSize = 36.sp,
                        fontFamily = latoFontFamily,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Create an account to use the Data Logger!",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontFamily = latoFontFamily,
                    )
                    Spacer(modifier = Modifier.height(0.05 * screenHeight))
                    Column {
                        Text(
                            text = "Your Username",
                            fontFamily = latoFontFamily,
                            color = Primary,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp)
                        )
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.063 * screenHeight),
                            shape = RoundedCornerShape(size = 16.dp),
                            placeholder = {
                                Text(
                                    "Username",
                                    fontFamily = latoFontFamily,
                                    color = Color(0xFFB2B2B2),
                                )
                            },
                            value = name,
                            onValueChange = { name = it },
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus() // Hide keyboard on done
                                }
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Color(0x66ABABAB),
                                focusedTextColor = Color(0xFF000000),
                                unfocusedTextColor = Color(0xFF595959),
                                unfocusedContainerColor = Color(0x14ABABAB),
                                focusedContainerColor = Color(0x14ABABAB)
                            )
                        )
                        Spacer(modifier = Modifier.height(0.02 * screenHeight))
                        Text(
                            text = "Your Email Address",
                            fontFamily = latoFontFamily,
                            color = Primary,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp)
                        )
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.063 * screenHeight),
                            shape = RoundedCornerShape(size = 16.dp),
                            placeholder = {
                                Text(
                                    "Email",
                                    fontFamily = latoFontFamily,
                                    color = Color(0xFFB2B2B2),
                                )
                            },
                            value = email,
                            onValueChange = { email = it },
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus() // Hide keyboard on done
                                }
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Color(0x66ABABAB),
                                focusedTextColor = Color(0xFF000000),
                                unfocusedTextColor = Color(0xFF595959),
                                unfocusedContainerColor = Color(0x14ABABAB),
                                focusedContainerColor = Color(0x14ABABAB)
                            )
                        )
                        Spacer(modifier = Modifier.height(0.02 * screenHeight))
                        Text(
                            text = "Your Password",
                            fontFamily = latoFontFamily,
                            color = Primary,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp)
                        )
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.063 * screenHeight),
                            shape = RoundedCornerShape(size = 16.dp),
                            placeholder = {
                                Text(
                                    "Password",
                                    fontFamily = latoFontFamily,
                                    color = Color(0xFFB2B2B2),
                                )
                            },
                            value = password,
                            onValueChange = { password = it },
                            keyboardOptions = KeyboardOptions(
                                autoCorrect = false,
                                keyboardType = KeyboardType.Password
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus() // Hide keyboard on done
                                }
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Color(0x66ABABAB),
                                focusedTextColor = Color(0xFF000000),
                                unfocusedTextColor = Color(0xFF595959),
                                unfocusedContainerColor = Color(0x14ABABAB),
                                focusedContainerColor = Color(0x14ABABAB)
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(0.03 * screenHeight))
                    FloatingActionButton(
                        onClick = {
//                            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
//                                authViewModel.signUpUser(
//                                    name = name,
//                                    email = email,
//                                    password = password,
//                                    onSuccess = {
//                                        navController.navigate("home") {
//                                            popUpTo("create") { inclusive = true }
//                                        }
//                                        Toast.makeText(
//                                            context,
//                                            "Account created successfully!",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                    }
//                                )
//                            } else {
//                                Toast.makeText(
//                                    context,
//                                    "Please fill in all fields",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
                            navController.navigate("home")
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
                            text = "Create Account",
                            fontFamily = latoFontFamily,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.W500,
                        )
                    }
                    Spacer(modifier = Modifier.height(0.03 * screenHeight))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Already have an account?",
                            fontFamily = latoFontFamily,
                            color = Color(0xFFB2B2B2),
                            fontSize = 16.sp,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sign in",
                            fontFamily = latoFontFamily,
                            color = Primary,
                            fontSize = 16.sp,
                            modifier = Modifier.clickable {
                                navController.navigate("signin")
                            }
                        )
                    }
                }
            }
        }
    )

}

@Preview(showBackground = true)
@Composable
fun SignUpPagePreview() {
    SignUpPage(rememberNavController())
}