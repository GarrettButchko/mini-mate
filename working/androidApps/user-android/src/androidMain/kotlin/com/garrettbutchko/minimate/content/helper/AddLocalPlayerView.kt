package com.garrettbutchko.minimate.content.helper

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.regex.Pattern

data class NamedColor(val name: String, val color: Color)

val LocalPlayerColors = listOf(
    NamedColor("red", Color(0xFFE53935)),
    NamedColor("orange", Color(0xFFFB8C00)),
    NamedColor("yellow", Color(0xFFFDD835)),
    NamedColor("green", Color(0xFF43A047)),
    NamedColor("blue", Color(0xFF1E88E5)),
    NamedColor("indigo", Color(0xFF3949AB)),
    NamedColor("purple", Color(0xFF8E24AA)),
    NamedColor("pink", Color(0xFFD81B60)),
    NamedColor("brown", Color(0xFF6D4C41))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLocalPlayerView(
    hasCourseSelected: Boolean,
    onAddPlayer: (name: String, email: String, color: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var newPlayerName by remember { mutableStateOf("") }
    var newPlayerEmail by remember { mutableStateOf("") }
    var playerBallColor by remember { mutableStateOf<String?>(null) }

    val EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )
    val emailTouched = newPlayerEmail.isNotEmpty()
    val isValidEmail = EMAIL_PATTERN.matcher(newPlayerEmail.trim()).matches()

    val isDisabled = if (hasCourseSelected) {
        newPlayerName.trim().isEmpty() || newPlayerEmail.trim().isEmpty() || !isValidEmail
    } else {
        newPlayerName.trim().isEmpty()
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Add Player",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = newPlayerName,
                onValueChange = { if (it.length <= 18) newPlayerName = it },
                label = { Text("Name") },
                singleLine = true,
                shape = AppTextFieldShape,
                keyboardOptions = KeyboardOptions(
                    imeAction = if (hasCourseSelected) ImeAction.Next else ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (hasCourseSelected) {
                OutlinedTextField(
                    value = newPlayerEmail,
                    onValueChange = { newPlayerEmail = it },
                    label = { Text("Email") },
                    singleLine = true,
                    shape = AppTextFieldShape,
                    isError = emailTouched && !isValidEmail,
                    supportingText = if (emailTouched && !isValidEmail) {
                        { Text("Enter a valid email address") }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Ball Color",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LocalPlayerColors.forEach { colorOption ->
                        val isSelected = playerBallColor == colorOption.name
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(colorOption.color)
                                .clickable { playerBallColor = colorOption.name }
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = AppTextFieldShape
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        onAddPlayer(newPlayerName.trim(), newPlayerEmail.trim(), playerBallColor)
                        onDismiss()
                    },
                    enabled = !isDisabled,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = AppTextFieldShape
                ) {
                    Text("Add Player")
                }
            }
        }
    }
}
