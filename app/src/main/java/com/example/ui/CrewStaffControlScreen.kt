package com.example.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.OfficerEntity
import com.example.data.model.StatusOptionEntity
import com.example.viewmodel.REDIMAINViewModel

@Composable
fun CrewStaffControlScreen(
    officers: List<OfficerEntity>,
    statusOptions: List<StatusOptionEntity>,
    viewModel: REDIMAINViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterStatus by remember { mutableStateOf("Todos") }
    var showAddOfficerDialog by remember { mutableStateOf(false) }
    var showAddStatusDialog by remember { mutableStateOf(false) }
    
    // Expediente dossier triggers
    var selectedExpedienteOfficer by remember { mutableStateOf<OfficerEntity?>(null) }
    var selectedEditOfficer by remember { mutableStateOf<OfficerEntity?>(null) }

    val filteredOfficers = officers.filter {
        val matchesSearch = it.name.contains(searchQuery, ignoreCase = true) || 
                            it.rankCargo.contains(searchQuery, ignoreCase = true) ||
                            it.cedula.contains(searchQuery, ignoreCase = true) ||
                            it.category.contains(searchQuery, ignoreCase = true)
        val matchesStatus = selectedFilterStatus == "Todos" || it.status == selectedFilterStatus
        matchesSearch && matchesStatus
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp)
    ) {
        // Dashboard Header Panel (sin botón duplicado – el toolbar global lo maneja)
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF11141A)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Column {
                        Text(
                            text = "SISTEMA DE TRIPULACIÓN REH",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFF59E0B),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Expedientes de Tripulantes",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar por nombres, cargo, Cédula...", fontSize = 12.sp, color = Color(0xFF64748B)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF1E293B),
                                unfocusedContainerColor = Color(0xFF141B26),
                                focusedBorderColor = Color(0xFFF59E0B),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 12.sp),
                            trailingIcon = {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                            }
                        )

                        OutlinedButton(
                            onClick = { showAddStatusDialog = true },
                            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF59E0B)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Estatus DB", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status Filter Chips Row
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            FilterChip(
                                selected = selectedFilterStatus == "Todos",
                                onClick = { selectedFilterStatus = "Todos" },
                                label = { Text("Ver Todos (${officers.size})", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFD97706),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF1E293B),
                                    labelColor = Color(0xFF94A3B8)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedFilterStatus == "Todos",
                                    borderColor = Color(0xFF334155)
                                )
                            )
                        }

                        items(statusOptions) { opt ->
                            val count = officers.count { it.status == opt.name }
                            FilterChip(
                                selected = selectedFilterStatus == opt.name,
                                onClick = { selectedFilterStatus = opt.name },
                                label = { Text("${opt.name} ($count)", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFD97706),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF1E293B),
                                    labelColor = Color(0xFF94A3B8)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedFilterStatus == opt.name,
                                    borderColor = Color(0xFF334155)
                                )
                            )
                        }
                    }
                }
            }
        }

        // Search Results / Officers Cards inside scroll context
        if (filteredOfficers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(Color(0xFF11141A), RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Icon(imageVector = Icons.Default.Group, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No se encontraron Tripulantes registrados", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Cambie los filtros o registre un nuevo miembro de la Tripulación.", color = Color(0xFF64748B), fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(filteredOfficers) { officer ->
                OfficerItemCard(
                    officer = officer,
                    onCardClick = { selectedExpedienteOfficer = officer },
                    onEdit = { selectedEditOfficer = officer },
                    onDelete = { viewModel.deleteOfficer(officer) }
                )
            }
        }
    }

    // Modal popup representing full expedient dossier (DIRECTRICES)
    if (selectedExpedienteOfficer != null) {
        ExpedienteDigitalDialog(
            officer = selectedExpedienteOfficer!!,
            statusOptions = statusOptions,
            onDismiss = { selectedExpedienteOfficer = null },
            onEdit = { 
                selectedEditOfficer = selectedExpedienteOfficer
                selectedExpedienteOfficer = null
            },
            onDelete = {
                viewModel.deleteOfficer(selectedExpedienteOfficer!!)
                selectedExpedienteOfficer = null
            }
        )
    }

    // Form modal representing add actions or edit actions (DIRECTRICES)
    if (showAddOfficerDialog) {
        AddOrEditOfficerDialog(
            officer = null,
            statusOptions = statusOptions,
            onDismiss = { showAddOfficerDialog = false },
            onSave = { updatedOfficer ->
                viewModel.addOfficer(
                    name = updatedOfficer.name,
                    cedula = updatedOfficer.cedula,
                    phone = updatedOfficer.phone,
                    rankCargo = updatedOfficer.rankCargo,
                    status = updatedOfficer.status,
                    fromDate = updatedOfficer.statusFromDate,
                    toDate = updatedOfficer.statusToDate,
                    hasAlarm = updatedOfficer.hasAlarm,
                    photoUri = updatedOfficer.photoUri,
                    category = updatedOfficer.category,
                    direccionHabitacion = updatedOfficer.direccionHabitacion,
                    direccionResidencia = updatedOfficer.direccionResidencia,
                    conyuge = updatedOfficer.conyuge,
                    padres = updatedOfficer.padres,
                    hijosJson = updatedOfficer.hijosJson,
                    grupoSanguineo = updatedOfficer.grupoSanguineo,
                    alergias = updatedOfficer.alergias,
                    contactoEmergencia = updatedOfficer.contactoEmergencia,
                    tallaCamisa = updatedOfficer.tallaCamisa,
                    tallaGuerrera = updatedOfficer.tallaGuerrera,
                    tallaBotas = updatedOfficer.tallaBotas,
                    tallaGorra = updatedOfficer.tallaGorra,
                    tallaQuepi = updatedOfficer.tallaQuepi,
                    tallaZapatos = updatedOfficer.tallaZapatos,
                    tallaPantalon = updatedOfficer.tallaPantalon
                )
                showAddOfficerDialog = false
            }
        )
    }

    if (selectedEditOfficer != null) {
        AddOrEditOfficerDialog(
            officer = selectedEditOfficer,
            statusOptions = statusOptions,
            onDismiss = { selectedEditOfficer = null },
            onSave = { updatedOfficer ->
                viewModel.updateOfficer(updatedOfficer)
                selectedEditOfficer = null
            }
        )
    }

    if (showAddStatusDialog) {
        ManageStatusesDialog(
            statusOptions = statusOptions,
            onDismiss = { showAddStatusDialog = false },
            onAdd = { viewModel.addStatusOption(it) },
            onDelete = { viewModel.deleteStatusOption(it) }
        )
    }
}

