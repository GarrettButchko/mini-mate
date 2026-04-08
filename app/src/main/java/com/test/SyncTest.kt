package com.test
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Test() {
    val state = rememberScrollState()
    Column {
        Row(Modifier.horizontalScroll(state)) {
            Text("1")
        }
        Row(Modifier.horizontalScroll(state)) {
            Text("2")
        }
    }
}
