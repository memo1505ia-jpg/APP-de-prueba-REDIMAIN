package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessageEntity
import com.example.data.model.UserEntity

data class CustomToast(
    val id: Long = System.currentTimeMillis() + (0..1000).random(),
    val message: String,
    val type: ToastType = ToastType.INFO
)

enum class ToastType {
    INFO, WARNING, SUCCESS, ALERT
}

@Composable
fun NavyCommandHeader(
    isConfidentialUnlocked: Boolean,
    onToggleUnlock: (Boolean) -> Unit,
    currentUser: UserEntity? = null,
    onLogout: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "REDIMAIN",
                    color = Color(0xFFF59E0B), // amber-500
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF64748B))
                )
                Text(
                    text = "Estado Mayor",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = "ALMIRANTE",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.2).sp
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (currentUser != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currentUser.name,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentUser.email,
                        color = Color(0xFF94A3B8),
                        fontSize = 8.sp
                    )
                }
            }

            // Security Lock toggle pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isConfidentialUnlocked) Color(0xFF1B4F32) else Color(0xFF2D1616))
                    .border(
                        1.dp,
                        if (isConfidentialUnlocked) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFFEF4444).copy(alpha = 0.4f),
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { onToggleUnlock(!isConfidentialUnlocked) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = if (isConfidentialUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                    contentDescription = "Desbloquear Clasificado",
                    tint = if (isConfidentialUnlocked) Color(0xFF10B981) else Color(0xFFEF4444),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = if (isConfidentialUnlocked) "OPERATIVO" else "CIFRADO",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Elegant ADM lettered rounded-badge with logout
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E293B)) // bg-slate-800
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp)) // border-slate-700
                    .clickable { onLogout() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(50))
                        .border(1.5.dp, Color(0xFFEF4444), RoundedCornerShape(50)), // alert border red
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Cerrar Sesión",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FooterTabItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) Color(0x15F59E0B) else Color.Transparent) // amber-500 light alpha background
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) Color(0xFFF59E0B) else Color(0xFF64748B), // text-amber-500 vs text-slate-500
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color(0xFFF59E0B) else Color(0xFF64748B), // text-amber-500 vs text-slate-500
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun FloatingToastItem(toast: CustomToast, onDismiss: () -> Unit) {
    LaunchedEffect(toast.id) {
        kotlinx.coroutines.delay(4000)
        onDismiss()
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .clickable { onDismiss() }
            .border(
                1.dp,
                when (toast.type) {
                    ToastType.SUCCESS -> Color(0xFF10B981)
                    ToastType.WARNING -> Color(0xFFFBBF24)
                    ToastType.ALERT -> Color(0xFFEF4444)
                    ToastType.INFO -> Color(0xFF3B82F6)
                }.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A).copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = when (toast.type) {
                    ToastType.SUCCESS -> Icons.Default.CheckCircle
                    ToastType.WARNING -> Icons.Default.Warning
                    ToastType.ALERT -> Icons.Default.Error
                    ToastType.INFO -> Icons.Default.Info
                },
                contentDescription = null,
                tint = when (toast.type) {
                    ToastType.SUCCESS -> Color(0xFF10B981)
                    ToastType.WARNING -> Color(0xFFFBBF24)
                    ToastType.ALERT -> Color(0xFFEF4444)
                    ToastType.INFO -> Color(0xFF3B82F6)
                },
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = toast.message,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cerrar",
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(14.dp)
                    .clickable { onDismiss() }
            )
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessageEntity) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        val bubbleColor = if (message.isUser) Color(0xFF1B2E49) else Color(0xFF0F1E36)
        val textAccent = if (message.isUser) Color.White else Color(0xFFE2EAF4)
        val bubbleBorder = if (message.isUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)

        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 10.dp,
                        topEnd = 10.dp,
                        bottomStart = if (message.isUser) 10.dp else 2.dp,
                        bottomEnd = if (message.isUser) 2.dp else 10.dp
                    )
                )
                .background(bubbleColor)
                .border(
                    width = 0.5.dp,
                    color = bubbleBorder,
                    shape = RoundedCornerShape(
                        topStart = 10.dp,
                        topEnd = 10.dp,
                        bottomStart = if (message.isUser) 10.dp else 2.dp,
                        bottomEnd = if (message.isUser) 2.dp else 10.dp
                    )
                )
                .padding(11.dp)
                .widthIn(max = 310.dp)
        ) {
            Text(
                text = message.message,
                color = textAccent,
                fontSize = 11.5.sp,
                lineHeight = 17.sp
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = if (message.isUser) "Comandante" else "Asistente de Enlace",
            color = Color(0xFF5A6B80),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun IntelligencePanel(
    chatMessages: List<ChatMessageEntity>,
    isLoading: Boolean,
    errorMsg: String?,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    onClosePanel: () -> Unit
) {
    val scrollState = rememberScrollState()
    var userText by remember { mutableStateOf("") }

    // Auto-scroll on new chats
    LaunchedEffect(chatMessages.size, isLoading) {
        if (chatMessages.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
    ) {
        // Intelligence header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SupportAgent,
                    contentDescription = "Asistente Militar",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "CENTRO DE INTELIGENCIA AI",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "Comunicaciones de Enlace Táctico",
                        fontSize = 9.sp,
                        color = Color(0xFFA1AFBF)
                    )
                }
            }

            Row {
                IconButton(onClick = onClearChat) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Limpiar Conversación",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onClosePanel) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

        // Chat conversation body
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            if (chatMessages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Táctico",
                            tint = Color(0x3000E1D9),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Ayudante Táctico Online",
                            color = Color(0xFFA1AFBF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Mi Almirante, estoy listo para responder de manera concreta sus dudas acerca de las planificaciones y misiones registradas en la nave central.",
                            color = Color(0xFF758599),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "PREGUNTAS RECOMENDADAS:",
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val prefilledList = listOf(
                            "Emitir el Parte exacto del Personal (Estructura Matemática de Categorías)",
                            "Auditar Agenda y Misiones próximas a concluir",
                            "Auditar Tráfico de Comunicaciones e Instrucciones",
                            "Análisis de Suministro de Combustible y Planificación Logística"
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            prefilledList.forEach { p ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF14243B))
                                        .border(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                        .clickable { onSendMessage(p) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = p,
                                        fontSize = 10.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                chatMessages.forEach { msg ->
                    ChatBubble(message = msg)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (isLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Enlace Satelital Simón Bolívar decodificando tácticas...",
                            color = Color(0xFF00E1D9),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (errorMsg != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF4A1515))
                    .padding(8.dp)
            ) {
                Text(
                    text = "⚠️ CONFIGURACIÓN CLAVE: El Almirante requiere configurar GEMINI_API_KEY en la terminal web para llamadas IA en tiempo real.",
                    color = Color(0xFFFFA0A0),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Intelligence chat messaging input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .background(Color(0xFF030710), RoundedCornerShape(10.dp))
                .border(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = userText,
                onValueChange = { userText = it },
                placeholder = { Text("Escriba su consulta comandante...", fontSize = 11.sp, color = Color(0xFF5A6B80)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp, max = 80.dp),
                textStyle = TextStyle(fontSize = 12.sp, lineHeight = 16.sp)
            )

            IconButton(
                onClick = {
                    if (userText.isNotBlank()) {
                        onSendMessage(userText)
                        userText = ""
                    }
                },
                enabled = userText.isNotBlank() && !isLoading,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (userText.isNotBlank()) MaterialTheme.colorScheme.primary else Color(0xFF1B2A3F))
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Enviar",
                    tint = if (userText.isNotBlank()) Color.Black else Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
