package com.example

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.data.model.RowEntity
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.REDIMAINViewModel
import com.example.ui.CrewStaffControlScreen
import com.example.ui.ReportesControlScreen
import com.example.ui.SeguridadControlScreen
import com.example.ui.AddOrEditOfficerDialog
import com.example.ui.AddCommTrafficDialog
import com.example.ui.components.*
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                REDIMAINControlApp()
            }
        }
    }
}

@Composable
fun REDIMAINControlApp(
    viewModel: REDIMAINViewModel = viewModel()
) {
    val tabs by viewModel.tabsState.collectAsStateWithLifecycle()
    val selectedTabId by viewModel.selectedTabId.collectAsStateWithLifecycle()
    val currentRows by viewModel.currentRowsState.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessagesState.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    val isConfidentialUnlocked by viewModel.isConfidentialUnlocked.collectAsStateWithLifecycle()
    val chatError by viewModel.chatError.collectAsStateWithLifecycle()

    val officers by viewModel.officersState.collectAsStateWithLifecycle()
    val statusOptions by viewModel.statusOptionsState.collectAsStateWithLifecycle()
    val commTraffic by viewModel.commTrafficState.collectAsStateWithLifecycle()

    // UI state states
    var showAddTabDialog by remember { mutableStateOf(false) }
    var showAddRowDialog by remember { mutableStateOf(false) }
    var editRowTarget by remember { mutableStateOf<RowEntity?>(null) }
    var showAISheet by remember { mutableStateOf(false) }
    var selectedFooterTab by remember { mutableStateOf("Agenda") }

    // Unified Quick-Action Dialog States
    var showAddOfficerDialog by remember { mutableStateOf(false) }
    var showAddCommDialog by remember { mutableStateOf(false) }
    var showAddFuelDialog by remember { mutableStateOf(false) }
    val customToasts = remember { mutableStateListOf<CustomToast>() }
    // Capacidad 3 - Communications traffic monitor loop check
    LaunchedEffect(commTraffic) {
        commTraffic.forEach { comm ->
            if (comm.status == "Pendiente") {
                var isExceeded = false
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val receivedDate = sdf.parse(comm.dateReceived)
                    val today = Date()
                    val diffTime = today.time - (receivedDate?.time ?: today.time)
                    val diffDays = diffTime / (1000 * 60 * 60 * 24)
                    if (diffDays >= 1) { // 1 or more days old without response is exceeded in REDIMAIN's elite military response
                        isExceeded = true
                    }
                } catch (e: Exception) {
                    if (comm.hasDeadlineAlarm) {
                        isExceeded = true
                    }
                }
                
                if (isExceeded) {
                    val alertMessage = "🚨 MONITOREO IA: Oficio N° ${comm.documentNumber} (${comm.subject.take(20)}...) asignado a ${comm.recipientRankName} excede tiempo prudencial sin acción."
                    if (customToasts.none { it.message == alertMessage }) {
                        customToasts.add(CustomToast(message = alertMessage, type = ToastType.ALERT))
                    }
                }
            }
        }
    }

    val activeTab = tabs.find { it.id == selectedTabId }
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    if (currentUser == null) {
        UserLoginScreen(viewModel = viewModel)
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                Column {
                    Divider(color = Color(0xFF1E293B), thickness = 0.5.dp) // border-slate-800
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0A0C10)) // bg-[#0A0C10]
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FooterTabItem(
                            title = "Agenda",
                            icon = Icons.Default.Assignment,
                            isSelected = selectedFooterTab == "Agenda",
                            onClick = { selectedFooterTab = "Agenda" }
                        )
                        FooterTabItem(
                            title = "Equipo",
                            icon = Icons.Default.Group,
                            isSelected = selectedFooterTab == "Equipo",
                            onClick = { selectedFooterTab = "Equipo" }
                        )
                        FooterTabItem(
                            title = "Reportes",
                            icon = Icons.Default.BarChart,
                            isSelected = selectedFooterTab == "Reportes",
                            onClick = { selectedFooterTab = "Reportes" }
                        )
                        FooterTabItem(
                            title = "Seguro",
                            icon = Icons.Default.Security,
                            isSelected = selectedFooterTab == "Seguro",
                            onClick = { selectedFooterTab = "Seguro" }
                        )
                        FooterTabItem(
                            title = "Combustible",
                            icon = Icons.Default.LocalGasStation,
                            isSelected = selectedFooterTab == "Combustible",
                            onClick = { selectedFooterTab = "Combustible" }
                        )
                        if (currentUser?.role == com.example.data.model.UserRole.SUPER_ADMIN.name) {
                            FooterTabItem(
                                title = "Admin",
                                icon = Icons.Default.AdminPanelSettings,
                                isSelected = selectedFooterTab == "Admin",
                                onClick = { selectedFooterTab = "Admin" }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFF0A0C10)) // bg-[#0A0C10]
            ) {
                // Background maritime sonar lines design ornament using custom drawBehind
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            // Drawing decorative concentric radar circles matching strategic command HUD
                            val center = Offset(size.width * 0.85f, size.height * 0.15f)
                            drawCircle(
                                color = Color(0x0CF59E0B), // amber-500 soft alpha radar circle
                                radius = 180f,
                                center = center,
                                style = Stroke(width = 1.5f)
                            )
                            drawCircle(
                                color = Color(0x06F59E0B), // amber-500 soft alpha outer radar circle
                                radius = 350f,
                                center = center,
                                style = Stroke(width = 1f)
                            )
                            drawLine(
                                color = Color(0x0CF59E0B),
                                start = Offset(center.x - 400f, center.y),
                                end = Offset(center.x + 100f, center.y),
                                strokeWidth = 1f
                            )
                            drawLine(
                                color = Color(0x0CF59E0B),
                                start = Offset(center.x, center.y - 100f),
                                end = Offset(center.x, center.y + 400f),
                                strokeWidth = 1f
                            )
                        }
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Majestic Navy Top Crest Bar
                    NavyCommandHeader(
                        isConfidentialUnlocked = isConfidentialUnlocked,
                        onToggleUnlock = { viewModel.setConfidentialUnlocked(it) },
                        currentUser = currentUser,
                        onLogout = { viewModel.logout() }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // HIGH-TECH TACTICAL QUICK ACTION TOOLBAR (Asymmetric Angles & Responsive Positioning)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F1524))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFF10B981))
                            )
                            Text(
                                text = "ESTADO: EN LÍNEA • COMANDO",
                                color = Color(0xFF10B981),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.3.sp,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // AI ASSISTANT BUTTON WITH ASYMMETRIC DYNAMIC ANGLES
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(topStart = 14.dp, bottomEnd = 14.dp, bottomStart = 2.dp, topEnd = 2.dp))
                                    .background(MaterialTheme.colorScheme.tertiary)
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(topStart = 14.dp, bottomEnd = 14.dp, bottomStart = 2.dp, topEnd = 2.dp))
                                    .clickable { showAISheet = !showAISheet }
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Asistente IA",
                                        tint = Color.Black,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = "ASISTENTE IA",
                                        color = Color.Black,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // '+' CONTEXTUAL MULTI-FUNCTION BUTTON
                            val hasAddAction = selectedFooterTab == "Agenda" || selectedFooterTab == "Equipo" || selectedFooterTab == "Reportes" || selectedFooterTab == "Combustible"
                            if (hasAddAction) {
                                val actionLabel = when (selectedFooterTab) {
                                    "Agenda" -> "REGISTRO"
                                    "Equipo" -> "OFICIAL"
                                    "Reportes" -> "OFICIO"
                                    "Combustible" -> "SURTIDO"
                                    else -> "AGREGAR"
                                }
                                val actionColor = if (selectedFooterTab == "Combustible") Color(0xFFF59E0B) else Color(0xFFD97706)
                                
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 2.dp, topStart = 2.dp))
                                        .background(actionColor)
                                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 2.dp, topStart = 2.dp))
                                        .clickable {
                                            when (selectedFooterTab) {
                                                "Agenda" -> if (activeTab != null) { showAddRowDialog = true }
                                                "Equipo" -> showAddOfficerDialog = true
                                                "Reportes" -> showAddCommDialog = true
                                                "Combustible" -> showAddFuelDialog = true
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Agregar",
                                            tint = if (selectedFooterTab == "Combustible") Color.Black else Color.White,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = "+ $actionLabel",
                                            color = if (selectedFooterTab == "Combustible") Color.Black else Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    when (selectedFooterTab) {
                        "Agenda" -> {
                            // Tactical Board Statistics Metrics Panel
                            TacticalControlStats(currentRows = currentRows, activeTabName = activeTab?.name ?: "")

                            Spacer(modifier = Modifier.height(16.dp))

                            // Custom sheets scroll panel tab-selector (Multi-pestañas Excel)
                            ExcelMultiTabsSelector(
                                tabs = tabs,
                                selectedTabId = selectedTabId,
                                onTabSelected = { viewModel.selectTab(it) },
                                onAddTabClicked = { showAddTabDialog = true },
                                onDeleteTabClicked = { viewModel.deleteTab(it) }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Excel-like customized columns grid table
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                if (activeTab == null) {
                                    EmptyStateIndicator(
                                        title = "No hay Tablas de Control",
                                        description = "Presione el botón '+' al final de las pestañas para crear un nuevo cuadro personalizado de misiones."
                                    )
                                } else if (currentRows.isEmpty()) {
                                    EmptyStateIndicator(
                                        title = "Pestaña Vacía: ${activeTab.name}",
                                        description = "Presione el botón flotante '+' en la esquina inferior para registrar información en las columnas: \n${activeTab.columns.joinToString(", ")}"
                                    )
                                } else {
                                    ExcelSpreadsheetGrid(
                                        tab = activeTab,
                                        rows = currentRows,
                                        isUnlocked = isConfidentialUnlocked,
                                        onCellEditRequest = { row, colIdx ->
                                            editRowTarget = row
                                        },
                                        onDeleteRow = { viewModel.deleteRow(it) },
                                        onToggleConfidential = { viewModel.toggleRowConfidentiality(it) },
                                        onUpdateRowValue = { row, colIdx, newVal ->
                                            viewModel.updateRowValue(row, colIdx, newVal)
                                        }
                                    )
                                }
                            }
                        }
                        "Equipo" -> {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                CrewStaffControlScreen(
                                    officers = officers,
                                    statusOptions = statusOptions,
                                    viewModel = viewModel
                                )
                            }
                        }
                        "Reportes" -> {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                ReportesControlScreen(
                                    currentRows = currentRows,
                                    officers = officers,
                                    commTraffic = commTraffic,
                                    viewModel = viewModel
                                )
                            }
                        }
                        "Seguro" -> {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                SeguridadControlScreen(
                                    tabs = tabs,
                                    currentRows = currentRows,
                                    viewModel = viewModel,
                                    isUnlocked = isConfidentialUnlocked
                                )
                            }
                        }
                        "Combustible" -> {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                CombustibleControlScreen(viewModel = viewModel)
                            }
                        }
                        "Admin" -> {
                            if (currentUser?.role == com.example.data.model.UserRole.SUPER_ADMIN.name) {
                                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                    AdminPanelScreen(viewModel = viewModel)
                                }
                            }
                        }
                    }
                }

                // AI Intelligence Chat Panel Overlay
                AnimatedVisibility(
                    visible = showAISheet,
                    enter = slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                    ),
                    exit = slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(durationMillis = 300)
                    ),
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 550.dp)
                        .align(Alignment.CenterEnd)
                        .background(Color(0xFF071120).copy(alpha = 0.98f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        .padding(vertical = 12.dp)
                ) {
                    IntelligencePanel(
                        chatMessages = chatMessages,
                        isLoading = isChatLoading,
                        errorMsg = chatError,
                        onSendMessage = { viewModel.askAssistant(it) },
                        onClearChat = { viewModel.clearChat() },
                        onClosePanel = { showAISheet = false }
                    )
                }

                // Floating Toast Notifications (Capacidad 3)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp, start = 16.dp, end = 16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().wrapContentHeight()
                    ) {
                        customToasts.forEach { toast ->
                            FloatingToastItem(toast = toast, onDismiss = { customToasts.remove(toast) })
                        }
                    }
                }
            }
        }

        // Dynamic Dialogs setup
        if (showAddTabDialog) {
            AddTabDialog(
                onDismiss = { showAddTabDialog = false },
                onSave = { name, list ->
                    viewModel.addCustomTab(name, list)
                    showAddTabDialog = false
                }
            )
        }

        if (showAddRowDialog && activeTab != null) {
            AddRowDialog(
                tab = activeTab,
                onDismiss = { showAddRowDialog = false },
                onSave = { cells, isConf ->
                    viewModel.addRowToSelectedTab(cells, isConf)
                    showAddRowDialog = false
                }
            )
        }



        if (editRowTarget != null && activeTab != null) {
            EditRowDialog(
                tab = activeTab,
                row = editRowTarget!!,
                onDismiss = { editRowTarget = null },
                onSave = { updatedRow ->
                    viewModel.updateRowValue(updatedRow, 0, updatedRow.cells[0])
                    editRowTarget = null
                },
                onCellUpdate = { colIdx, newVal ->
                    viewModel.updateRowValue(editRowTarget!!, colIdx, newVal)
                }
            )
        }

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

        if (showAddFuelDialog) {
            val context = LocalContext.current
            val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            AddFuelTransactionDialog(
                onDismiss = { showAddFuelDialog = false },
                onSave = { person, vehicle, plate, liters, type, date ->
                    viewModel.addFuelTransaction(person, vehicle, plate, liters, type, date)
                    showAddFuelDialog = false

                    // Alert check
                    val currentWeekTxs = viewModel.transactionsState.value.filter {
                        it.plate.trim().lowercase() == plate.trim().lowercase() || 
                        it.personName.trim().lowercase() == person.trim().lowercase()
                    }
                    val weekTotal = currentWeekTxs.sumOf { it.liters } + liters
                    val userLimit = viewModel.limitsState.value.find { 
                        it.plateOrPerson.trim().lowercase() == plate.trim().lowercase() || 
                        it.plateOrPerson.trim().lowercase() == person.trim().lowercase()
                    }?.weeklyLimitLiters ?: 80.0

                    if (weekTotal > userLimit) {
                        Toast.makeText(context, "⚠️ ALERTA: ¡Límite semanal superado! Cupo asignado: $userLimit L. Surtido total: $weekTotal L.", Toast.LENGTH_LONG).show()
                    } else if (weekTotal >= userLimit * 0.9) {
                        Toast.makeText(context, "⚠️ ALERTA: Suministro cercano al cupo semanal ($weekTotal L / $userLimit L)", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Registro de combustible surtido guardado correctamente.", Toast.LENGTH_SHORT).show()
                    }
                },
                todayString = todayDateString
            )
        }
    }
}
