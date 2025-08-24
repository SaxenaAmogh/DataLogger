package com.example.datalogger.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SensorViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance().getReference("sensorData/readings/ldrReading")
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    fun startSendingData(ldrData: Float) {
        val timestamp = timeFormat.format(Date())
        database.child(timestamp).setValue(ldrData) // Push data to Firebase
    }
}