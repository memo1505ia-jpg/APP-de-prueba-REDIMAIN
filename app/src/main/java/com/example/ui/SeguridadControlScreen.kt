package com.example.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RowEntity
import com.example.data.model.TabEntity
import com.example.viewmodel.REDIMAINViewModel

@Composable
fun SeguridadControlScreen(
    tabs: List<TabEntity>,
    currentRows: List<RowEntity>,
    viewModel: REDIMAINViewModel,
    isUnlocked: Boolean
) {
    var triggerAuditPin by remember { mutableStateOf("") }
    val secureLogsState = remember {
        mutableStateListOf(
            "2026-05-27 18:22 - Intento de acceso general autorizado por ALMIRANTE",
            "2026-05-27 15:40 - Desbloqueo temporal de canal confidencial de patrullaje",
            "2026-05-26 09:12 - Auditoría de integridad de base de datos finalizada - Satisfactorio"
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Main Cryptographic Status card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF11141A)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "CONTROL DE CRIPTOGRAFÍA MILITAR",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFEF4444),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Nivel de Seguridad Naval",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F1520), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isUnlocked) Color(0x2010B981) else Color(0x20EF4444)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (isUnlocked) Color(0xFF10B981) else Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = if (isUnlocked) "DATOS CLASIFICADOS VISIBLES" else "ACCESO PROTEGIDO Y CIFRADO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isUnlocked) "Nivel de seguridad G-2 descifrado temporalmente" else "Información secreta enmascarada detrás de hash SHA-256",
                                    fontSize = 9.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Switch(
                            checked = isUnlocked,
                            onCheckedChange = {
                                viewModel.setConfidentialUnlocked(it)
                                secureLogsState.add(0, "2026-05-27 20:20 - Cambio manual de estatus de cifrado a: ${if (it) "DESBLOQUEADO" else "CIFRADO"}")
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF10B981),
                                checkedTrackColor = Color(0xFF064E3B)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Security metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF0F1520), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("ELEMENTOS PROTEGIDOS", fontSize = 8.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${currentRows.count { it.isConfidential }} Registros", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF0F1520), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("CANAL TRANS-MARÍTIMO", fontSize = 8.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("CIFRADO AES-256", fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        // List of currently protected/masked rows mapping to spreadsheets
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF11141A)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "AUDITORÍA DE REGISTROS CLASIFICADOS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "Elementos Marcados como Confidencial",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val confidentialRows = currentRows.filter { it.isConfidential }
                    if (confidentialRows.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay filas marcadas como confidenciales en la tabla activa.", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            confidentialRows.forEach { row ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0F1520), RoundedCornerShape(6.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                                        Column(modifier = Modifier.weight(1f, fill = false)) {
                                            Text(
                                                text = if (isUnlocked) "Registro Revelado: ${row.cells.take(2).joinToString(" | ")}" else "REGISTRO ENCRIPTADO SHA-256",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isUnlocked) Color.White else Color(0xFFEF4444),
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "Canal activo • ID del registro: ${row.id}",
                                                fontSize = 9.sp,
                                                color = Color(0xFF64748B),
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Row(modifier = Modifier.padding(start = 4.dp)) {
                                        IconButton(
                                            onClick = { viewModel.toggleRowConfidentiality(row) },
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.LockOpen, contentDescription = "Descifrar", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color(0xFF334155), thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Security audit trails
                    Text("Bitácora de Seguridad Reciente:", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        secureLogsState.forEach { log ->
                            Text(
                                text = log,
                                fontSize = 9.sp,
                                color = Color(0xFF94A3B8),
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(vertical = 1.dp),
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
