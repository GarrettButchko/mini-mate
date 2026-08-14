package com.garrettbutchko.minimate.content

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.garrettbutchko.minimate.content.helper.AppAlertDialog
import com.garrettbutchko.minimate.content.helper.AppTextFieldShape
import com.garrettbutchko.minimate.content.helper.appTextFieldColors
import com.garrettbutchko.minimate.content.helper.InfoRow
import com.garrettbutchko.minimate.content.helper.PhotoIconView
import com.garrettbutchko.minimate.content.helper.PhotoPicker
import com.garrettbutchko.minimate.content.helper.LoadingOverlay
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

    // Mirrors iOS `isDeletingAccount`: drives the "Deleting your account..." popup.
    var isDeletingAccount by remember { mutableStateOf(false) }

    // iOS clears `isDeletingAccount` when a botMessage lands (i.e. an error), so the
    // popup animates back out on failure instead of hanging.
    LaunchedEffect(botMessage) {
        if (botMessage.isNotEmpty()) isDeletingAccount = false
    }

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

    Box(modifier = Modifier.fillMaxSize()) {
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

    LoadingOverlay(visible = isDeletingAccount, message = "Deleting your account...")
    }

    // Edit Name Dialog
    if (showEditName) {
        AppAlertDialog(
            onDismissRequest = { showEditName = false },
            title = "Edit Name",
            confirmText = "Change",
            onConfirm = {
                onEditName(editNameInput)
                showEditName = false
            },
            confirmEnabled = editNameInput.isNotBlank(),
            dismissText = "Cancel",
            onDismiss = { showEditName = false },
            content = {
                OutlinedTextField(
                    value = editNameInput,
                    onValueChange = { if (it.length <= 18) editNameInput = it },
                    label = { Text("New Name") },
                    shape = AppTextFieldShape,
                    colors = appTextFieldColors()
                )
            }
        )
    }

    // Standard Delete Alerts (Google / Apple)
    if (activeDeleteAlert == DeleteAlertType.GOOGLE || activeDeleteAlert == DeleteAlertType.APPLE) {
        AppAlertDialog(
            onDismissRequest = { activeDeleteAlert = null },
            title = "Confirm Deletion",
            message = "This will permanently delete your account.",
            confirmText = "Delete",
            onConfirm = {
                isDeletingAccount = true
                if (activeDeleteAlert == DeleteAlertType.GOOGLE) {
                    onDeleteAccountGoogle()
                } else {
                    onDeleteAccountApple()
                }
                activeDeleteAlert = null
            },
            destructive = true,
            dismissText = "Cancel",
            onDismiss = { activeDeleteAlert = null }
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
        AppAlertDialog(
            onDismissRequest = { showEmailDeleteOverlay = false },
            title = "Confirm Deletion",
            message = "This will permanently delete your account.",
            confirmText = "Delete",
            onConfirm = {
                isDeletingAccount = true
                onDeleteAccountEmail(deletePasswordInput)
                showEmailDeleteOverlay = false
            },
            confirmEnabled = deletePasswordInput.isNotBlank() && deletePasswordInput == deleteConfirmPasswordInput,
            destructive = true,
            dismissText = "Cancel",
            onDismiss = {
                showEmailDeleteOverlay = false
                deletePasswordInput = ""
                deleteConfirmPasswordInput = ""
            },
            content = {
                OutlinedTextField(
                    value = deletePasswordInput,
                    onValueChange = { deletePasswordInput = it },
                    label = { Text("Password") },
                    shape = AppTextFieldShape,
                    colors = appTextFieldColors()
                )
                OutlinedTextField(
                    value = deleteConfirmPasswordInput,
                    onValueChange = { deleteConfirmPasswordInput = it },
                    label = { Text("Confirm Password") },
                    shape = AppTextFieldShape,
                    colors = appTextFieldColors()
                )
            }
        )
    }
}
