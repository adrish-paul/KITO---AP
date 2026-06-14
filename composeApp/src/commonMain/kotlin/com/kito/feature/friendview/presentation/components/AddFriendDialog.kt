package com.kito.feature.friendview.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kito.core.designsystem.UIColors
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalHazeMaterialsApi::class,
    ExperimentalHazeApi::class
)
@Composable
fun AddFriendDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    hazeState: HazeState
) {
    val uiColors = UIColors()
    var rollNumber by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        icon = {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = uiColors.progressAccent,
                    modifier = Modifier.height(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add Friend",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        },
        onDismissRequest = onDismiss,
        text = {
            Column {
                OutlinedTextField(
                    value = rollNumber,
                    onValueChange = {
                        rollNumber = it
                        isError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    label = {
                        Text(
                            text = "Friend's Roll Number",
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.titleMediumEmphasized
                        )
                    },
                    placeholder = {
                        Text(
                            text = "e.g., 23053382",
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF8C00),
                        unfocusedBorderColor = Color(0xFF3F3942),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFFFF8C00),
                        cursorColor = Color(0xFFFF8C00),
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.3f)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                if (isError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Please enter a valid roll number",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Enter your friend's roll number to view their schedule.",
                        fontFamily = FontFamily.Monospace,
                        color = uiColors.accentOrangeStart,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    if (rollNumber.isNotBlank()) {
                        onConfirm(rollNumber)
                    } else {
                        isError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = uiColors.progressAccent,
                    contentColor = uiColors.textPrimary
                )
            ) {
                Text(
                    text = "Add",
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = uiColors.progressAccent
                )
            ) {
                Text(
                    text = "Cancel",
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        containerColor = Color.Transparent,
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .shadow(
                elevation = 24.dp,
                spotColor = uiColors.progressAccent
            )
            .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin()) {
                blurRadius = 30.dp
                noiseFactor = 0.00f
                inputScale = HazeInputScale.Auto
                alpha = 0.98f
                tints = listOf(HazeTint(uiColors.cardBackground.copy(alpha = 0.15f)))
            },
    )
}
