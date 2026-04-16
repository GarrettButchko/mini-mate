package com.garrettbutchko.minimate.content

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.VectorConverter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.garrettbutchko.minimate.shared.BuildConfig

enum class DeleteAlertType {
    GOOGLE, APPLE, EMAIL
}

@Composable
fun ProfileView(
    isMiniMateApp: Boolean = true,
    userProfileImage: String?,
    userName: String,
    userEmail: String?,
    userGoogleId: String,
    userIsPro: Boolean,
    userAccountType: String,
    buildTypeLabel: String? = null,
    botMessage: String = "",
    isBotMessageError: Boolean = false,
    onPhotoChange: (Uri) -> Unit,
    onGetPro: () -> Unit,
    onEditName: (String) -> Unit,
    onPasswordReset: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccountGoogle: () -> Unit,
    onDeleteAccountApple: () -> Unit,
    onDeleteAccountEmail: (password: String) -> Unit,
    onDismiss: () -> Unit
) {
    var showEditName by remember { mutableStateOf(false) }
    var editNameInput by remember { mutableStateOf(userName) }
    
    var activeDeleteAlert by remember { mutableStateOf<DeleteAlertType?>(null) }
    var showEmailDeleteOverlay by remember { mutableStateOf(false) }
    var deletePasswordInput by remember { mutableStateOf("") }
    var deleteConfirmPasswordInput by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { onPhotoChange(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Profile",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tap to change photo", fontSize = 12.sp)
                Spacer(modifier = Modifier.width(6.dp))
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (userProfileImage != null) {
                        AsyncImage(
                            model = userProfileImage,
                            contentDescription = "Profile Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Placeholder LogoOpp equivalent
                        Text("", fontSize = 10.sp, color = Color.White)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Details Section
            item {
                Text("User Details", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row {
                            Text("Name: ", fontWeight = FontWeight.SemiBold)
                            Text(userName)
                        }
                        HorizontalDivider()
                        Row {
                            Text("Email: ", fontWeight = FontWeight.SemiBold)
                            Text(userEmail ?: "N/A")
                        }
                        HorizontalDivider()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Pro: ", fontWeight = FontWeight.SemiBold)
                            Text(if (userIsPro) "Yes" else "Not Yet!")
                            if (isMiniMateApp && !userIsPro) {
                                Spacer(modifier = Modifier.weight(1f))
                                TextButton(onClick = onGetPro) {
                                    Text("Get Pro Now!")
                                }
                            }
                        }
                        if (BuildConfig.DEBUG) {
                            HorizontalDivider()
                            Row {
                                Text("Build: ", fontWeight = FontWeight.SemiBold)
                                Text(buildTypeLabel ?: BuildConfig.BUILD_TYPE)
                            }
                            HorizontalDivider()
                            Row {
                                Text("UID: ", fontWeight = FontWeight.SemiBold)
                                Text(userGoogleId)
                            }
                            HorizontalDivider()
                            Row {
                                Text("PhotoURL: ", fontWeight = FontWeight.SemiBold)
                                if (userProfileImage != null) {
                                    Text(userProfileImage)
                                } else {
                                    Text("N/A")
                                }
                            }
                        }
                    }
                }
            }

            // Account Management Section
            item {
                Text("Account Management", fontWeight = FontWeight.Bold)
                Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showEditName = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Edit Name", color = Color.White)
                        }

                        if (userAccountType.contains("email", ignoreCase = true) && !userAccountType.contains("google", ignoreCase = true)) {

                            Button(
                                onClick = onPasswordReset,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Password Reset", color = Color.White)
                            }
                        }

                        Button(
                            onClick = onLogout,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00))
                        ) {
                            Text("Logout", color = Color.White)
                        }


                        Button(
                            onClick = {
                                if (userAccountType.contains("google", ignoreCase = true)) {
                                    activeDeleteAlert = DeleteAlertType.GOOGLE
                                } else if (userAccountType.contains("apple", ignoreCase = true)) {
                                    activeDeleteAlert = DeleteAlertType.APPLE
                                } else {
                                    activeDeleteAlert = DeleteAlertType.EMAIL
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Delete Account", color = Color.White)
                        }
                    }
                }
            }

            // Bot Message Section
            if (botMessage.isNotEmpty()) {
                item {
                    Text("Message", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Text(
                            text = botMessage,
                            color = if (isBotMessageError) Color.Red else Color.Green,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }

    // Edit Name Dialog
    if (showEditName) {
        AlertDialog(
            onDismissRequest = { showEditName = false },
            title = { Text("Edit Name") },
            text = {
                OutlinedTextField(
                    value = editNameInput,
                    onValueChange = { if (it.length <= 18) editNameInput = it },
                    label = { Text("New Name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onEditName(editNameInput)
                        showEditName = false
                    },
                    enabled = editNameInput.isNotBlank() // Add profanity filter logic externally
                ) {
                    Text("Change")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditName = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Standard Delete Alerts (Google / Apple)
    if (activeDeleteAlert == DeleteAlertType.GOOGLE || activeDeleteAlert == DeleteAlertType.APPLE) {
        AlertDialog(
            onDismissRequest = { activeDeleteAlert = null },
            title = { Text("Confirm Deletion") },
            text = { Text("This will permanently delete your account.") },
            confirmButton = {
                Button(
                    onClick = {
                        if (activeDeleteAlert == DeleteAlertType.GOOGLE) {
                            onDeleteAccountGoogle()
                        } else {
                            onDeleteAccountApple()
                        }
                        activeDeleteAlert = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeDeleteAlert = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (activeDeleteAlert == DeleteAlertType.EMAIL) {
        LaunchedEffect(Unit) {
            showEmailDeleteOverlay = true
            activeDeleteAlert = null
        }
    }

    // Email Delete Alert
    if (showEmailDeleteOverlay) {
        AlertDialog(
            onDismissRequest = { showEmailDeleteOverlay = false },
            title = { Text("Confirm Deletion") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("This will permanently delete your account.")
                    OutlinedTextField(
                        value = deletePasswordInput,
                        onValueChange = { deletePasswordInput = it },
                        label = { Text("Password") },
                        // password visual transformation usually done here
                    )
                    OutlinedTextField(
                        value = deleteConfirmPasswordInput,
                        onValueChange = { deleteConfirmPasswordInput = it },
                        label = { Text("Confirm Password") }
                    )
                }
            },
            confirmButton = {
                val valid = deletePasswordInput.isNotBlank() && deletePasswordInput == deleteConfirmPasswordInput
                Button(
                    onClick = {
                        onDeleteAccountEmail(deletePasswordInput)
                        showEmailDeleteOverlay = false
                    },
                    enabled = valid,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showEmailDeleteOverlay = false
                    deletePasswordInput = ""
                    deleteConfirmPasswordInput = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
