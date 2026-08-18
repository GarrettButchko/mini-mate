package com.garrettbutchko.minimate.content.preViews

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import com.garrettbutchko.minimate.viewModels.AuthFlowCallbacks
import com.garrettbutchko.minimate.viewModels.AuthViewModel
import com.garrettbutchko.minimate.dataModels.gameModels.Game
import androidx.compose.ui.res.colorResource
import com.garrettbutchko.minimate.shared.R
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

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
    isMiniMateApp: Boolean = true,
    onBack: () -> Unit
) {
    var showSignUp by remember { mutableStateOf(initialShowSignUp) }
    var showVerifyEmail by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isErrorPositive by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val brandColor = colorResource(id = if (isMiniMateApp) R.color.swiftGreen else R.color.swiftPurple)

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
            delay(250.milliseconds)
            isLoading = false
        }
    }

    // Mirror AuthViewModel's one-shot status events (sign-in/sign-up errors, previously
    // an onErrorMessage callback) into this screen's local error state, consuming each
    // one right after it's picked up.
    val authEvent by authModel?.authEvent?.collectAsState() ?: remember { mutableStateOf(null) }
    LaunchedEffect(authEvent) {
        val event = authEvent
        if (event != null) {
            errorMessage = event.message
            isErrorPositive = event.isError
            authModel?.consumeAuthEvent()
        }
    }

    LaunchedEffect(showSignUp, showVerifyEmail) { isLoading = false }

    /** Verification finished: drop the session and reset back to a clean Login form. */
    fun finishVerification() {
        authModel?.logout()
        showVerifyEmail = false
        showSignUp = false
        onEmailChange("")
        onPasswordChange("")
        onConfirmPasswordChange("")
        errorMessage = null
        isLoading = false
    }

    // Poll while the verify screen is up so the user can just tap the emailed
    // link and come back — no "Done Verifying" tap required.
    LaunchedEffect(showVerifyEmail) {
        if (!showVerifyEmail) return@LaunchedEffect
        while (true) {
            delay(3.seconds)
            if (authModel?.refreshVerificationStatus() == true) {
                finishVerification()
                break
            }
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

            AnimatedContent(
                targetState = showVerifyEmail,
                transitionSpec = {
                    (slideInVertically { -it } + fadeIn()) togetherWith
                        (slideOutVertically { -it } + fadeOut())
                },
                label = "emailPasswordSection"
            ) { verifying ->
                if (verifying) {
                    VerifySection(
                        email = email,
                        brandColor = brandColor,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        isErrorPositive = isErrorPositive,
                        onDoneVerifying = {
                            isLoading = true
                            scope.launch {
                                if (authModel?.refreshVerificationStatus() == true) {
                                    finishVerification()
                                } else {
                                    errorMessage = "Not verified yet — check your inbox and try again."
                                    isErrorPositive = false
                                }
                                isLoading = false
                            }
                        },
                        onResendEmail = {
                            scope.launch {
                                val sent = authModel?.resendVerificationEmail() == true
                                errorMessage = if (sent) "Verification email sent." else "Couldn't send email. Try again."
                                isErrorPositive = sent
                            }
                        }
                    )
                } else {
                    // Title + form fields. Spacers live inside AnimatedVisibility so
                    // they collapse together with the content they belong to.
                    Column {
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
                                        // Email is kept so the verify screen can name the address
                                        callbacks = AuthFlowCallbacks(
                                            onClearForm = {
                                                onPasswordChange("")
                                                onConfirmPasswordChange("")
                                                isLoading = false
                                            },
                                            onShowSignUp = {
                                                showSignUp = true
                                                isLoading = false
                                            },
                                            onClearGuestGame = { /* Handled in AuthViewModel / Repo */ },
                                            onShowVerifyEmail = {
                                                errorMessage = null
                                                isErrorPositive = false
                                                showSignUp = false
                                                showVerifyEmail = true
                                            }
                                        ),
                                        // Stay signed in so refreshVerificationStatus() has a
                                        // session to poll; finishVerification logs out after.
                                        keepSignedIn = true
                                    )
                                } else {
                                    authModel?.signUpUIManage(
                                        emailInput = email,
                                        passwordInput = password,
                                        guestGame = guestGame,
                                        callbacks = AuthFlowCallbacks(
                                            onClearForm = {
                                                onPasswordChange("")
                                                onConfirmPasswordChange("")
                                                isLoading = false
                                            },
                                            onSuccess = {
                                                showSignUp = false
                                                isLoading = false
                                            },
                                            onClearGuestGame = { /* Handled in AuthViewModel / Repo */ },
                                            onShowVerifyEmail = {
                                                errorMessage = null
                                                isErrorPositive = false
                                                showSignUp = false
                                                showVerifyEmail = true
                                            }
                                        ),
                                        keepSignedIn = true
                                    )
                                }
                            },
                            enabled = !disabled && !isLoading,
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = if (isMiniMateApp) R.color.swiftGreen else R.color.swiftPurple),
                                disabledContainerColor = colorResource(id = if (isMiniMateApp) R.color.swiftGreen else R.color.swiftPurple).copy(alpha = 0.5f)
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // Bottom sign up / log in toggle (like the web version)
                        TextButton(
                            onClick = {
                                if (showSignUp) onConfirmPasswordChange("")
                                showSignUp = !showSignUp
                                errorMessage = null
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(color = colorResource(id = R.color.subTwoOpp))) {
                                        append(if (showSignUp) "Already have an account? " else "Don't have an account? ")
                                    }
                                    withStyle(SpanStyle(color = brandColor, fontWeight = FontWeight.SemiBold)) {
                                        append(if (showSignUp) "Login" else "Sign Up")
                                    }
                                },
                                fontSize = 12.sp
                            )
                        }
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
                showVerifyEmail = false
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
                tint = colorResource(id = if (isMiniMateApp) R.color.swiftGreen else R.color.swiftPurple)
            )
        }
    }
}

/**
 * Shown after sign-in/sign-up when the account's email is still unverified.
 * The caller polls `refreshVerificationStatus()` in the background, so this is
 * mostly a holding screen — "Done Verifying" just short-circuits the poll.
 */
@Composable
private fun VerifySection(
    email: String,
    brandColor: Color,
    isLoading: Boolean,
    errorMessage: String?,
    isErrorPositive: Boolean,
    onDoneVerifying: () -> Unit,
    onResendEmail: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Icon(
            imageVector = Icons.Default.MarkEmailUnread,
            contentDescription = null,
            tint = brandColor,
            modifier = Modifier.size(44.dp)
        )

        Text(
            text = "Please Verify Your Email",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorResource(id = R.color.mainOpp)
        )

        Text(
            text = "We sent a verification link to ${email.ifBlank { "your email" }}. " +
                "Verify it, then come back here to continue.",
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            color = colorResource(id = R.color.subTwoOpp),
            textAlign = TextAlign.Center
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                fontSize = 12.sp,
                color = colorResource(id = if (isErrorPositive) R.color.swiftGreen else R.color.swiftRed),
                textAlign = TextAlign.Center
            )
        }

        Button(
            onClick = onDoneVerifying,
            enabled = !isLoading,
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = brandColor,
                disabledContainerColor = brandColor.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .height(50.dp)
                .width(150.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(text = "Done Verifying", color = Color.White)
            }
        }

        TextButton(onClick = onResendEmail) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = null,
                tint = brandColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Resend Email",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = brandColor
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
