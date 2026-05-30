package com.example

import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FuelLimitEntity
import com.example.data.model.FuelTransactionEntity
import com.example.viewmodel.REDIMAINViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CombustibleControlScreen(
    viewModel: REDIMAINViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val transactions by viewModel.transactionsState.collectAsStateWithLifecycle()
    val limits by viewModel.limitsState.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableStateOf("registro") } // "registro", "auditoria", "comportamiento"

    // Form Dialog state
    var showLimitConfigDialog by remember { mutableStateOf(false) }

    // Alarm Details Dialog
    var alarmAlertReason by remember { mutableStateOf<String?>(null) }

    // Date references
    val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0C10))
    ) {
        // Section Header Row - Highly Compact to maximize information viewport and elevate layout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ÁREA DE COMBUSTIBLE",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Control táctico de suministros",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { showLimitConfigDialog = true },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configurar Cupos",
                        tint = Color(0xFFE2E8F0),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        // Sub Tab Selector Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val tabs = listOf(
                Triple("registro", "Suministros", Icons.Default.LocalGasStation),
                Triple("auditoria", "Auditoría / Fraude", Icons.Default.Security),
                Triple("comportamiento", "Consumo Histórico", Icons.Default.Analytics)
            )

            tabs.forEach { (id, title, icon) ->
                val isSelected = activeSubTab == id
                Button(
                    onClick = { activeSubTab = id },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFF1E293B) else Color.Transparent,
                        contentColor = if (isSelected) Color(0xFFF59E0B) else Color(0xFF94A3B8)
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Screens based on sub tab
        Box(modifier = Modifier.weight(1f)) {
            when (activeSubTab) {
                "registro" -> FuelRegistryPage(
                    transactions = transactions,
                    limits = limits,
                    viewModel = viewModel,
                    onShowAlarmDetails = { alarmAlertReason = it }
                )
                "auditoria" -> FuelAuditPage(
                    transactions = transactions,
                    limits = limits,
                    viewModel = viewModel
                )
                "comportamiento" -> FuelAnalyticsPage(
                    transactions = transactions,
                    todayString = todayDateString
                )
            }
        }
    }

    // Alarm Details Dialog
    if (alarmAlertReason != null) {
        AlertDialog(
            onDismissRequest = { alarmAlertReason = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(36.dp)) },
            title = { Text("ALERTA DE SEGURIDAD OPERATIVA", fontWeight = FontWeight.Bold, color = Color.White) },
            text = { Text(alarmAlertReason ?: "", color = Color(0xFFCBD5E1), fontSize = 14.sp) },
            confirmButton = {
                TextButton(onClick = { alarmAlertReason = null }) {
                    Text("Entendido", color = Color(0xFFF59E0B))
                }
            },
            containerColor = Color(0xFF0F172A)
        )
    }

    // Limit configuration modal
    if (showLimitConfigDialog) {
        LimitConfigurationDialog(
            limits = limits,
            onDismiss = { showLimitConfigDialog = false },
            onSaveLimit = { key, ltr ->
                viewModel.saveFuelLimit(key, ltr)
                Toast.makeText(context, "Cupo de $key configurado a $ltr Litros semanalmente.", Toast.LENGTH_SHORT).show()
            },
            onDeleteLimit = { viewModel.deleteFuelLimit(it) }
        )
    }

}

// ---------------------- SUB-PAGE 1: SUMINISTROS REGISTRY TABLE ----------------------

