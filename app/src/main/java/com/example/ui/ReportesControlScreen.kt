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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CommTrafficEntity
import com.example.data.model.OfficerEntity
import com.example.data.model.RowEntity
import com.example.viewmodel.REDIMAINViewModel

@Composable
fun ReportesControlScreen(
    currentRows: List<RowEntity>,
    officers: List<OfficerEntity>,
    commTraffic: List<CommTrafficEntity>,
    viewModel: REDIMAINViewModel
) {
    var showAddCommDialog by remember { mutableStateOf(false) }

    // Dynamic calculus for graphs
    val activeStaffCount = officers.count { it.status == "Activo" }
    val leaveStaffCount = officers.count { it.status == "Permiso especial" }
    val sickStaffCount = officers.count { it.status.startsWith("Reposo", ignoreCase = true) }
    val totalStaff = officers.size.coerceAtLeast(1)

    // Communications statuses
    val pendingComms = commTraffic.count { it.status == "Pendiente" }
    val executingComms = commTraffic.count { it.status == "Ejecutando" }
    val completedComms = commTraffic.count { it.status == "Cumplido" }
    val totalComms = commTraffic.size.coerceAtLeast(1)

    // Complete percentage
    val missionRowsCount = currentRows.size.coerceAtLeast(1)
    val completedMissions = currentRows.count { row ->
        row.cells.any { cell -> cell.contains("Completado", ignoreCase = true) || cell.contains("Completada", ignoreCase = true) || cell.contains("100%", ignoreCase = true) }
    }
    val missionSuccessRatio = (completedMissions.toFloat() / missionRowsCount.toFloat() * 100f).toInt()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Stats Summary Dashboard cards
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF11141A)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "CUADRO DE ENLACE ESTRATÉGICO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFF59E0B),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Métricas Navales de Gestión",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Three columns KPI grid representation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Card KPI 1: Staff Efficiency
                        Card(
                            modifier = Modifier.weight(1f).border(1.dp, Color(0x3010B981), RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1520))
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Apresto Personal", fontSize = 8.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${(activeStaffCount.toFloat() / totalStaff * 100).toInt()}%", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                                Text("$activeStaffCount / $totalStaff Activos", fontSize = 9.sp, color = Color(0xFF94A3B8))
                            }
                        }

                        // Card KPI 2: Missions Completion Ratio
                        Card(
                            modifier = Modifier.weight(1f).border(1.dp, Color(0x30F59E0B), RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1520))
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Cumplimiento", fontSize = 8.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("$missionSuccessRatio%", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFFF59E0B))
                                Text("$completedMissions / $missionRowsCount Tareas", fontSize = 9.sp, color = Color(0xFF94A3B8))
                            }
                        }

                        // Card KPI 3: Orders Pending
                        Card(
                            modifier = Modifier.weight(1f).border(1.dp, Color(0x30EF4444), RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1520))
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Instrucciones Sup", fontSize = 8.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("$pendingComms", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFFEF4444))
                                Text("Pendientes de Ejec", fontSize = 9.sp, color = Color(0xFF94A3B8))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Simulated Status Ratio Distribution bar
                    Text("Distribución del Talento Humano:", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF1F2937))
                    ) {
                        val activeFract = activeStaffCount.toFloat() / totalStaff
                        val leaveFract = leaveStaffCount.toFloat() / totalStaff
                        val sickFract = sickStaffCount.toFloat() / totalStaff
                        val remFract = 1f - activeFract - leaveFract - sickFract

                        if (activeFract > 0) {
                            Box(modifier = Modifier.fillMaxHeight().weight(activeFract.coerceAtLeast(0.01f)).background(Color(0xFF10B981)))
                        }
                        if (leaveFract > 0) {
                            Box(modifier = Modifier.fillMaxHeight().weight(leaveFract.coerceAtLeast(0.01f)).background(Color(0xFF3B82F6)))
                        }
                        if (sickFract > 0) {
                            Box(modifier = Modifier.fillMaxHeight().weight(sickFract.coerceAtLeast(0.01f)).background(Color(0xFFEF4444)))
                        }
                        if (remFract > 0) {
                            Box(modifier = Modifier.fillMaxHeight().weight(remFract.coerceAtLeast(0.01f)).background(Color(0xFF64748B)))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    // Legend elements
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(50)).background(Color(0xFF10B981)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Activos ($activeStaffCount)", fontSize = 8.sp, color = Color(0xFF94A3B8))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(50)).background(Color(0xFF3B82F6)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Permiso ($leaveStaffCount)", fontSize = 8.sp, color = Color(0xFF94A3B8))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(50)).background(Color(0xFFEF4444)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reposo ($sickStaffCount)", fontSize = 8.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }
            }
        }

        // CONTROL DE TRÁFICO DE COMUNICACIONES CABECERA
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "COMUNICACIONES ESCRITAS E INSTRUCCIONES SUPERIORIDAD",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFBBF24),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Tráfico de Órdenes REDIMAIN",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        if (commTraffic.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color(0xFF11141A), RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.MailOutline, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No hay tráfico de oficios registrado", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Utilice 'Registrar Oficio' para cargar órdenes.", color = Color(0xFF64748B), fontSize = 10.sp)
                    }
                }
            }
        } else {
            items(commTraffic) { comm ->
                CommTrafficItemCard(
                    comm = comm,
                    onUpdateStatus = { updated -> viewModel.updateCommTraffic(updated) },
                    onDelete = { viewModel.deleteCommTraffic(comm) }
                )
            }
        }
    }

    if (showAddCommDialog) {
        AddCommTrafficDialog(
            officers = officers,
            onDismiss = { showAddCommDialog = false },
            onSave = { docNum, subj, origin, assign, dateRec, deadline, alarm, imageKey ->
                viewModel.addCommTraffic(docNum, subj, origin, assign, dateRec, deadline, alarm, imageKey)
                showAddCommDialog = false
            }
        )
    }
}

