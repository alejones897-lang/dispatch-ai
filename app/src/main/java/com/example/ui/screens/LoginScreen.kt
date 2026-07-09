package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DispatchViewModel

@Composable
fun LoginScreen(
    viewModel: DispatchViewModel,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("dispatch.lead@dispatchai.com") }
    var password by remember { mutableStateOf("securepass123") }
    var selectedRole by remember { mutableStateOf("Dispatcher") }
    var isRegisterMode by remember { mutableStateOf(false) }

    val roles = listOf("Dispatcher", "Admin", "Driver", "Fleet Manager")
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                .testTag("auth_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = "Dispatch AI Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DISPATCH AI",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp
                    )
                }

                Text(
                    text = "USA Logistics Co-Pilot & Fleet Suite",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Semi-Truck Illustration drawing on Canvas
                SemiTruckCanvasIllustration()

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isRegisterMode) "Create Dispatcher Account" else "Secure Driver & Fleet Login",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Input Fields
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("email_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Role Selection Heading
                Text(
                    text = "Select Operations Role:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Role Options Grid / Column
                Column(
                    modifier = Modifier
                        .selectableGroup()
                        .fillMaxWidth()
                ) {
                    roles.chunked(2).forEach { rowRoles ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowRoles.forEach { role ->
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .selectable(
                                            selected = (selectedRole == role),
                                            onClick = { selectedRole = role },
                                            role = Role.RadioButton
                                        )
                                        .padding(vertical = 4.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (selectedRole == role),
                                        onClick = { selectedRole = role },
                                        modifier = Modifier.testTag("role_radio_$role")
                                    )
                                    Text(
                                        text = role,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (isRegisterMode) {
                            viewModel.register(email, selectedRole)
                        } else {
                            viewModel.login(email, selectedRole)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (isRegisterMode) "Register Team Member" else "Launch Dispatch Suite",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mode switcher
                TextButton(
                    onClick = { isRegisterMode = !isRegisterMode },
                    modifier = Modifier.testTag("switch_auth_mode_button")
                ) {
                    Text(
                        text = if (isRegisterMode) "Already have an account? Sign In" else "New to Fleet? Register Operations",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
fun SemiTruckCanvasIllustration() {
    val outlineColor = MaterialTheme.colorScheme.primary
    val wheelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
    val bodyColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(vertical = 8.dp)
    ) {
        val width = size.width
        val height = size.height

        // Clean Canvas Ground Line
        drawLine(
            color = wheelColor,
            start = Offset(0f, height * 0.8f),
            end = Offset(width, height * 0.8f),
            strokeWidth = 2f
        )

        // Draw Trailer Box (left part)
        drawRect(
            color = bodyColor,
            topLeft = Offset(width * 0.1f, height * 0.2f),
            size = Size(width * 0.55f, height * 0.5f)
        )
        drawRect(
            color = outlineColor,
            topLeft = Offset(width * 0.1f, height * 0.2f),
            size = Size(width * 0.55f, height * 0.5f),
            style = Stroke(width = 3f)
        )

        // Trailer details (horizontal safety stripes)
        drawLine(
            color = Color.Red.copy(alpha = 0.7f),
            start = Offset(width * 0.1f, height * 0.65f),
            end = Offset(width * 0.65f, height * 0.65f),
            strokeWidth = 4f
        )

        // Draw Truck Cab (right part)
        drawRect(
            color = bodyColor,
            topLeft = Offset(width * 0.66f, height * 0.35f),
            size = Size(width * 0.24f, height * 0.35f)
        )
        drawRect(
            color = outlineColor,
            topLeft = Offset(width * 0.66f, height * 0.35f),
            size = Size(width * 0.24f, height * 0.35f),
            style = Stroke(width = 3f)
        )

        // Nose hood
        drawRect(
            color = bodyColor,
            topLeft = Offset(width * 0.83f, height * 0.48f),
            size = Size(width * 0.08f, height * 0.22f)
        )
        drawRect(
            color = outlineColor,
            topLeft = Offset(width * 0.83f, height * 0.48f),
            size = Size(width * 0.08f, height * 0.22f),
            style = Stroke(width = 3f)
        )

        // Cab Windshield
        drawRect(
            color = outlineColor,
            topLeft = Offset(width * 0.8f, height * 0.38f),
            size = Size(width * 0.06f, height * 0.1f)
        )

        // Wheels
        val wheels = listOf(
            width * 0.18f,
            width * 0.28f,
            width * 0.55f,
            width * 0.72f,
            width * 0.82f
        )
        wheels.forEach { x ->
            drawCircle(
                color = wheelColor,
                radius = 12f,
                center = Offset(x, height * 0.78f)
            )
            drawCircle(
                color = outlineColor,
                radius = 12f,
                center = Offset(x, height * 0.78f),
                style = Stroke(width = 2f)
            )
            drawCircle(
                color = Color.White,
                radius = 4f,
                center = Offset(x, height * 0.78f)
            )
        }
    }
}
