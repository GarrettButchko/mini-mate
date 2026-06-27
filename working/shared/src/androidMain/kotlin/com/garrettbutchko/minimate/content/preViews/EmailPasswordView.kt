package com.garrettbutchko.minimate.content.preViews

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import com.garrettbutchko.minimate.viewModels.AuthViewModel
import com.garrettbutchko.minimate.dataModels.gameModels.Game
import androidx.compose.ui.res.colorResource
import com.garrettbutchko.minimate.shared.R
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmailPasswordView(
    authModel: AuthViewModel?,
    guestGame: Game?,
    onShowEmailChange: (Boolean) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    initialShowSignUp: Boolean = false,
    isDebug: Boolean = false,
    onBack: () -> Unit
) {
    var showSignUp by remember { mutableStateOf(initialShowSignUp) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isErrorPositive by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Retains the last non-null message so the text stays visible during the exit animation
    var lastErrorMessage by remember { mutableStateOf("") }
    var lastIsErrorPositive by remember { mutableStateOf(false) }
    if (errorMessage != null) {
        lastErrorMessage = errorMessage!!
        lastIsErrorPositive = isErrorPositive
    }

    // Hold the spinner until the error banner has had time to animate into view
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            kotlinx.coroutines.delay(250)
            isLoading = false
        }
    }

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

    Box(modifier = Modifier.fillMaxSize()) {
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
                color = colorResource(id = R.color.mainOpp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter email and password",
                fontSize = 20.sp,
                fontWeight = FontWeight.Light,
                color = colorResource(id = R.color.subTwoOpp)
            )

            Spacer(modifier = Modifier.height(50.dp))

            // Form Fields — spacers live inside AnimatedVisibility so they collapse with the content
            Column {
                EmailSection(
                    email = email,
                    onEmailChange = onEmailChange,
                    focusRequester = emailFocus
                )

                Spacer(modifier = Modifier.height(20.dp))

                PasswordSection(
                    title = "Password",
                    password = password,
                    onPasswordChange = {
                        if (it.length <= characterLimit) onPasswordChange(it)
                    },
                    focusRequester = passwordFocus
                )

                AnimatedVisibility(
                    visible = showSignUp,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(20.dp))
                        PasswordSection(
                            title = "Confirm Password",
                            password = confirmPassword,
                            onPasswordChange = {
                                if (it.length <= characterLimit) onConfirmPasswordChange(it)
                            },
                            focusRequester = confirmFocus
                        )
                    }
                }

                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(20.dp))
                        val swiftGreen = colorResource(id = R.color.swiftGreen)
                        val swiftRed = colorResource(id = R.color.swiftRed)
                        val subOpp = MaterialTheme.colorScheme.onSurfaceVariant
                        val baseColor = if (lastIsErrorPositive) swiftGreen else swiftRed
                        val textColor = lerp(baseColor, subOpp, 0.5f)
                        Text(
                            text = lastErrorMessage,
                            color = textColor,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    baseColor.copy(alpha = 0.25f),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Submit Button
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        isLoading = true
                        if (!showSignUp) {
                            authModel?.signInUIManage(
                                emailInput = email,
                                passwordInput = password,
                                guestGame = guestGame,
                                onClearForm = {
                                    onEmailChange("")
                                    onPasswordChange("")
                                    onConfirmPasswordChange("")
                                    isLoading = false
                                },
                                onShowSignUp = {
                                    showSignUp = true
                                    isLoading = false
                                },
                                onErrorMessage = { msg, type ->
                                    errorMessage = msg
                                    isErrorPositive = type
                                },
                                onClearGuestGame = { /* Handled in AuthViewModel / Repo */ }
                            )
                        } else {
                            authModel?.signUpUIManage(
                                emailInput = email,
                                passwordInput = password,
                                guestGame = guestGame,
                                onClearForm = {
                                    onEmailChange("")
                                    onPasswordChange("")
                                    onConfirmPasswordChange("")
                                    isLoading = false
                                },
                                onSuccess = {
                                    showSignUp = false
                                    isLoading = false
                                },
                                onErrorMessage = { msg, type ->
                                    errorMessage = msg
                                    isErrorPositive = type
                                },
                                onClearGuestGame = { /* Handled in AuthViewModel / Repo */ }
                            )
                        }
                    },
                    enabled = !disabled && !isLoading,
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.swiftGreen),
                        disabledContainerColor = colorResource(id = R.color.swiftGreen).copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .height(50.dp)
                        .width(150.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (showSignUp) "Sign Up" else "Login",
                            color = Color.White
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
                .background(colorResource(id = R.color.sub).copy(alpha = 0.7f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colorResource(id = R.color.swiftGreen)
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
        Text(text = "Email", color = colorResource(id = R.color.subTwoOpp))
        TextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = { Text("example@example.com", color = colorResource(id = R.color.subTwoOpp).copy(alpha = 0.5f)) },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = colorResource(id = R.color.subTwoOpp)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colorResource(id = R.color.sub).copy(alpha = 0.7f),
                unfocusedContainerColor = colorResource(id = R.color.sub).copy(alpha = 0.7f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = colorResource(id = R.color.mainOpp),
                unfocusedTextColor = colorResource(id = R.color.mainOpp),
                cursorColor = colorResource(id = R.color.swiftGreen)
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
        Text(text = title, color = colorResource(id = R.color.subTwoOpp))
        TextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = { Text("••••••", color = colorResource(id = R.color.subTwoOpp).copy(alpha = 0.5f)) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = colorResource(id = R.color.subTwoOpp)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colorResource(id = R.color.sub).copy(alpha = 0.7f),
                unfocusedContainerColor = colorResource(id = R.color.sub).copy(alpha = 0.7f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = colorResource(id = R.color.mainOpp),
                unfocusedTextColor = colorResource(id = R.color.mainOpp),
                cursorColor = colorResource(id = R.color.swiftGreen)
            ),
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
    }
}