@Composable
fun CommTrafficItemCard(
    comm: CommTrafficEntity,
    onUpdateStatus: (CommTrafficEntity) -> Unit,
    onDelete: () -> Unit
) {
    var photoExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1420)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Doc id & status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                    Text(
                        text = comm.documentNumber,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Status Badge click options toggling status inline with help cue
                    Column(horizontalAlignment = Alignment.End) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when (comm.status) {
                                        "Cumplido" -> Color(0x1810B981)
                                        "Ejecutando" -> Color(0x18FBBF24)
                                        else -> Color(0x18EF4444)
                                    }
                                )
                                .clickable {
                                    val nextStatus = when (comm.status) {
                                        "Pendiente" -> "Ejecutando"
                                        "Ejecutando" -> "Cumplido"
                                        else -> "Pendiente"
                                    }
                                    onUpdateStatus(comm.copy(status = nextStatus))
                                }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = comm.status.uppercase(),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (comm.status) {
                                    "Cumplido" -> Color(0xFF10B981)
                                    "Ejecutando" -> Color(0xFFFBBF24)
                                    else -> Color(0xFFEF4444)
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "Toca para cambiar",
                            color = Color(0xFF64748B),
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar Oficio", tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Subject description
            Text(
                text = comm.subject,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ORIGEN: ", fontSize = 9.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                        Text(comm.origin, fontSize = 11.sp, color = Color(0xFFE2E8F0))
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("RESPONSABLE: ", fontSize = 9.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                        Text(comm.recipientRankName, fontSize = 11.sp, color = Color(0xFFFBBF24), fontWeight = FontWeight.SemiBold)
                    }
                }

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF141B26))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                        .clickable { photoExpanded = !photoExpanded },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (comm.imagePath != null) Icons.Default.InsertDriveFile else Icons.Default.ImageNotSupported,
                        contentDescription = "Ver Oficio Ofic",
                        tint = if (comm.imagePath != null) Color(0xFFF59E0B) else Color(0xFF475569),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress tracking limit timeline row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF070B12), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("FECHA RECIBIDO: ", fontSize = 8.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(comm.dateReceived, fontSize = 10.sp, color = Color(0xFF94A3B8), fontFamily = FontFamily.Monospace)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("PLAZO MÁXIMO: ", fontSize = 8.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = comm.deadlineDate,
                        fontSize = 10.sp,
                        color = if (comm.hasDeadlineAlarm && comm.status != "Cumplido") Color(0xFFEF4444) else Color(0xFF10B981),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (comm.hasDeadlineAlarm && comm.status != "Cumplido") {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFEF4444))
                        )
                    }
                }
            }

            // Expanded view of the visual communication document uploaded
            if (photoExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color(0xFF334155), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "VISTA PREVIA DEL DOCUMENTO OFICIAL:",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF59E0B)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("REPÚBLICA BOLIVARIANA DE VENEZUELA", color = Color.DarkGray, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                Text("FUERZA ARMADA NACIONAL BOLIVARIANA - REDIMAIN", color = Color.Gray, fontSize = 6.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                    .border(1.dp, Color(0xFF10B981), RoundedCornerShape(50)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("OK", color = Color(0xFF10B981), fontSize = 6.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        Column(modifier = Modifier.padding(vertical = 10.dp)) {
                            Text("COMUNICACIÓN REGISTRADA: ${comm.documentNumber}", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("ASUNTO: ${comm.subject}", color = Color.DarkGray, fontSize = 8.sp, maxLines = 2)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Se insta al Oficial ejecutor designado, ciudadano de la REDIMAIN, a rendir novedades antes del día límite estipulado de cumplimiento militar.", color = Color.Gray, fontSize = 7.sp, maxLines = 3)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text("ID: COMMAND-SEC-${comm.id}", color = Color.Gray, fontSize = 6.sp)
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Firma Autorizada", color = Color.DarkGray, fontSize = 6.sp, fontWeight = FontWeight.Bold)
                                Text("COMANDANTE DE LA REDIMAIN", color = Color.LightGray, fontSize = 5.sp)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedButton(
                    onClick = { photoExpanded = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Cerrar Vista Previa", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun AddCommTrafficDialog(
    officers: List<OfficerEntity>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, Boolean, String?) -> Unit
) {
    var docNum by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var origin by remember { mutableStateOf("") }
    var assignedTo by remember { mutableStateOf("") }
    var dateRec by remember { mutableStateOf("2026-05-27") }
    var deadline by remember { mutableStateOf("2026-06-03") }
    var enableAlarm by remember { mutableStateOf(true) }
    var selectedTemplateName by remember { mutableStateOf("Oficio Oficial REDIMAIN") }
    var showDropdown by remember { mutableStateOf(false) }

    if (assignedTo.isEmpty() && officers.isNotEmpty()) {
        assignedTo = officers.first().name
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp).verticalScroll(rememberScrollState())) {
                Text("REGISTRO DE ÓRDENES ESCRITAS", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFFFBBF24))
                Text("Cargar Comunicación de Superioridad", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = docNum,
                    onValueChange = { docNum = it },
                    label = { Text("Nº Oficio / Documento (Ej: OF-MPPD-2026-04)", fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFBBF24),
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Asunto o Instrucción Específica", fontSize = 12.sp) },
                    singleLine = false,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFBBF24),
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp, max = 110.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = origin,
                    onValueChange = { origin = it },
                    label = { Text("Ente Origen (Ej: MPPD, COMAN, Comandancia etc.)", fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFBBF24),
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Assign responsible official
                Text("Responsable Desingado Ejecutor:", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    OutlinedButton(
                        onClick = { showDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text(assignedTo.ifEmpty { "Seleccionar Oficial..." }, fontSize = 12.sp)
                    }
                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false },
                        modifier = Modifier.background(Color(0xFF1E293B))
                    ) {
                        officers.forEach { off ->
                            DropdownMenuItem(
                                text = { Text(off.name, color = Color.White) },
                                onClick = {
                                    assignedTo = off.name
                                    showDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Date ranges
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dateRec,
                        onValueChange = { dateRec = it },
                        label = { Text("Fecha Recibido", fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFBBF24),
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = deadline,
                        onValueChange = { deadline = it },
                        label = { Text("Plazo Límite", fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFBBF24),
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Asociar Imagen / Oficio Físico Digitalizado:", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF141B26), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFFFBBF24))
                    Column {
                        Text("Simulación de Escaneo y Carga", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Carga plantilla de oficio naval firmado", color = Color(0xFF64748B), fontSize = 9.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Plazo Alarm
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Alarma de Plazo Expirándose Activa", fontSize = 11.sp, color = Color.White)
                    Switch(
                        checked = enableAlarm,
                        onCheckedChange = { enableAlarm = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFF59E0B),
                            checkedTrackColor = Color(0xFF1B2E49)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("No", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            if (docNum.isNotBlank() && subject.isNotBlank()) {
                                onSave(docNum, subject, origin, assignedTo, dateRec, deadline, enableAlarm, selectedTemplateName)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("Registrar Oficio", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
