package com.garrettbutchko.minimate.content.helper

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.unit.sp
import java.util.regex.Pattern

data class NamedColor(val name: String, val color: Color)

val LocalPlayerColors = listOf(
    NamedColor("red", Color.Red),
    NamedColor("orange", Color(0xFFFFA500)), // Orange
    NamedColor("yellow", Color.Yellow),
    NamedColor("green", Color.Green),
    NamedColor("blue", Color.Blue),
    NamedColor("indigo", Color(0xFF4B0082)), // Indigo
    NamedColor("purple", Color(0xFF800080)), // Purple
    NamedColor("pink", Color(0xFFFFC0CB)), // Pink
    NamedColor("brown", Color(0xFFA52A2A))  // Brown
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
    val isValidEmail = EMAIL_PATTERN.matcher(newPlayerEmail.trim()).matches()
    
    val isDisabled = if (hasCourseSelected) {
        newPlayerName.trim().isEmpty() || newPlayerEmail.trim().isEmpty() || !isValidEmail
    } else {
        newPlayerName.trim().isEmpty()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header Section
            Text(
                text = "Add Local Player",
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Name and Email Row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextField(
                    value = newPlayerName,
                    onValueChange = { if (it.length <= 18) newPlayerName = it },
                    placeholder = { Text("Name") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = if (hasCourseSelected) ImeAction.Next else ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )

                if (hasCourseSelected) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                    TextField(
                        value = newPlayerEmail,
                        onValueChange = { newPlayerEmail = it },
                        placeholder = { Text("Email") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Text(
                text = "Ball Color",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Color Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LocalPlayerColors.forEach { colorOption ->
                    val isSelected = playerBallColor == colorOption.name
                    val animatedColor by animateColorAsState(
                        targetValue = if (isSelected) colorOption.color.copy(alpha = 0.3f) else colorOption.color,
                        animationSpec = tween(durationMillis = 300)
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                            .clickable { playerBallColor = colorOption.name }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(animatedColor)
                        )
                    }
                }
            }

            // Button Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cancel Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                        .clickable { onDismiss() }
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Add Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isDisabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f) 
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                        )
                        .clickable(enabled = !isDisabled) {
                            onAddPlayer(newPlayerName.trim(), newPlayerEmail.trim(), playerBallColor)
                            onDismiss()
                        }
                ) {
                    Text(
                        text = "Add",
                        fontSize = 18.sp,
                        color = if (isDisabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) 
                                else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
