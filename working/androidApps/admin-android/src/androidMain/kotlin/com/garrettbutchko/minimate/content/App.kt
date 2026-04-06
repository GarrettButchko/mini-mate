package com.garrettbutchko.minimate.content

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.Greeting
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics

@Composable
fun App() {
    val message = remember { mutableStateOf(Greeting().greet()) }
    var firebaseStatus by remember { mutableStateOf("Checking Firebase...") }

    LaunchedEffect(Unit) {
        try {
            initializeFirebase()
            firebaseStatus = "Firebase Analytics Initialized!"
        } catch (e: Exception) {
            firebaseStatus = "Firebase Error: ${e.message}"
        }
    }

    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Admin says: $message")
            Text(text = firebaseStatus, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

private fun initializeFirebase() {
    // Accessing analytics to trigger initialization check
    val analytics: FirebaseAnalytics = Firebase.analytics
}