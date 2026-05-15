package com.garrettbutchko.minimate.content

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.colorResource
import coil.compose.AsyncImage
import com.garrettbutchko.minimate.shared.R
import com.garrettbutchko.minimate.content.helper.UserInfoRow

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
    isDebug: Boolean = false,
    botMessage: String = "",
    isBotMessageError: Boolean = false,
    onPhotoChange: (Uri) -> Unit,
    onGetPro: () -> Unit,
    onEditName: (String) -> Unit,
    onPasswordReset: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccountGoogle: () -> Unit,
    onDeleteAccountApple: () -> Unit,
    onDeleteAccountEmail: (password: String) -> Unit
) {
    var showEditName by remember { mutableStateOf(false) }
    var editNameInput by remember { mutableStateOf(userName) }
    
    var activeDeleteAlert by remember { mutableStateOf<DeleteAlertType?>(null) }
    var showEmailDeleteOverlay by remember { mutableStateOf(false) }
    var deletePasswordInput by remember { mutableStateOf("") }
    var deleteConfirmPasswordInput by remember { mutableStateOf("") }

    val bgColor = colorResource(id = R.color.bg)
    val mainOppColor = colorResource(id = R.color.mainOpp)
    val subColor = colorResource(id = R.color.sub)
    val subOppColor = colorResource(id = R.color.subOpp)
    val subThreeOppColor = colorResource(id = R.color.subThreeOpp)
    val successColor = colorResource(id = R.color.swiftBlue)
    val warningColor = colorResource(id = R.color.swiftOrange)
    val dangerColor = colorResource(id = R.color.swiftRed)

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { onPhotoChange(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
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
                color = mainOppColor
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tap to change photo", fontSize = 12.sp, color = mainOppColor)
                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(subColor)
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
                        Text("", fontSize = 10.sp, color = mainOppColor)
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
                Text("User Details", fontWeight = FontWeight.Bold, color = mainOppColor)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = subColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        UserInfoRow(label = "Name", value = userName, mainOppColor = mainOppColor, subOppColor = subOppColor)
                        HorizontalDivider()
                        UserInfoRow(label = "Email", value = userEmail ?: "N/A", mainOppColor = mainOppColor, subOppColor = subOppColor)
                        HorizontalDivider()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Pro:", style = MaterialTheme.typography.bodyMedium, color = mainOppColor, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.weight(1f))
                            Text(if (userIsPro) "Yes" else "Not Yet!", style = MaterialTheme.typography.bodyMedium, color = subOppColor, fontWeight = FontWeight.Medium)
                            if (isMiniMateApp && !userIsPro) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = onGetPro,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colorResource(id = R.color.swiftBlue),
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                    modifier = Modifier.heightIn(min = 32.dp)
                                ) {
                                    Text("Get Pro Now!", fontSize = 12.sp)
                                }
                            }
                        }
                        if (isDebug) {
                            HorizontalDivider()
                            UserInfoRow(label = "Build", value = buildTypeLabel ?: "debug", mainOppColor = mainOppColor, subOppColor = subThreeOppColor)
                            HorizontalDivider()
                            UserInfoRow(label = "UID", value = userGoogleId, mainOppColor = mainOppColor, subOppColor = subThreeOppColor)
                            HorizontalDivider()
                            UserInfoRow(label = "PhotoURL", value = userProfileImage ?: "N/A", mainOppColor = mainOppColor, subOppColor = subThreeOppColor)
                        }
                    }
                }
            }

            // Account Management Section
            item {
                Text("Account Management", fontWeight = FontWeight.Bold, color = mainOppColor)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = subColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

                        Button(
                            onClick = { showEditName = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = successColor, contentColor = Color.White)
                        ) {
                            Text("Edit Name")
                        }

                        if (userAccountType.contains("email", ignoreCase = true) && !userAccountType.contains("google", ignoreCase = true)) {
                            Button(
                                onClick = onPasswordReset,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = successColor, contentColor = Color.White)
                            ) {
                                Text("Password Reset")
                            }
                        }

                        Button(
                            onClick = onLogout,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = warningColor, contentColor = Color.White)
                        ) {
                            Text("Logout")
                        }

                        Button(
                            onClick = {
                                activeDeleteAlert =
                                    if (userAccountType.contains("google", ignoreCase = true)) {
                                        DeleteAlertType.GOOGLE
                                    } else if (userAccountType.contains("apple", ignoreCase = true)) {
                                        DeleteAlertType.APPLE
                                    } else {
                                        DeleteAlertType.EMAIL
                                    }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = dangerColor, contentColor = Color.White)
                        ) {
                            Text("Delete Account")
                        }

                    }
                }
            }

            // Bot Message Section
            item {
                AnimatedVisibility(
                    visible = botMessage.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Message", fontWeight = FontWeight.Bold, color = mainOppColor)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = (if (isBotMessageError) dangerColor else successColor).copy(alpha = 0.1f)
                            )
                        ) {
                            Text(
                                text = botMessage,
                                color = if (isBotMessageError) dangerColor else successColor,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
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
                    label = { Text("New Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = subColor,
                        unfocusedContainerColor = subColor,
                        focusedTextColor = mainOppColor,
                        unfocusedTextColor = mainOppColor,
                        focusedLabelColor = mainOppColor,
                        unfocusedLabelColor = subThreeOppColor,
                        cursorColor = successColor,
                        focusedBorderColor = successColor,
                        unfocusedBorderColor = subThreeOppColor
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onEditName(editNameInput)
                        showEditName = false
                    },
                    enabled = editNameInput.isNotBlank(), // Add profanity filter logic externally
                    colors = ButtonDefaults.buttonColors(containerColor = successColor, contentColor = Color.White)
                ) {
                    Text("Change")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditName = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = mainOppColor)
                ) {
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
            text = { Text("This will permanently delete your account.", color = mainOppColor) },
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
                    colors = ButtonDefaults.buttonColors(containerColor = dangerColor, contentColor = Color.White)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { activeDeleteAlert = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = mainOppColor)
                ) {
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
                    Text("This will permanently delete your account.", color = mainOppColor)
                    OutlinedTextField(
                        value = deletePasswordInput,
                        onValueChange = { deletePasswordInput = it },
                        label = { Text("Password") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = subColor,
                            unfocusedContainerColor = subColor,
                            focusedTextColor = mainOppColor,
                            unfocusedTextColor = mainOppColor,
                            focusedLabelColor = mainOppColor,
                            unfocusedLabelColor = subThreeOppColor,
                            cursorColor = dangerColor,
                            focusedBorderColor = dangerColor,
                            unfocusedBorderColor = subThreeOppColor
                        )
                    )
                    OutlinedTextField(
                        value = deleteConfirmPasswordInput,
                        onValueChange = { deleteConfirmPasswordInput = it },
                        label = { Text("Confirm Password") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = subColor,
                            unfocusedContainerColor = subColor,
                            focusedTextColor = mainOppColor,
                            unfocusedTextColor = mainOppColor,
                            focusedLabelColor = mainOppColor,
                            unfocusedLabelColor = subThreeOppColor,
                            cursorColor = dangerColor,
                            focusedBorderColor = dangerColor,
                            unfocusedBorderColor = subThreeOppColor
                        )
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
                    colors = ButtonDefaults.buttonColors(containerColor = dangerColor, contentColor = Color.White)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEmailDeleteOverlay = false
                        deletePasswordInput = ""
                        deleteConfirmPasswordInput = ""
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = mainOppColor)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
