package com.garrettbutchko.minimate.content.preViews

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class Field {
    Email, Password, Confirm
}

@Composable
fun EmailPasswordView(
    // authModel: AuthViewModel, // Pass your ViewModel here
    showEmail: Boolean,
    onShowEmailChange: (Boolean) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    // guestGame: Game?,
    // onGuestGameChange: (Game?) -> Unit,
    initialShowSignUp: Boolean = false,
    onBack: () -> Unit
) {
    var showSignUp by remember { mutableStateOf(initialShowSignUp) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isErrorPositive by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    val emailFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    val confirmFocus = remember { FocusRequester() }

    val characterLimit = 15

    val disabled = if (!showSignUp) {
        email.isEmpty() || password.isEmpty()
    } else {
        email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || password != confirmPassword
    }

    Box(modifier = Modifier.fillMaxSize().imePadding()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(90.dp))

            // Title
            Text(
                text = if (showSignUp) "Sign Up" else "Login",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter email and password",
                fontSize = 20.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(50.dp))

            // Form Fields
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                // Email Field
                EmailSection(
                    email = email,
                    onEmailChange = onEmailChange,
                    focusRequester = emailFocus
                )

                // Password Field
                PasswordSection(
                    title = "Password",
                    password = password,
                    onPasswordChange = {
                        if (it.length <= characterLimit) onPasswordChange(it)
                    },
                    focusRequester = passwordFocus
                )

                if (showSignUp) {
                    // Confirm Password Field
                    PasswordSection(
                        title = "Confirm Password",
                        password = confirmPassword,
                        onPasswordChange = {
                            if (it.length <= characterLimit) onConfirmPasswordChange(it)
                        },
                        focusRequester = confirmFocus
                    )
                }

                // Submit Button
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (!showSignUp) {
                            // authModel.signInUIManage(...)
                        } else {
                            // authModel.signUpUIManage(...)
                        }
                    },
                    enabled = !disabled,
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Green,
                        disabledContainerColor = Color.Green.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .height(50.dp)
                        .width(150.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = if (showSignUp) "Sign Up" else "Login",
                        color = Color.White
                    )
                }

                // Error Message
                errorMessage?.let { msg ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isErrorPositive) Color.Green.copy(alpha = 0.2f) 
                                else Color.Red.copy(alpha = 0.2f)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = msg,
                            color = if (isErrorPositive) Color.Green else Color.Red,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(50.dp))
        }

        // Back Button
        IconButton(
            onClick = {
                focusManager.clearFocus()
                onShowEmailChange(false)
                showSignUp = false
                onEmailChange("")
                onPasswordChange("")
                onConfirmPasswordChange("")
                errorMessage = null
                onBack()
            },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.Green
            )
        }
    }
}

@Composable
fun EmailSection(
    email: String,
    onEmailChange: (String) -> Unit,
    focusRequester: FocusRequester
) {
    Column {
        Text(text = "Email", color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = { Text("example@example.com") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
    }
}

@Composable
fun PasswordSection(
    title: String,
    password: String,
    onPasswordChange: (String) -> Unit,
    focusRequester: FocusRequester
) {
    Column {
        Text(text = title, color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = { Text("••••••") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
    }
}
