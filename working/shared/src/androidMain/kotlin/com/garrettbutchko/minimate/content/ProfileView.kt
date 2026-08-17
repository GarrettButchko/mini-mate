package com.garrettbutchko.minimate.content

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garrettbutchko.minimate.content.helper.AppAlertDialog
import com.garrettbutchko.minimate.content.helper.AppTextFieldShape
import com.garrettbutchko.minimate.content.helper.InfoRow
import com.garrettbutchko.minimate.content.helper.LoadingOverlay
import com.garrettbutchko.minimate.content.helper.PhotoIconView
import com.garrettbutchko.minimate.content.helper.PhotoPicker
import com.garrettbutchko.minimate.content.helper.SectionCard
import com.garrettbutchko.minimate.content.helper.StatusMessage
import com.garrettbutchko.minimate.content.helper.TransientStatusSignal
import com.garrettbutchko.minimate.content.helper.appTextFieldColors
import com.garrettbutchko.minimate.modelKotlinExtensions.toColor
import com.garrettbutchko.minimate.shared.R
import com.garrettbutchko.minimate.viewModels.DeleteAlertType
import com.garrettbutchko.minimate.viewModels.ProfileViewModel
import com.garrettbutchko.minimate.viewModelsAndroidAddOns.appleReauthAndDelete
import com.garrettbutchko.minimate.viewModelsAndroidAddOns.editName
import com.garrettbutchko.minimate.viewModelsAndroidAddOns.googleReauthAndDelete
import org.koin.compose.koinInject

/**
 * Auth/reauth/delete-account screen shared by both admin-android and user-android — the
 * same VM both iOS apps already consume via ProfileViewModelSwift (see ProfileView.swift
 * in sharedSwift, which mirrors this same "resolve the VM directly in the View" shape
 * rather than splitting a presentational view from a separate VM-wiring host).
 *
 * Resolves ProfileViewModel itself (factory-scoped, matching iOS's per-sheet-mount
 * @StateObject — one koinInject() call here, at the screen root, is the single call site
 * that keeps that safe). Callers only supply what's genuinely per-app: the web client id
 * and debug flag (different R/BuildConfig class per app module), and what "Get Pro" should
 * do. Navigation-on-logout/delete is handled by the VM itself via its own injected
 * viewManager, so callers don't need to pass a navigate-to-sign-in callback.
 *
 * Callers still own the ModalBottomSheet/visibility wrapper — this is just the content.
 */
@Composable
fun ProfileView(
    isMiniMateApp: Boolean = true,
    defaultWebClientId: String,
    isDebug: Boolean = false,
    userNameFallback: String = "User",
    buildTypeLabel: String? = null,
    onDismiss: () -> Unit,
    onGetPro: (() -> Unit)? = null
) {
    val profileVM: ProfileViewModel = koinInject()
    val context = LocalContext.current
    val user by profileVM.authModel.userModel.collectAsState()
    val botMessage by profileVM.botMessage.collectAsState()
    val isBotMessageError by profileVM.isRed.collectAsState()
    val logoutTint = colorResource(id = R.color.swiftBlue)

    // isSheetPresented(false) only ever fires from ProfileViewModel on a *successful*
    // delete (see handleDeleteAccount) — never on failure — so this is exclusively the
    // success signal for all three delete-account paths below.
    val onAccountDeleted: (Boolean) -> Unit = { visible ->
        if (!visible) {
            announceAccountDeleted()
            onDismiss()
        }
    }

    var showEditName by remember { mutableStateOf(false) }
    var editNameInput by remember { mutableStateOf(user?.name ?: userNameFallback) }

    val activeDeleteAlert by profileVM.activeDeleteAlert.collectAsState()
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

    val userProfileImage = user?.photoURL
    val userName = user?.name ?: userNameFallback
    val userEmail = user?.email
    val userGoogleId = user?.googleId ?: ""
    val userIsPro = user?.isPro ?: false
    val userAccountType = user?.accountType?.joinToString(",") ?: ""
    val userBallColor = user?.ballColorDT.toColor()

    PhotoPicker(
        show = showPhotoPicker,
        onImagePicked = { uri ->
            // No Android image-picker wiring yet for this path (matches previous behavior
            // — photo upload is only implemented on iOS today, via managePictureChange).
            if (uri != null) {
                profileVM.setBotMessage("Photo updates are not available here yet.")
                profileVM.setIsRed(true)
            }
        },
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
                                onClick = {
                                    if (onGetPro != null) {
                                        onDismiss()
                                        onGetPro()
                                    }
                                },
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
                            onClick = { user?.let { profileVM.passwordReset(it) } },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = successColor, contentColor = Color.White),
                            shape = RoundedCornerShape(15.dp)
                        ) {
                            Text("Password Reset")
                        }
                    }

                    Button(
                        onClick = {
                            // Posted before navigating away — SignInView picks this up via
                            // ConsumeTransientStatusSignal() once it lands there.
                            TransientStatusSignal.pending = StatusMessage(
                                title = "Logged Out",
                                subtitle = "You've been signed out.",
                                icon = Icons.AutoMirrored.Filled.Logout,
                                tint = logoutTint
                            )
                            onDismiss()
                            profileVM.logOut()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = warningColor, contentColor = Color.White),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Text("Logout")
                    }

                    Button(
                        onClick = { user?.let { profileVM.deleteAccount(it) } },
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
                profileVM.editName(user?.name, editNameInput)
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
            onDismissRequest = { profileVM.setActiveDeleteAlert(null) },
            title = "Confirm Deletion",
            message = "This will permanently delete your account.",
            confirmText = "Delete",
            onConfirm = {
                isDeletingAccount = true
                if (activeDeleteAlert == DeleteAlertType.GOOGLE) {
                    profileVM.googleReauthAndDelete(context, defaultWebClientId, onAccountDeleted)
                } else {
                    profileVM.appleReauthAndDelete(context, onAccountDeleted)
                }
                profileVM.setActiveDeleteAlert(null)
            },
            destructive = true,
            dismissText = "Cancel",
            onDismiss = { profileVM.setActiveDeleteAlert(null) }
        )
    }

    if (activeDeleteAlert == DeleteAlertType.EMAIL) {
        LaunchedEffect(Unit) {
            showEmailDeleteOverlay = true
            profileVM.setActiveDeleteAlert(null)
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
                val email = user?.email
                if (email.isNullOrBlank()) {
                    profileVM.setBotMessage("No email found for this account.")
                    profileVM.setIsRed(true)
                } else {
                    isDeletingAccount = true
                    profileVM.emailReauthAndDelete(email, deletePasswordInput, onAccountDeleted)
                }
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

/**
 * `handleDeleteAccount` in the shared Kotlin VM only invokes this completion on the
 * success path (failures only set botMessage), so any call here means deletion succeeded.
 * Posted via TransientStatusSignal before navigating away, so SignInView shows it once
 * it lands there (same pattern as the logout notice, and as iOS's own
 * ProfileViewModelSwift.announceAccountDeleted()).
 */
private fun announceAccountDeleted() {
    TransientStatusSignal.pending = StatusMessage(
        title = "Account Deleted",
        subtitle = "We're sorry to see you go.",
        icon = Icons.Default.Delete
    )
}
