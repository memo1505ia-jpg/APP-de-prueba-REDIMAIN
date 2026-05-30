package com.example

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.REDIMAINViewModel

@Composable
fun UserLoginScreen(
    viewModel: REDIMAINViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isSignUpMode by remember { mutableStateOf(false) }
    
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var verifCodeInput by remember { mutableStateOf("") }
    
    var isCodeSent by remember { mutableStateOf(false) }

    val gradientBg = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F172A), Color(0xFF020617)) // Slate dark gradient
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradientBg)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Military Crest & Heading
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0x22F59E0B), RoundedCornerShape(16.dp))
                    .border(2.dp, Color(0xFFF59E0B), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Escudo REDIMAIN",
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(44.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "REDIMAIN",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                letterSpacing = 2.sp
            )
            
            Text(
                text = "Región de Defensa Integral Marítima e Insular",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(28.dp))


            // Authentication Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B132B)),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isSignUpMode) "Registro con Cuenta de Google" else "Identificación Militar de Guardia",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))

                    if (isSignUpMode) {
                        // Full Name Input (Only on Sign Up)
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Nombre Completo (Rango, Apellido)", color = Color(0xFF64748B)) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF64748B)) },
                            textStyle = TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFF59E0B),
                                unfocusedBorderColor = Color(0xFF1E293B)
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Google Email Input
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Correo Google (@gmail.com)") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF64748B)) },
                        textStyle = TextStyle(color = Color.White),
                        placeholder = { Text("ejemplo@gmail.com", color = Color(0xFF475569)) },
                        modifier = Modifier.fillMaxWidth().testTag("email_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFF1E293B),
                            focusedLabelColor = Color(0xFFF59E0B),
                            unfocusedLabelColor = Color(0xFF64748B)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // "Send Code" or verification process
                    var isSending by remember { mutableStateOf(false) }
                    if (!isCodeSent) {
                        Button(
                            onClick = {
                                if (emailInput.isBlank()) {
                                    Toast.makeText(context, "Ingrese su correo Google primero.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val normalized = emailInput.trim().lowercase()
                                if (!normalized.endsWith("@gmail.com") && !normalized.endsWith("@google.com")) {
                                    Toast.makeText(context, "Error: Debe ser un correo electrónico Google válido (@gmail.com / @google.com)", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                if (isSignUpMode && nameInput.isBlank()) {
                                    Toast.makeText(context, "Por favor ingrese su Nombre Completo para registrarse.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                isSending = true
                                viewModel.sendVerificationCode(
                                    email = normalized,
                                    onSuccess = {
                                        isSending = false
                                        isCodeSent = true
                                        Toast.makeText(context, "Código de verificación enviado al correo Google.", Toast.LENGTH_SHORT).show()
                                    },
                                    onFailure = { error ->
                                        isSending = false
                                        Toast.makeText(context, "Error al enviar código: $error", Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            enabled = !isSending,
                            modifier = Modifier.fillMaxWidth().testTag("send_code_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)) // Google Blue
                        ) {
                            if (isSending) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Enviar Código de Verificación", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Enter verification OTP code
                        OutlinedTextField(
                            value = verifCodeInput,
                            onValueChange = { verifCodeInput = it },
                            label = { Text("Código de Seguridad Recibido") },
                            placeholder = { Text("6 dígitos") },
                            leadingIcon = { Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF34D399)) },
                            textStyle = TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth().testTag("verif_code_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF34D399),
                                unfocusedBorderColor = Color(0xFF1E293B),
                                focusedLabelColor = Color(0xFF34D399),
                                unfocusedLabelColor = Color(0xFF64748B)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        var isVerifying by remember { mutableStateOf(false) }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    isCodeSent = false
                                    verifCodeInput = ""
                                },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, Color(0xFF1E293B))
                            ) {
                                Text("Atrás", color = Color(0xFF94A3B8))
                            }

                            Button(
                                onClick = {
                                    if (verifCodeInput.isBlank()) {
                                        Toast.makeText(context, "Ingrese el código recibido.", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isVerifying = true
                                    val emailNormalized = emailInput.trim().lowercase()
                                    viewModel.verifyCode(
                                        email = emailNormalized,
                                        pin = verifCodeInput.trim(),
                                        onSuccess = {
                                            isVerifying = false
                                            if (isSignUpMode) {
                                                viewModel.registerGoogleUser(
                                                    email = emailNormalized,
                                                    name = nameInput.trim()
                                                ) {
                                                    Toast.makeText(context, "¡Identificación exitosa! Usuario registrado.", Toast.LENGTH_LONG).show()
                                                }
                                            } else {
                                                viewModel.loginWithGoogleEmail(
                                                    email = emailNormalized,
                                                    onSuccess = {
                                                        Toast.makeText(context, "Acceso concedido para ${it.name}", Toast.LENGTH_LONG).show()
                                                    },
                                                    onFailure = { error ->
                                                        // Automatically register if Google email is verified
                                                        viewModel.registerGoogleUser(
                                                            email = emailNormalized,
                                                            name = emailInput.substringBefore("@").replaceFirstChar { it.uppercase() }
                                                        ) {
                                                            Toast.makeText(context, "Google Email corroborado: Usuario creado inmediatamente.", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                )
                                            }
                                        },
                                        onFailure = { error ->
                                            isVerifying = false
                                            Toast.makeText(context, "Código inválido: $error", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                },
                                enabled = !isVerifying,
                                modifier = Modifier.weight(1.5f).testTag("verify_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)) // Emerald
                            ) {
                                if (isVerifying) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Verificar e Ingresar", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    TextButton(
                        onClick = {
                            isSignUpMode = !isSignUpMode
                            isCodeSent = false
                        }
                    ) {
                        Text(
                            text = if (isSignUpMode) "¿Ya posee identificación registrada? Inicie sesión" else "¿No posee identificación de guardia? Créela de inmediato",
                            color = Color(0xFFF59E0B),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}