// 1. Vista Previa Limpia (DIRECTRICES)
@Composable
fun OfficerItemCard(
    officer: OfficerEntity,
    onCardClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1520)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp), // Margen interno de tarjeta (DIRECTRICES)
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Contenedor Circular de la Fotografia (DIRECTRICES)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .border(1.5.dp, Color(0xFFF59E0B), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!officer.photoUri.isNullOrBlank()) {
                    AsyncImage(
                        model = officer.photoUri,
                        contentDescription = "Foto de ${officer.name}",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop // object-fit: cover
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Textos descriptivos con control anti-deformacion (DIRECTRICES)
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = officer.category.uppercase(),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B).copy(alpha = 0.8f),
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = officer.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = officer.rankCargo,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Contonedor de icono campana y estatus sin overflow (DIRECTRICES)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (officer.hasAlarm) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFFFBBF24).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFFFBBF24).copy(alpha = 0.35f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Alarma",
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                // Estatus Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            when (officer.status) {
                                "Activo" -> Color(0x2010B981)
                                "Permiso especial" -> Color(0x203B82F6)
                                "Reposo" -> Color(0x20EF4444)
                                "Reposo médico domiciliario" -> Color(0x20F59E0B)
                                else -> Color(0x2064748B)
                            }
                        )
                        .border(
                            1.dp,
                            when (officer.status) {
                                "Activo" -> Color(0xFF10B981)
                                "Permiso especial" -> Color(0xFF3B82F6)
                                "Reposo" -> Color(0xFFEF4444)
                                "Reposo médico domiciliario" -> Color(0xFFF59E0B)
                                else -> Color(0xFF94A3B8)
                            }.copy(alpha = 0.3f),
                            RoundedCornerShape(30.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = officer.status.uppercase(),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = when (officer.status) {
                            "Activo" -> Color(0xFF10B981)
                            "Permiso especial" -> Color(0xFF3B82F6)
                            "Reposo" -> Color(0xFFEF4444)
                            "Reposo médico domiciliario" -> Color(0xFFF59E0B)
                            else -> Color(0xFF94A3B8)
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF475569),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// 3. Componente de Perfil Completo (Modal de Expediente DIGITAL - DIRECTRICES)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpedienteDigitalDialog(
    officer: OfficerEntity,
    statusOptions: List<StatusOptionEntity>,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B0F19)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ESTADO MAYOR • REH",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFF59E0B),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "EXPEDIENTE DIGITAL MILITAR",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scroll Content (overflow-y: auto)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Profile Header with Rounded circular picture (DIRECTRICES)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF111726), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Contenedor circular (border-radius: 50%; object-fit: cover;) (DIRECTRICES)
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B))
                                .border(2.dp, Color(0xFFF59E0B), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!officer.photoUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = officer.photoUri,
                                    contentDescription = "Foto de ${officer.name}",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop // object-fit: cover
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MilitaryTech,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0x20F59E0B), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                  Text(
                                    text = officer.category,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF59E0B)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = officer.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = officer.rankCargo,
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier.size(8.dp).clip(CircleShape).background(
                                        if (officer.status == "Activo") Color(0xFF10B981) else Color(0xFFEF4444)
                                    )
                                )
                                Text(
                                    text = "Condición: ${officer.status} (${officer.statusFromDate} al ${officer.statusToDate})",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Datos Personales Basicos
                    Text("1. DATOS IDENTIFICATORIOS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFFF59E0B))
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF151D30)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("CÉDULA DE IDENTIDAD", fontSize = 8.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                    Text(officer.cedula, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Monospace)
                                }
                                Column {
                                    Text("TELÉFONO DE CONTACTO", fontSize = 8.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                    Text(officer.phone.ifBlank { "No registrado" }, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    // Datos de Domicilio (DIRECTRICES)
                    Text("2. GEOGRAFÍA Y DIRECCIONES", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFFF59E0B))
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF151D30)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column {
                                Text("DIRECCIÓN DE HABITACIÓN (PERMANENTE)", fontSize = 8.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                Text(officer.direccionHabitacion.ifBlank { "No especificado" }, fontSize = 11.sp, color = Color.White)
                            }
                            Divider(color = Color(0xFF334155).copy(alpha = 0.3f))
                            Column {
                                Text("DIRECCIÓN DE RESIDENCIA ACTUAL EN LA ISLA", fontSize = 8.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                Text(officer.direccionResidencia.ifBlank { "No especificado" }, fontSize = 11.sp, color = Color.White)
                                Text("(Dirección actual en la isla de Margarita)", fontSize = 8.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    // Nucleo Familiar (DIRECTRICES)
                    Text("3. NÚCLEO FAMILIAR TRASCENDENTAL", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFFF59E0B))
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF151D30)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column {
                                Text("CÓNYUGE / CONSORTE", fontSize = 8.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                Text(officer.conyuge.ifBlank { "No registrado" }, fontSize = 11.sp, color = Color.White)
                            }
                            Divider(color = Color(0xFF334155).copy(alpha = 0.3f))
                            Column {
                                Text("PROGENITORES / PADRES", fontSize = 8.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                Text(officer.padres.ifBlank { "No registrado" }, fontSize = 11.sp, color = Color.White)
                            }
                            Divider(color = Color(0xFF334155).copy(alpha = 0.3f))
                            Column {
                                Text("HIJOS E HIJAS REGISTRADAS", fontSize = 8.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                val hijos = remember(officer.hijosJson) {
                                    if (officer.hijosJson.isBlank() || officer.hijosJson == "[]") emptyList()
                                    else {
                                        try {
                                            officer.hijosJson.split(";").mapNotNull {
                                                val parts = it.split(",")
                                                if (parts.size == 2) Pair(parts[0].trim(), parts[1].trim()) else null
                                            }
                                        } catch (e: Exception) { emptyList() }
                                    }
                                }
                                if (hijos.isEmpty()) {
                                    Text("Sin hijos declarados en el expediente.", fontSize = 11.sp, color = Color(0xFF64748B))
                                } else {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    hijos.forEach { child ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF0D131F), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Icon(
                                                    imageVector = if (child.first.contains("hemb", ignoreCase = true) || child.first.contains("fem", ignoreCase = true) || child.first.contains("f", ignoreCase = true)) Icons.Default.Female else Icons.Default.Male,
                                                    contentDescription = null,
                                                    tint = Color(0xFFF59E0B),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text("Hijo (${child.first})", fontSize = 11.sp, color = Color.White)
                                            }
                                            Text("Edad: ${child.second} años", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(3.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Datos Medicos y Emergencia (DIRECTRICES)
                    Text("4. PARTE MÉDICO Y SOCORRO", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFFF59E0B))
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF151D30)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("GRUPO SANGUÍNEO", fontSize = 8.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                    Text(officer.grupoSanguineo.ifBlank { "No especificado" }, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("ALERGIAS DECLARADAS", fontSize = 8.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                    Text(officer.alergias.ifBlank { "Ninguna declarada" }, fontSize = 11.sp, color = Color.White)
                                }
                            }
                            Divider(color = Color(0xFF334155).copy(alpha = 0.3f))
                            Column {
                                Text("CONTACTO DE EMERGENCIA", fontSize = 8.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                Text(officer.contactoEmergencia.ifBlank { "No registrado" }, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    // Tallas / Intendencia (DIRECTRICES)
                    Text("5. TALLAS Y DOTACION (INTENDENCIA/VESTUARIO)", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFFF59E0B))
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF151D30)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            val sizes = listOf(
                                "Camisa" to officer.tallaCamisa,
                                "Guerrera" to officer.tallaGuerrera,
                                "Botas" to officer.tallaBotas,
                                "Gorra" to officer.tallaGorra,
                                "Quepi" to officer.tallaQuepi,
                                "Zapatos" to officer.tallaZapatos,
                                "Pantalón" to officer.tallaPantalon
                            )
                            
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                maxItemsInEachRow = 3
                            ) {
                                sizes.forEach { size ->
                                    Row(
                                        modifier = Modifier
                                            .padding(vertical = 4.dp)
                                            .background(Color(0xFF0B0F19), RoundedCornerShape(6.dp))
                                            .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Checkroom, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(12.dp))
                                        Text(
                                            text = "${size.first.uppercase()}: ${size.second.ifBlank { "N/R" }}",
                                            fontSize = 10.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Remover", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onEdit,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar Expediente", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
    }
}

// 4. Formulario de Añadir/Editar Tripulante (DIRECTRICES)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddOrEditOfficerDialog(
    officer: OfficerEntity? = null,
    statusOptions: List<StatusOptionEntity>,
    onDismiss: () -> Unit,
    onSave: (OfficerEntity) -> Unit
) {
    val isEditMode = officer != null

    var name by remember { mutableStateOf(officer?.name ?: "") }
    var rankCargo by remember { mutableStateOf(officer?.rankCargo ?: "") }
    var cedula by remember { mutableStateOf(officer?.cedula ?: "") }
    var phone by remember { mutableStateOf(officer?.phone ?: "") }
    var statusState by remember { mutableStateOf(officer?.status ?: (statusOptions.firstOrNull()?.name ?: "Activo")) }
    var fromDate by remember { mutableStateOf(officer?.statusFromDate ?: "2026-05-28") }
    var toDate by remember { mutableStateOf(officer?.statusToDate ?: "2026-12-31") }
    var triggerAlarm by remember { mutableStateOf(officer?.hasAlarm ?: true) }
    var photoUri by remember { mutableStateOf(officer?.photoUri ?: "") }
    
    // Categorias standard obligatorias (DIRECTRICES)
    val categories = listOf("OFICIALES", "OFICIALES TÉCNICOS", "OFICIAL DE TROPA", "OFICIAL ASIMILADO", "TROPA PROFESIONAL", "TROPA ALISTADA")
    var categoryState by remember { mutableStateOf(officer?.category ?: "OFICIALES") }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    // Domicilio and tools
    var direccionHabitacion by remember { mutableStateOf(officer?.direccionHabitacion ?: "") }
    var direccionResidencia by remember { mutableStateOf(officer?.direccionResidencia ?: "") }
    var isResidenciaFocused by remember { mutableStateOf(false) }

    // Nucleo Familiar
    var conyuge by remember { mutableStateOf(officer?.conyuge ?: "") }
    var padres by remember { mutableStateOf(officer?.padres ?: "") }
    
    // Children list management
    val hijosList = remember { mutableStateListOf<Pair<String, Int>>() }
    LaunchedEffect(officer) {
        if (officer != null && officer.hijosJson.isNotBlank() && officer.hijosJson != "[]") {
            try {
                officer.hijosJson.split(";").forEach {
                    val parts = it.split(",")
                    if (parts.size == 2) {
                        val sexo = parts[0].trim()
                        val edad = parts[1].trim().toIntOrNull() ?: 0
                        hijosList.add(Pair(sexo, edad))
                    }
                }
            } catch (e: Exception) { /* ignore */ }
        }
    }

    var tempChildSexo by remember { mutableStateOf("Varón") }
    var tempChildEdad by remember { mutableStateOf("") }

    // Medicos y Emergencia
    var grupoSanguineo by remember { mutableStateOf(officer?.grupoSanguineo ?: "O+") }
    var alergias by remember { mutableStateOf(officer?.alergias ?: "") }
    var contactoEmergencia by remember { mutableStateOf(officer?.contactoEmergencia ?: "") }

    // Tallas
    var tallaCamisa by remember { mutableStateOf(officer?.tallaCamisa ?: "") }
    var tallaGuerrera by remember { mutableStateOf(officer?.tallaGuerrera ?: "") }
    var tallaBotas by remember { mutableStateOf(officer?.tallaBotas ?: "") }
    var tallaGorra by remember { mutableStateOf(officer?.tallaGorra ?: "") }
    var tallaQuepi by remember { mutableStateOf(officer?.tallaQuepi ?: "") }
    var tallaZapatos by remember { mutableStateOf(officer?.tallaZapatos ?: "") }
    var tallaPantalon by remember { mutableStateOf(officer?.tallaPantalon ?: "") }

    var showStatusDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Title
                Text(
                    text = if (isEditMode) "MODIFICAR EXPEDIENTE" else "REGISTRAR EN LA TRIPULACIÓN",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFF59E0B)
                )
                Text(
                    text = if (isEditMode) "Editar Tripulante" else "Nuevo Tripulante",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Scroll flow of fields
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Avatar uploader & simulated photopicker
                    Text("Fotografía Oficial (Hacer Click para cambiar sugerencias)", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .size(74.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B), CircleShape)
                            .border(1.5.dp, Color(0xFFF59E0B), CircleShape)
                            .align(Alignment.CenterHorizontally)
                            .clickable {
                                val simulatedAvatars = listOf(
                                    "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&q=80&w=200", // F 1
                                    "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&q=80&w=200", // M 1
                                    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=200", // F 2
                                    "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=200", // M 2
                                    "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?auto=format&fit=crop&q=80&w=200"  // Command portrait
                                )
                                photoUri = if (photoUri in simulatedAvatars) {
                                    val currentIdx = simulatedAvatars.indexOf(photoUri)
                                    val nextIdx = (currentIdx + 1) % (simulatedAvatars.size + 1)
                                    if (nextIdx == simulatedAvatars.size) "" else simulatedAvatars[nextIdx]
                                } else {
                                    simulatedAvatars[0]
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUri.isNotBlank()) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = "Simulación de Avatar",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop // object-fit: cover
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(22.dp))
                                Text("SUBIR FOTO", fontSize = 8.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 1. Selector de Categoria Obligatoria (DIRECTRICES)
                    Text("Categoría de Personal (Obligatorio)*", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showCategoryDropdown = true },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text(categoryState, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false },
                            modifier = Modifier.background(Color(0xFF1E293B))
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        categoryState = cat
                                        showCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Datos Personales Basicos
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Grado / Nombres Completos (Ej: CC Pedro Pérez)", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 12.sp)
                    )

                    OutlinedTextField(
                        value = rankCargo,
                        onValueChange = { rankCargo = it },
                        label = { Text("Cargo Militar / Operativo en la Base", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 12.sp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cedula,
                            onValueChange = { cedula = it },
                            label = { Text("Nº Cédula", fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                  focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFF59E0B),
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 12.sp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Teléfono", fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFF59E0B),
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 12.sp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                    }

                    // 2. Datos de Domicilio & Tooltip (DIRECTRICES)
                    Text("Datos de Domicilio", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = direccionHabitacion,
                        onValueChange = { direccionHabitacion = it },
                        label = { Text("Dirección de habitación permanente", fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 12.sp)
                    )

                    // Residencia actual en la Isla with specific Focus trigger Tooltip requirement (DIRECTRICES)
                    Column {
                        OutlinedTextField(
                            value = direccionResidencia,
                            onValueChange = { direccionResidencia = it },
                            label = { Text("Dirección de residencia en la isla", fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFF59E0B),
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { state ->
                                    isResidenciaFocused = state.isFocused
                                },
                            textStyle = TextStyle(fontSize = 12.sp)
                        )

                        if (isResidenciaFocused) {
                            Spacer(modifier = Modifier.height(3.dp))
                            // Tooltip de Requisito de UI exacto (DIRECTRICES)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF59E0B).copy(alpha = 0.15f)),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "Especificar lugar de residencia actual en la isla",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // 3. Nucleo Familiar (Cónyuge, Padres, Hijos dinámico) (DIRECTRICES)
                    Text("Núcleo Familiar", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = conyuge,
                        onValueChange = { conyuge = it },
                        label = { Text("Datos del Cónyuge / Pareja", fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 12.sp)
                    )

                    OutlinedTextField(
                        value = padres,
                        onValueChange = { padres = it },
                        label = { Text("Datos de los Padres (Nombres)", fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 12.sp)
                    )

                    // Dynamic children list (DIRECTRICES)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.3f)),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Lista Dinámica de Hijos", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(6.dp))

                            // Input fields for children and Add button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Sexo Selector Button Row
                                Row(
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                        .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                        .padding(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (tempChildSexo == "Varón") Color(0xFFD97706) else Color.Transparent)
                                            .clickable { tempChildSexo = "Varón" }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Varón", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (tempChildSexo == "Varón") Color.White else Color(0xFF64748B))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (tempChildSexo == "Hembra") Color(0xFFD97706) else Color.Transparent)
                                            .clickable { tempChildSexo = "Hembra" }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Hembra", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (tempChildSexo == "Hembra") Color.White else Color(0xFF64748B))
                                    }
                                }

                                OutlinedTextField(
                                    value = tempChildEdad,
                                    onValueChange = { tempChildEdad = it },
                                    label = { Text("Edad", fontSize = 10.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = Color(0xFFF59E0B)),
                                    modifier = Modifier.weight(0.7f),
                                    textStyle = TextStyle(fontSize = 11.sp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )

                                Button(
                                    onClick = {
                                        val age = tempChildEdad.toIntOrNull()
                                        if (age != null) {
                                            hijosList.add(Pair(tempChildSexo, age))
                                            tempChildEdad = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(38.dp)
                                ) {
                                    Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            // Current children chips list
                            if (hijosList.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    hijosList.forEachIndexed { idx, child ->
                                        Row(
                                            modifier = Modifier
                                                .padding(vertical = 2.dp)
                                                .background(Color(0xFF0F172A), RoundedCornerShape(14.dp))
                                                .border(0.5.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (child.first == "Hembra") Icons.Default.Female else Icons.Default.Male,
                                                contentDescription = null,
                                                tint = Color(0xFFF59E0B),
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Text("${child.first} (${child.second}a)", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            Icon(
                                                imageVector = Icons.Default.Cancel,
                                                contentDescription = "Borrar",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clickable { hijosList.removeAt(idx) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. Datos Medicos y Emergencia (DIRECTRICES)
                    Text("Parte Médico y Emergencia", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = grupoSanguineo,
                            onValueChange = { grupoSanguineo = it },
                            label = { Text("Grupo Sanguíneo (Ejem: A+, O-)", fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFF59E0B)
                            ),
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 12.sp)
                        )
                        OutlinedTextField(
                            value = alergias,
                            onValueChange = { alergias = it },
                            label = { Text("Alergias Conocidas", fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFF59E0B)
                            ),
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 12.sp)
                        )
                    }

                    OutlinedTextField(
                        value = contactoEmergencia,
                        onValueChange = { contactoEmergencia = it },
                        label = { Text("Contacto de Emergencia (Nombre y Nº)", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 12.sp)
                    )

                    // 5. Tallas / Vestuario (DIRECTRICES)
                    Text("Tallas de Intendencia / Vestuario", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.2f)),
                        border = BorderStroke(0.5.dp, Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = tallaCamisa,
                                    onValueChange = { tallaCamisa = it },
                                    label = { Text("Camisa", fontSize = 10.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = Color(0xFFF59E0B)),
                                    modifier = Modifier.weight(1f),
                                    textStyle = TextStyle(fontSize = 11.sp)
                                )
                                OutlinedTextField(
                                    value = tallaGuerrera,
                                    onValueChange = { tallaGuerrera = it },
                                    label = { Text("Guerrera", fontSize = 10.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = Color(0xFFF59E0B)),
                                    modifier = Modifier.weight(1f),
                                    textStyle = TextStyle(fontSize = 11.sp)
                                )
                                OutlinedTextField(
                                    value = tallaPantalon,
                                    onValueChange = { tallaPantalon = it },
                                    label = { Text("Pantalón", fontSize = 10.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = Color(0xFFF59E0B)),
                                    modifier = Modifier.weight(1f),
                                    textStyle = TextStyle(fontSize = 11.sp)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = tallaGorra,
                                    onValueChange = { tallaGorra = it },
                                    label = { Text("Gorra", fontSize = 10.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = Color(0xFFF59E0B)),
                                    modifier = Modifier.weight(1f),
                                    textStyle = TextStyle(fontSize = 11.sp)
                                )
                                OutlinedTextField(
                                    value = tallaQuepi,
                                    onValueChange = { tallaQuepi = it },
                                    label = { Text("Quepi", fontSize = 10.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = Color(0xFFF59E0B)),
                                    modifier = Modifier.weight(1f),
                                    textStyle = TextStyle(fontSize = 11.sp)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = tallaBotas,
                                    onValueChange = { tallaBotas = it },
                                    label = { Text("Botas", fontSize = 10.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = Color(0xFFF59E0B)),
                                    modifier = Modifier.weight(1f),
                                    textStyle = TextStyle(fontSize = 11.sp)
                                )
                                OutlinedTextField(
                                    value = tallaZapatos,
                                    onValueChange = { tallaZapatos = it },
                                    label = { Text("Zapatos", fontSize = 10.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = Color(0xFFF59E0B)),
                                    modifier = Modifier.weight(1f),
                                    textStyle = TextStyle(fontSize = 11.sp)
                                )
                            }
                        }
                    }

                    // Plazo de Estatus en base de datos
                    Text("Condición Naval / Estatus Temporal", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showStatusDropdown = true },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text(statusState, fontSize = 12.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = showStatusDropdown,
                            onDismissRequest = { showStatusDropdown = false },
                            modifier = Modifier.background(Color(0xFF1E293B))
                        ) {
                            statusOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt.name, color = Color.White) },
                                    onClick = {
                                        statusState = opt.name
                                        showStatusDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = fromDate,
                            onValueChange = { fromDate = it },
                            label = { Text("Desde (YYYY-MM-DD)", fontSize = 10.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFF59E0B),
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 11.sp)
                        )
                        OutlinedTextField(
                            value = toDate,
                            onValueChange = { toDate = it },
                            label = { Text("Hasta (YYYY-MM-DD)", fontSize = 10.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFF59E0B),
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 11.sp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Recordatorio activo del estatus", fontSize = 11.sp, color = Color.White, modifier = Modifier.weight(1f))
                        Switch(
                            checked = triggerAlarm,
                            onCheckedChange = { triggerAlarm = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFF59E0B),
                                checkedTrackColor = Color(0xFF1B2E49)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions Cancel / Save
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
                        Text("Cancelar", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                // Serialize dynamic children
                                val serializedKids = hijosList.joinToString(";") { "${it.first},${it.second}" }
                                val outOfficer = OfficerEntity(
                                    id = officer?.id ?: 0,
                                    name = name,
                                    cedula = cedula,
                                    phone = phone,
                                    rankCargo = rankCargo,
                                    status = statusState,
                                    statusFromDate = fromDate,
                                    statusToDate = toDate,
                                    hasAlarm = triggerAlarm,
                                    photoUri = photoUri,
                                    category = categoryState,
                                    direccionHabitacion = direccionHabitacion,
                                    direccionResidencia = direccionResidencia,
                                    conyuge = conyuge,
                                    padres = padres,
                                    hijosJson = serializedKids,
                                    grupoSanguineo = grupoSanguineo,
                                    alergias = alergias,
                                    contactoEmergencia = contactoEmergencia,
                                    tallaCamisa = tallaCamisa,
                                    tallaGuerrera = tallaGuerrera,
                                    tallaBotas = tallaBotas,
                                    tallaGorra = tallaGorra,
                                    tallaQuepi = tallaQuepi,
                                    tallaZapatos = tallaZapatos,
                                    tallaPantalon = tallaPantalon
                                )
                                onSave(outOfficer)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("Guardar Expediente", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// Dialog to add status options
@Composable
fun ManageStatusesDialog(
    statusOptions: List<StatusOptionEntity>,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit,
    onDelete: (StatusOptionEntity) -> Unit
) {
    var newStatusName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("CONFIGURACIÓN DE CONDICIONES", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFFF59E0B))
                Text("Tabla de Estatus del Personal", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                
                Spacer(modifier = Modifier.height(12.dp))

                // Input to add a new condition
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newStatusName,
                        onValueChange = { newStatusName = it },
                        label = { Text("Nueva Condición", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (newStatusName.isNotBlank()) {
                                onAdd(newStatusName)
                                newStatusName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text("Añadir", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Divider(color = Color(0xFF334155), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Estatus Definidos Actualmente:", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                
                LazyColumn(modifier = Modifier.height(160.dp).padding(vertical = 4.dp)) {
                    items(statusOptions) { opt ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(opt.name, color = Color.White, fontSize = 13.sp)
                            // Allow deletion of non-essential statuses safely
                            if (opt.name != "Activo") {
                                IconButton(onClick = { onDelete(opt) }, modifier = Modifier.size(24.dp)) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Eliminar estatus", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                }
                            } else {
                                Text("(Esencial)", color = Color(0xFF64748B), fontSize = 10.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar", color = Color.White)
                }
            }
        }
    }
}
