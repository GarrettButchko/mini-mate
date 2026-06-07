package com.garrettbutchko.minimate.content.preViews

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import com.garrettbutchko.minimate.shared.R


import com.garrettbutchko.minimate.viewModels.AuthViewModel
import com.garrettbutchko.minimate.dataModels.gameModels.Game

@Composable
fun SignInView(
    isMiniMateApp: Boolean = true, // Set to false for MANAGER app
    authModel: AuthViewModel? = null,
    guestGame: Game? = null,
    onSignInWithGoogle: () -> Unit,
    onCreateGuestGame: (name: String, email: String) -> Unit,
    guestGameDate: String? = null // Passed down if guest game is found
) {
    var showEmailSignIn by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val gradientColors = if (isMiniMateApp) {
        listOf(
            colorResource(id = R.color.swiftBlue),
            colorResource(id = R.color.swiftGreen)
        )
    } else {
        listOf(
            colorResource(id = R.color.MMMBlue),
            colorResource(id = R.color.MMMGreen)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
            .imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SignInHeader(isMiniMateApp = isMiniMateApp)
            Spacer(modifier = Modifier.weight(1f))
            
            // Card Container
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .clip(RoundedCornerShape(35.dp))
                    .background(colorResource(id = R.color.main).copy(alpha = 0.5f)),
            ) {
                AnimatedContent(
                    targetState = showEmailSignIn,
                    transitionSpec = {
                        slideInVertically(
                            animationSpec = tween(500),
                            initialOffsetY = { it }
                        ) + fadeIn() togetherWith slideOutVertically(
                            animationSpec = tween(500),
                            targetOffsetY = { it }
                        ) + fadeOut()
                    },
                    label = "SignInTransition"
                ) { showEmail ->
                    if (!showEmail) {
                        StartButtons(
                            isMiniMateApp = isMiniMateApp,
                            onShowEmailSignInChange = { showEmailSignIn = it },
                            onSignInWithGoogle = onSignInWithGoogle,
                            onCreateGuestGame = onCreateGuestGame,
                            guestGameDate = guestGameDate
                        )
                    } else {
                        EmailPasswordView(
                            authModel = authModel,
                            guestGame = guestGame,
                            showEmail = showEmailSignIn,
                            onShowEmailChange = { showEmailSignIn = it },
                            email = email,
                            onEmailChange = { email = it },
                            password = password,
                            onPasswordChange = { password = it },
                            confirmPassword = confirmPassword,
                            onConfirmPasswordChange = { confirmPassword = it },
                            onBack = { showEmailSignIn = false }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SignInHeader(isMiniMateApp: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = "Welcome to,",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = if (isMiniMateApp) "Mini Mate" else "Mini Manager",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        val logoRes = if (isSystemInDarkTheme()) {
            R.drawable.logo_image
        } else {
            R.drawable.logo_image_opp
        }
        Image(
            painter = painterResource(id = logoRes),
            contentDescription = "Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(75.dp)
        )
    }
}

@Composable
fun StartButtons(
    isMiniMateApp: Boolean,
    onShowEmailSignInChange: (Boolean) -> Unit,
    onSignInWithGoogle: () -> Unit,
    onCreateGuestGame: (name: String, email: String) -> Unit,
    guestGameDate: String?
) {
    var showGuestAlert by remember { mutableStateOf(false) }
    var guestName by remember { mutableStateOf("") }
    var guestEmail by remember { mutableStateOf("") }

    if (showGuestAlert) {
        AlertDialog(
            onDismissRequest = { showGuestAlert = false },
            title = { Text("Welcome To MiniMate!") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Name is required. Email is optional — used only for course analytics.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = guestName,
                        onValueChange = { if (it.length <= 18) guestName = it },
                        label = { Text("Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = guestEmail,
                        onValueChange = { guestEmail = it },
                        label = { Text("Email (Optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                val isFormValid = guestName.isNotBlank() && 
                    (guestEmail.isBlank() || Patterns.EMAIL_ADDRESS.matcher(guestEmail).matches())
                
                Button(
                    onClick = {
                        onCreateGuestGame(guestName, guestEmail)
                        showGuestAlert = false
                    },
                    enabled = isFormValid
                ) {
                    Text("Play")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showGuestAlert = false
                    guestName = ""
                    guestEmail = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (isMiniMateApp && guestGameDate != null) {
            // Guest Game Info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), 
                        RoundedCornerShape(25.dp)
                    )
                    .padding(vertical = 18.dp, horizontal = 22.dp)
            ) {
                Column {
                    Text("Save Guest-Play Game?", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Sign in to save it to your profile.", 
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Game played on: $guestGameDate", 
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }

        if (isMiniMateApp) {
            Button(
                onClick = { showGuestAlert = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.main).copy(alpha = 0.5f),
                    contentColor = colorResource(id = R.color.mainOpp)
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Guest Play", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Button(
            onClick = { onShowEmailSignInChange(true) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.swiftGreen))
        ) {
            Icon(Icons.Default.Email, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Sign in with Email", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }

        Button(
            onClick = onSignInWithGoogle,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.swiftBlue))
        ) {
            Box (modifier = Modifier
                .background(
                    Color.White,
                    CircleShape
                )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.google),
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(23.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text("Sign in with Google", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
