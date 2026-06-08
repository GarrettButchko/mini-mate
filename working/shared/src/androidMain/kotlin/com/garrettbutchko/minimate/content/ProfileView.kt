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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.colorResource
import com.garrettbutchko.minimate.shared.R
import com.garrettbutchko.minimate.content.helper.InfoRow
import com.garrettbutchko.minimate.content.helper.PhotoIconView
import com.garrettbutchko.minimate.content.helper.PhotoPicker
import com.garrettbutchko.minimate.content.helper.SectionCard

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
    userBallColor: Color? = null,
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
    var showPhotoPicker by remember { mutableStateOf(false) }

    val bgColor = colorResource(id = R.color.bg)
    val mainOppColor = colorResource(id = R.color.mainOpp)
    val subColor = colorResource(id = R.color.sub)
    val subOppColor = colorResource(id = R.color.subOpp)
    val subThreeOppColor = colorResource(id = R.color.subThreeOpp)
    val successColor = colorResource(id = R.color.swiftBlue)
    val warningColor = colorResource(id = R.color.swiftOrange)
    val dangerColor = colorResource(id = R.color.swiftRed)

    PhotoPicker(
        show = showPhotoPicker,
        onImagePicked = { uri -> uri?.let { onPhotoChange(it) } },
        onDismiss = { showPhotoPicker = false }
    )

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

                PhotoIconView(
                    photoURL = userProfileImage,
                    name = userName,
                    ballColor = userBallColor,
                    imageSize = 40.dp,
                    backgroundColor = subColor,
                    showName = false,
                    modifier = Modifier.clickable {
                        showPhotoPicker = true
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Details Section
            item {
                SectionCard(title = "User Details") {
                    InfoRow(label = "Name", value = userName, mainOppColor = mainOppColor, subOppColor = subOppColor)
                    HorizontalDivider(color = mainOppColor.copy(alpha = 0.1f))
                    InfoRow(label = "Email", value = userEmail ?: "N/A", mainOppColor = mainOppColor, subOppColor = subOppColor)
                    HorizontalDivider(color = mainOppColor.copy(alpha = 0.1f))
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
                        HorizontalDivider(color = mainOppColor.copy(alpha = 0.1f))
                        InfoRow(label = "Build", value = buildTypeLabel ?: "debug", mainOppColor = mainOppColor, subOppColor = subThreeOppColor)
                        HorizontalDivider(color = mainOppColor.copy(alpha = 0.1f))
                        InfoRow(label = "UID", value = userGoogleId, mainOppColor = mainOppColor, subOppColor = subThreeOppColor)
                        HorizontalDivider(color = mainOppColor.copy(alpha = 0.1f))
                        InfoRow(label = "PhotoURL", value = userProfileImage ?: "N/A", mainOppColor = mainOppColor, subOppColor = subThreeOppColor)
                    }
                }
            }

            // Account Management Section
            item {
                SectionCard(title = "Account Management") {
                    Button(
                        onClick = { showEditName = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = successColor, contentColor = Color.White),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Text("Edit Name")
                    }

                    if (userAccountType.contains("email", ignoreCase = true) && !userAccountType.contains("google", ignoreCase = true)) {
                        Button(
                            onClick = onPasswordReset,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = successColor, contentColor = Color.White),
                            shape = RoundedCornerShape(15.dp)
                        ) {
                            Text("Password Reset")
                        }
                    }

                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = warningColor, contentColor = Color.White),
                        shape = RoundedCornerShape(15.dp)
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
                        colors = ButtonDefaults.buttonColors(containerColor = dangerColor, contentColor = Color.White),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Text("Delete Account")
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
                        SectionCard(title = "Message") {
                            Text(
                                text = botMessage,
                                color = if (isBotMessageError) dangerColor else successColor,
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
                    label = { Text("New Name") }
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