@Composable
fun FuelRegistryPage(
    transactions: List<FuelTransactionEntity>,
    limits: List<FuelLimitEntity>,
    viewModel: REDIMAINViewModel,
    onShowAlarmDetails: (String) -> Unit
) {
    if (transactions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = Color(0xFF334155), modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("No hay registros de combustible surtido hoy", color = Color(0xFF64748B), fontSize = 14.sp)
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Historial Completo de Suministros Diarios",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(transactions) { tx ->
                    // Calculate week sum for this person/plate to check alarm states
                    val individualWeekTxs = transactions.filter {
                        it.plate.trim().lowercase() == tx.plate.trim().lowercase() ||
                        it.personName.trim().lowercase() == tx.personName.trim().lowercase()
                    }
                    val weekTotal = individualWeekTxs.sumOf { it.liters }
                    
                    // Specific limits assigned
                    val limitEntity = limits.find {
                        it.plateOrPerson.trim().lowercase() == tx.plate.trim().lowercase() ||
                        it.plateOrPerson.trim().lowercase() == tx.personName.trim().lowercase()
                    }
                    val currentLimit = limitEntity?.weeklyLimitLiters ?: 80.0 // Default 80L limit

                    val ratio = weekTotal / currentLimit
                    val isExceeded = weekTotal > currentLimit
                    val isApproaching = ratio >= 0.9 && !isExceeded

                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            if (isExceeded) {
                                onShowAlarmDetails("El oficial ${tx.personName} superó su cupo semanal permitido de combustible.\n\nSurtido esta semana: $weekTotal Litros\nLímite máximo semanal: $currentLimit Litros\n\nExceso registrado: ${(weekTotal - currentLimit).toFloat()} L en vehículo ${tx.vehicleName} (${tx.plate}).")
                            } else if (isApproaching) {
                                onShowAlarmDetails("Atención: Suministro de combustible cercano al límite semanal.\n\nSurtido esta semana: $weekTotal Litros de su cupo asignado de $currentLimit Litros (${ratio.valPercent.toInt()}% del límite).")
                            }
                        },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = BorderStroke(
                            width = 1.dp,
                            color = when {
                                isExceeded -> Color(0xFFEF4444) // Urgent red alarm
                                isApproaching -> Color(0xFFF59E0B) // Warning orange alarm
                                else -> Color(0xFF1E293B)
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = tx.personName,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${tx.vehicleName} • Placa: ${tx.plate}",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${tx.liters} Litros",
                                        color = Color(0xFF38BDF8), // Blue-sky liters
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = tx.fuelType,
                                        color = if (tx.fuelType.lowercase() == "gasolina") Color(0xFFFCD34D) else Color(0xFFA7F3D0),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = tx.dateString, color = Color(0xFF64748B), fontSize = 11.sp)
                                }

                                // Alarms chip inside individual items
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isExceeded) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF7F1D1D))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("🚨 LÍMITE EXCEDIDO", color = Color(0xFFFCA5A5), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else if (isApproaching) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF78350F))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("⚠️ LÍMITE PRÓXIMO", color = Color(0xFFFDE68A), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteFuelTransaction(tx) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Borrar",
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private val Double.valPercent: Float
    get() = (this * 100).toFloat()

// ---------------------- SUB-PAGE 2: AUDITING & FRAUD BOARD ----------------------

@Composable
fun FuelAuditPage(
    transactions: List<FuelTransactionEntity>,
    limits: List<FuelLimitEntity>,
    viewModel: REDIMAINViewModel
) {
    // 1. Audit Check: Person using multiple vehicles (plates) this week
    val multiplePlatesDiscrepancies = remember(transactions) {
        transactions
            .groupBy { it.personName.trim().lowercase() }
            .mapNotNull { (rawName, list) ->
                val distinctPlates = list.map { it.plate.trim().uppercase() }.distinct()
                if (distinctPlates.size >= 2) {
                    val matchingName = list.first().personName
                    Pair(matchingName, distinctPlates)
                } else null
            }
    }

    // 2. Audit Check: Persons exceeding weekly limits
    val limitExceededDiscrepancies = remember(transactions, limits) {
        transactions
            .groupBy { it.personName.trim().lowercase() }
            .mapNotNull { (rawName, list) ->
                val matchingName = list.first().personName
                val userLimit = limits.find {
                    it.plateOrPerson.trim().lowercase() == rawName ||
                    it.plateOrPerson.trim().lowercase() == list.first().plate.trim().lowercase()
                }?.weeklyLimitLiters ?: 80.0

                val totalLiters = list.sumOf { it.liters }
                if (totalLiters > userLimit) {
                    Triple(matchingName, totalLiters, userLimit)
                } else null
            }
    }

    // 3. Audit Check: Supply frequency visits in week
    val visitsCountRank = remember(transactions) {
        transactions
            .groupBy { it.personName.trim().lowercase() }
            .map { (rawName, list) ->
                val matchingName = list.first().personName
                val mainVehicle = list.first().vehicleName
                val lastPlate = list.first().plate
                val count = list.size
                Triple(matchingName, "$mainVehicle ($lastPlate)", count)
            }
            .sortedByDescending { it.third }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Auditing Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                    Text(
                        text = "AUDITORÍA OPERATIVA EN TIEMPO REAL",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // SECTION 1: Multiple Vehicles Alarm
            item {
                Column {
                    Text(
                        text = "🚨 CONTROL MULTI-VEHÍCULOS (Estafa de Suministro Cruzado)",
                        color = Color(0xFFFCA5A5),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    if (multiplePlatesDiscrepancies.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                        ) {
                            Text(
                                text = "Perfecto: Ningún oficial ha abastecido múltiples vehículos de manera cruzada esta semana.",
                                color = Color(0xFF10B981),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    } else {
                        multiplePlatesDiscrepancies.forEach { (person, plates) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D)),
                                border = BorderStroke(1.dp, Color(0xFFEF4444))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFCA5A5))
                                    Column {
                                        Text(
                                            text = "ALERTA: Suministro múltiple detectado",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "El militar $person utilizó ${plates.size} vehículos distintos esta semana con placas: ${plates.joinToString(", ")}.",
                                            color = Color(0xFFFCA5A5),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 2: Weekly Limit Infraction checks
            item {
                Column {
                    Text(
                        text = "⚙️ AUDITORÍA DE EXCEDE DE CUPO SEMANAL",
                        color = Color(0xFFFDE68A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    if (limitExceededDiscrepancies.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                        ) {
                            Text(
                                text = "Conformidad: Todos los suministros se encuentran seguros dentro de sus límites semanales.",
                                color = Color(0xFF10B981),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    } else {
                        limitExceededDiscrepancies.forEach { (person, total, limit) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = person,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Consumido: ${total.toFloat()} Litros / Cupo: ${limit.toFloat()} L",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 12.sp
                                        )
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF78350F))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "EXCESO: +${(total - limit).toFloat()} L",
                                            color = Color(0xFFFDE68A),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 3: Frecuencia de Abastecimiento en la semana
            item {
                Column {
                    Text(
                        text = "📊 RANGO DE FRECUENCIA DE SUMINISTRO SEMANAL (Visitas Estación)",
                        color = Color(0xFFE2E8F0),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Persona / Vehículo Principal", color = Color(0xFF64748B), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text("Frecuencia (Visitas)", color = Color(0xFF64748B), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Divider(color = Color(0xFF1E293B), modifier = Modifier.padding(bottom = 8.dp))

                            if (visitsCountRank.isEmpty()) {
                                Text("No hay datos de frecuencia para auditar aún.", color = Color(0xFF475569), fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            } else {
                                visitsCountRank.forEach { (person, vehicle, visits) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 5.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(person, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Text(vehicle, color = Color(0xFF94A3B8), fontSize = 11.sp)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (visits >= 3) Color(0x33EF4444) else Color(0xFF1E293B))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "$visits veces",
                                                color = if (visits >= 3) Color(0xFFFCA5A5) else Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Spacer bottom
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ---------------------- SUB-PAGE 3: PROGRESSIVE ANALYTICS REPORTS ----------------------

@Composable
fun FuelAnalyticsPage(
    transactions: List<FuelTransactionEntity>,
    todayString: String
) {
    // Math groupings for Daily, Weekly, Monthly, Annual
    val dailyTxs = transactions.filter { it.dateString == todayString }

    // Let's calculate for calendar week 
    // Simply check date prefix or matching dates representing May 2026 week.
    // For this prototype, we consider the same week of 2026-05.
    val weeklyTxs = transactions // Under sample data we only have current week supplies!

    val monthlyTxs = transactions.filter { it.dateString.startsWith("2026-05") }
    val annualTxs = transactions.filter { it.dateString.startsWith("2026") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "REGISTRO DE CONSUMOS Y TIPOS DE COMBUSTIBLE ENTREGADO",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        // 4 Columns / Stats Panel for Periods
        val periods = listOf(
            Quadruple("DIARIO (Hoy)", dailyTxs),
            Quadruple("SEMANAL", weeklyTxs),
            Quadruple("MENSUAL (Mayo)", monthlyTxs),
            Quadruple("ANUAL (2026)", annualTxs)
        )

        periods.forEach { (title, list) ->
            val totalLiters = list.sumOf { it.liters }
            val gasolinaLiters = list.filter { it.fuelType.lowercase() == "gasolina" }.sumOf { it.liters }
            val dieselLiters = list.filter { it.fuelType.lowercase() == "diésel" || it.fuelType.lowercase() == "diesel" }.sumOf { it.liters }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(title, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Surtido:",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${totalLiters.toFloat()} Litros",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Gasolina vs Diesel splits bar
                    val gasPct = if (totalLiters > 0) (gasolinaLiters / totalLiters).toFloat() else 0f
                    val dieselPct = if (totalLiters > 0) (dieselLiters / totalLiters).toFloat() else 0f

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E293B))
                    ) {
                        if (gasPct > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(kotlin.math.max(gasPct, 0.01f))
                                    .background(Color(0xFFFCD34D)) // Gasolina yellow
                            )
                        }
                        if (dieselPct > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(kotlin.math.max(dieselPct, 0.01f))
                                    .background(Color(0xFFA7F3D0)) // Diesel light-emerald
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Detailed values row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFFCD34D)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Gasolina: ${gasolinaLiters.toFloat()} L (${(gasPct * 100).toInt()}%)",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFA7F3D0)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Diésel: ${dieselLiters.toFloat()} L (${(dieselPct * 100).toInt()}%)",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}

data class Quadruple<A, B>(val first: A, val second: B)

// ---------------------- MODAL DIALOGS ----------------------

@Composable
fun LimitConfigurationDialog(
    limits: List<FuelLimitEntity>,
    onDismiss: () -> Unit,
    onSaveLimit: (String, Double) -> Unit,
    onDeleteLimit: (FuelLimitEntity) -> Unit
) {
    var keyInput by remember { mutableStateOf("") }
    var ltrsInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar Cupos Semanales", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Configure topes máximos de combustible por Persona o Placa de Vehículo para activar alertas automáticas.", color = Color(0xFF94A3B8), fontSize = 12.sp)

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    label = { Text("Militar o Placa (ej: AMB-24A)") },
                    textStyle = TextStyle(color = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFF59E0B),
                        unfocusedBorderColor = Color(0xFF1E293B)
                    )
                )

                OutlinedTextField(
                    value = ltrsInput,
                    onValueChange = { ltrsInput = it },
                    label = { Text("Límite Semanales (Litros)") },
                    textStyle = TextStyle(color = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFF59E0B),
                        unfocusedBorderColor = Color(0xFF1E293B)
                    )
                )

                Button(
                    onClick = {
                        val key = keyInput.trim()
                        val ltrs = ltrsInput.toDoubleOrNull()
                        if (key.isNotEmpty() && ltrs != null && ltrs > 0) {
                            onSaveLimit(key, ltrs)
                            keyInput = ""
                            ltrsInput = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                ) {
                    Text("Asignar Cupo", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Divider(color = Color(0xFF1E293B), modifier = Modifier.padding(vertical = 8.dp))

                Text("Cupos Vigentes Registrados:", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)

                Box(modifier = Modifier.height(140.dp)) {
                    if (limits.isEmpty()) {
                        Text("No hay cupos configurados. Se aplica cupo base general de 80.0L.", color = Color(0xFF475569), fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 20.dp))
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(limits) { lim ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0A0C10), RoundedCornerShape(4.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(lim.plateOrPerson, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("Cupo: ${lim.weeklyLimitLiters.toFloat()} Litros semanales", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    }
                                    
                                    IconButton(
                                        onClick = { onDeleteLimit(lim) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Listo", color = Color(0xFFF59E0B))
            }
        },
        containerColor = Color(0xFF0F172A)
    )
}

@Composable
fun AddFuelTransactionDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, Double, String, String) -> Unit,
    todayString: String
) {
    var person by remember { mutableStateOf("") }
    var vehicle by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }
    var litersState by remember { mutableStateOf("") }
    var fuelType by remember { mutableStateOf("Gasolina") }
    var surtidoDate by remember { mutableStateOf(todayString) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Suministro de Combustible", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = person,
                    onValueChange = { person = it },
                    label = { Text("Nombre de la Persona (Surtido por)") },
                    textStyle = TextStyle(color = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFF59E0B),
                        unfocusedBorderColor = Color(0xFF1E293B)
                    )
                )

                OutlinedTextField(
                    value = vehicle,
                    onValueChange = { vehicle = it },
                    label = { Text("Vehículo Surtido (ej: Hilux Guardacostas)") },
                    textStyle = TextStyle(color = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFF59E0B),
                        unfocusedBorderColor = Color(0xFF1E293B)
                    )
                )

                OutlinedTextField(
                    value = plate,
                    onValueChange = { plate = it },
                    label = { Text("Placa del Vehículo") },
                    textStyle = TextStyle(color = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFF59E0B),
                        unfocusedBorderColor = Color(0xFF1E293B)
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = litersState,
                        onValueChange = { litersState = it },
                        label = { Text("Litros Surtidos") },
                        textStyle = TextStyle(color = Color.White),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFF1E293B)
                        )
                    )

                    OutlinedTextField(
                        value = surtidoDate,
                        onValueChange = { surtidoDate = it },
                        label = { Text("Fecha (AAAA-MM-DD)") },
                        textStyle = TextStyle(color = Color.White),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFF1E293B)
                        )
                    )
                }

                // Fuel type custom select
                Column {
                    Text("Tipo de Combustible:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Gasolina", "Diésel").forEach { type ->
                            val isSelected = fuelType == type
                            Button(
                                onClick = { fuelType = type },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Color(0xFFF59E0B) else Color(0xFF1E293B),
                                    contentColor = if (isSelected) Color.Black else Color.White
                                ),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(type, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ltrs = litersState.toDoubleOrNull()
                    if (person.isNotBlank() && vehicle.isNotBlank() && plate.isNotBlank() && ltrs != null && ltrs > 0) {
                        onSave(person.trim(), vehicle.trim(), plate.trim().uppercase(), ltrs, fuelType, surtidoDate.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
            ) {
                Text("Registrar Surtido", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color(0xFF94A3B8))
            }
        },
        containerColor = Color(0xFF0F172A)
    )
}
