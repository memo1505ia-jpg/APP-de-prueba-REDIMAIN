package com.example.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.RowEntity
import com.example.data.model.TabEntity

@Composable
fun TacticalControlStats(
    currentRows: List<RowEntity>,
    activeTabName: String
) {
    val totalRows = currentRows.size
    val completedCount = currentRows.count { row ->
        row.cells.any { cell -> cell.contains("Completado", ignoreCase = true) || cell.contains("Completada", ignoreCase = true) }
    }
    val runningCount = currentRows.count { row ->
        row.cells.any { cell -> cell.contains("Ejecución", ignoreCase = true) || cell.contains("Progreso", ignoreCase = true) || cell.contains("Activo", ignoreCase = true) }
    }

    val completionPercentage = if (totalRows > 0) (completedCount.toFloat() / totalRows.toFloat() * 100).toInt() else 0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Left mini-meter: Completion rate
        Card(
            modifier = Modifier
                .weight(1.2f)
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF11141A))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(46.dp)
                        .drawBehind {
                            drawCircle(
                                color = Color(0xFF334155),
                                radius = size.minDimension / 2,
                                style = Stroke(width = 4.dp.toPx())
                            )
                            drawArc(
                                color = Color(0xFFF59E0B),
                                startAngle = -90f,
                                sweepAngle = (completionPercentage / 100f) * 360f,
                                useCenter = false,
                                style = Stroke(width = 4.dp.toPx())
                            )
                        }
                ) {
                    Text(
                        text = "$completionPercentage%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFF59E0B)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "EVALUACIÓN DE GESTIÓN",
                        fontSize = 8.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "$completedCount/$totalRows misiones",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Right mini-meter: Tactical status
        Card(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF11141A))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = "MISIONES EN CURSO",
                    fontSize = 8.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (runningCount > 0) Color(0xFFF59E0B) else Color(0xFF334155))
                    )
                    Text(
                        text = "$runningCount activas",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (runningCount > 0) Color(0xFFF59E0B) else Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ExcelMultiTabsSelector(
    tabs: List<TabEntity>,
    selectedTabId: Int?,
    onTabSelected: (Int) -> Unit,
    onAddTabClicked: () -> Unit,
    onDeleteTabClicked: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Source,
            contentDescription = "Pestañas",
            tint = Color(0xFFF59E0B),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "MATRIZ OPERATIVA:",
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF94A3B8),
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = onAddTabClicked,
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFD97706))
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Crear Cuadro",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(bottom = 6.dp)
    ) {
        items(tabs) { tab ->
            val isSelected = tab.id == selectedTabId
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isSelected) Color(0xFFD97706) else Color(0xFF1E293B)
                    )
                    .then(
                        if (!isSelected) {
                            Modifier.border(
                                width = 1.dp,
                                color = Color(0xFF334155),
                                shape = RoundedCornerShape(50)
                            )
                        } else Modifier
                    )
                    .combinedClickable(
                        onClick = { onTabSelected(tab.id) },
                        onLongClick = {
                            if (tabs.size > 1) {
                                onDeleteTabClicked(tab.id)
                            }
                        }
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isSelected) {
                        Canvas(modifier = Modifier.size(5.dp)) {
                            drawCircle(color = Color.White)
                        }
                    }
                    Text(
                        text = tab.name.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isSelected) Color.White else Color(0xFF94A3B8)
                    )

                    if (isSelected && tabs.size > 1) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Borrar",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(12.dp)
                                .clickable { onDeleteTabClicked(tab.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExcelSpreadsheetGrid(
    tab: TabEntity,
    rows: List<RowEntity>,
    isUnlocked: Boolean,
    onCellEditRequest: (RowEntity, Int) -> Unit,
    onDeleteRow: (RowEntity) -> Unit,
    onToggleConfidential: (RowEntity) -> Unit,
    onUpdateRowValue: (RowEntity, Int, String) -> Unit = { _, _, _ -> }
) {
    // Internal state for the quick-detail popup
    var detailRow by remember { mutableStateOf<RowEntity?>(null) }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF11141A)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFEF4444))
                    )
                    Text(
                        text = "Seguimiento Estratégico",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = "SEC-CONF-01",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    modifier = Modifier
                        .background(Color(0xFF1E293B), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Compact instruction hint
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
                    .background(Color(0xFF1E293B).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.TouchApp, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(12.dp))
                Text("Toca cualquier fila para ver detalles completos y editar", color = Color(0xFF94A3B8), fontSize = 8.sp)
            }

            // Column headers – compact
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Confidential indicator column
                Box(modifier = Modifier.width(36.dp))

                // Show first 2 columns only in the preview header
                val previewCols = tab.columns.take(2)
                previewCols.forEach { colHeader ->
                    Text(
                        text = colHeader.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFF59E0B),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Status column header
                Text(
                    text = "ESTADO",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.width(72.dp),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }

            Divider(color = Color(0xFF1E293B), modifier = Modifier.fillMaxWidth())

            // Compact rows list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                itemsIndexed(rows) { index, rowEntity ->
                    val isConfidentialLine = rowEntity.isConfidential
                    val isBlurred = isConfidentialLine && !isUnlocked

                    val isDelayedOrUnfulfilled = rowEntity.cells.any { cell ->
                        val c = cell.lowercase()
                        c.contains("retrasado") || c.contains("retrasada") || c.contains("incumplido") ||
                        c.contains("no cumplido") || c.contains("no cumplida") ||
                        c.contains("vencido") || c.contains("vencida") || c.contains("alerta")
                    }

                    // Detect status from any cell
                    val statusCell = rowEntity.cells.firstOrNull { cell ->
                        val c = cell.lowercase()
                        c.contains("completado") || c.contains("completada") || c.contains("100%") ||
                        c.contains("ejecución") || c.contains("progreso") || c.contains("activo") ||
                        c.contains("planificado") || c.contains("pendiente") || c.contains("retrasado")
                    } ?: ""

                    val statusColor = when {
                        statusCell.contains("Completado", ignoreCase = true) || statusCell.contains("100%") -> Color(0xFF10B981)
                        statusCell.contains("Ejecución", ignoreCase = true) || statusCell.contains("Activo", ignoreCase = true) || statusCell.contains("Progreso", ignoreCase = true) -> Color(0xFFFBBF24)
                        statusCell.contains("Retrasado", ignoreCase = true) || statusCell.contains("Incumplido", ignoreCase = true) -> Color(0xFFEF4444)
                        else -> Color(0xFF64748B)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                when {
                                    isDelayedOrUnfulfilled -> Color(0x1AEF4444)
                                    isConfidentialLine -> if (isUnlocked) Color(0x15EF4444) else Color(0x25EF4444)
                                    index % 2 == 0 -> Color(0xFF1E293B).copy(alpha = 0.25f)
                                    else -> Color.Transparent
                                }
                            )
                            .then(
                                if (isDelayedOrUnfulfilled) Modifier.border(BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.45f)))
                                else Modifier
                            )
                            .clickable { detailRow = rowEntity }
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Confidential icon (compact)
                        Icon(
                            imageVector = if (isConfidentialLine) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = if (isConfidentialLine) Color(0xFFEF4444) else Color(0xFF334155),
                            modifier = Modifier
                                .size(14.dp)
                                .width(36.dp)
                        )

                        // Preview of first 2 cells
                        val previewCols = tab.columns.take(2)
                        previewCols.forEachIndexed { colIdx, _ ->
                            val cellValue = rowEntity.cells.getOrNull(colIdx) ?: ""
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (isBlurred) {
                                    Text(
                                        text = "••••••••",
                                        color = Color(0xFFEF4444),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                            .blur(1.dp)
                                    )
                                } else {
                                    Text(
                                        text = cellValue,
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Status badge compact
                        Box(
                            modifier = Modifier
                                .width(72.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isBlurred) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(30.dp))
                                        .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text("CIFRADO", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                }
                            } else if (statusCell.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(30.dp))
                                        .background(statusColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = statusCell.take(10).uppercase(),
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Color(0xFF475569),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Divider(color = Color(0xFF1E293B).copy(alpha = 0.5f), modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }

    // Inline detail popup triggered by row tap
    if (detailRow != null) {
        RowQuickDetailDialog(
            tab = tab,
            row = detailRow!!,
            isUnlocked = isUnlocked,
            onDismiss = { detailRow = null },
            onEditTrigger = {
                onCellEditRequest(detailRow!!, 0)
                detailRow = null
            },
            onDelete = {
                onDeleteRow(detailRow!!)
                detailRow = null
            },
            onToggleConfidential = {
                onToggleConfidential(detailRow!!)
                detailRow = null
            },
            onStatusChange = { colIdx, newVal ->
                onUpdateRowValue(detailRow!!, colIdx, newVal)
                detailRow = null
            },
            onAlertChange = { colIdx, newVal ->
                onUpdateRowValue(detailRow!!, colIdx, newVal)
                detailRow = null
            }
        )
    }
}

// ─── NUEVO: Popup de detalle compacto con estatus editable y nivel de alerta ───
@Composable
fun RowQuickDetailDialog(
    tab: TabEntity,
    row: RowEntity,
    isUnlocked: Boolean,
    onDismiss: () -> Unit,
    onEditTrigger: () -> Unit,
    onDelete: () -> Unit,
    onToggleConfidential: () -> Unit,
    onStatusChange: (Int, String) -> Unit = { _, _ -> },
    onAlertChange: (Int, String) -> Unit = { _, _ -> }
) {
    val isBlurred = row.isConfidential && !isUnlocked

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E36)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // ── Header ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DETALLE DE REGISTRO",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = tab.name.uppercase(),
                            color = Color(0xFF64748B),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── Datos completos de la fila ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp)
                        .background(Color(0xFF070B12).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tab.columns.forEachIndexed { idx, colName ->
                            val cellValue = row.cells.getOrNull(idx) ?: "N/D"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "${colName.uppercase()}: ",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(90.dp)
                                )
                                if (isBlurred) {
                                    Text(
                                        text = "[CIFRADO]",
                                        color = Color(0xFFEF4444),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                } else {
                                    Text(
                                        text = if (cellValue.isBlank()) "—" else cellValue,
                                        color = if (cellValue.isBlank()) Color(0xFF475569) else Color.White,
                                        fontSize = 11.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ── Selector de Estatus (editable inline) ──
                Text(
                    text = "CAMBIAR ESTADO DE LA TAREA:",
                    color = Color(0xFF94A3B8),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Find which column is status-related
                val statusColIdx = tab.columns.indexOfFirst { col ->
                    col.contains("estado", ignoreCase = true) || col.contains("estatus", ignoreCase = true)
                }
                val currentStatus = if (statusColIdx >= 0) row.cells.getOrNull(statusColIdx) ?: "" else ""

                val statusOptions = listOf(
                    Triple("Planificado", Color(0xFF64748B), Icons.Default.Schedule),
                    Triple("En Ejecución", Color(0xFFFBBF24), Icons.Default.PlayArrow),
                    Triple("Completado", Color(0xFF10B981), Icons.Default.CheckCircle),
                    Triple("Retrasado", Color(0xFFEF4444), Icons.Default.Warning)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    statusOptions.forEach { (label, color, icon) ->
                        val isSelected = currentStatus.equals(label, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) color.copy(alpha = 0.2f) else Color(0xFF1E293B))
                                .border(
                                    1.dp,
                                    if (isSelected) color else Color(0xFF334155),
                                    RoundedCornerShape(8.dp)
                                )
                                .then(
                                     if (statusColIdx >= 0 && !isBlurred)
                                         Modifier.clickable { onStatusChange(statusColIdx, label) }
                                     else Modifier
                                 )
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) color else Color(0xFF475569),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = label.split(" ").first(),
                                    fontSize = 7.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                    color = if (isSelected) color else Color(0xFF64748B),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── Nivel de Alerta ──
                Text(
                    text = "NIVEL DE ALERTA / PRIORIDAD:",
                    color = Color(0xFF94A3B8),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                val alertColIdx = tab.columns.indexOfFirst { col ->
                    col.contains("prioridad", ignoreCase = true) || col.contains("alerta", ignoreCase = true) || col.contains("urgencia", ignoreCase = true)
                }
                val currentAlert = if (alertColIdx >= 0) row.cells.getOrNull(alertColIdx) ?: "" else ""

                val alertLevels = listOf(
                    Triple("BAJA", Color(0xFF10B981), Color(0x1510B981)),
                    Triple("MEDIA", Color(0xFFFBBF24), Color(0x15FBBF24)),
                    Triple("ALTA", Color(0xFFF97316), Color(0x15F97316)),
                    Triple("CRÍTICA", Color(0xFFEF4444), Color(0x15EF4444))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    alertLevels.forEach { (label, fgColor, bgColor) ->
                        val isSelected = currentAlert.equals(label, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) bgColor else Color(0xFF1E293B))
                                .border(
                                    1.dp,
                                    if (isSelected) fgColor else Color(0xFF334155),
                                    RoundedCornerShape(6.dp)
                                )
                                .then(
                                    if (alertColIdx >= 0 && !isBlurred)
                                        Modifier.clickable { onAlertChange(alertColIdx, label) }
                                    else Modifier
                                )
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 7.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                color = if (isSelected) fgColor else Color(0xFF64748B)
                            )
                        }
                    }
                }

                // Confidential indicator row
                if (row.isConfidential) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.1f))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                        Text("REGISTRO CLASIFICADO – Contenido cifrado en base de datos", color = Color(0xFFEF4444), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Botones de acción ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Confidential toggle
                    OutlinedButton(
                        onClick = onToggleConfidential,
                        border = BorderStroke(1.dp, if (row.isConfidential) Color(0xFFEF4444).copy(alpha = 0.5f) else Color(0xFF334155)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = if (row.isConfidential) Color(0xFFEF4444) else Color(0xFF94A3B8)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (row.isConfidential) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Delete
                    OutlinedButton(
                        onClick = onDelete,
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eliminar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Edit full record
                    Button(
                        onClick = onEditTrigger,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.5f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("EDITAR", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateIndicator(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.TableChart,
            contentDescription = "No data",
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            color = Color(0xFFA1AFBF),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun AddTabDialog(
    onDismiss: () -> Unit,
    onSave: (String, List<String>) -> Unit
) {
    var tabName by remember { mutableStateOf("") }
    var columnsStr by remember { mutableStateOf("Fecha, Nombre, Detalle, Estado") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E36))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "NUEVA REJILLA OPERACIONAL",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                OutlinedTextField(
                    value = tabName,
                    onValueChange = { tabName = it },
                    label = { Text("Nombre de la Pestaña / Tabla", color = Color(0xFFA1AFBF), fontSize = 11.sp) },
                    placeholder = { Text("Ej. Frontera Insular", color = Color.White.copy(alpha = 0.3f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFF1E2E44)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = columnsStr,
                    onValueChange = { columnsStr = it },
                    label = { Text("Columnas del Cuadro (separar por comas)", color = Color(0xFFA1AFBF), fontSize = 11.sp) },
                    placeholder = { Text("Ej. Barco, Zona, Alerta, Estatus", color = Color.White.copy(alpha = 0.3f)) },
                    supportingText = { Text("Configure las columnas como un cuadro dinámico de Excel.", color = Color(0xFFA1AFBF).copy(alpha = 0.6f), fontSize = 9.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFF1E2E44)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCELAR", color = Color(0xFFA1AFBF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (tabName.isNotBlank() && columnsStr.isNotBlank()) {
                                val colList = columnsStr.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                if (colList.isNotEmpty()) {
                                    onSave(tabName, colList)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("CREAR TABLA", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddRowDialog(
    tab: TabEntity,
    onDismiss: () -> Unit,
    onSave: (List<String>, Boolean) -> Unit
) {
    val cellValues = remember { mutableStateMapOf<Int, String>() }
    var isConfidential by remember { mutableStateOf(false) }

    LaunchedEffect(tab.id) {
        tab.columns.forEachIndexed { idx, _ ->
            cellValues[idx] = ""
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E36))
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "AÑADIR REGISTRO EN: ${tab.name.uppercase()}",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                tab.columns.forEachIndexed { idx, colName ->
                    val value = cellValues[idx] ?: ""
                    Column {
                        Text(
                            text = colName.uppercase(),
                            color = Color(0xFFA1AFBF),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        if (colName.contains("estado", ignoreCase = true) || colName.contains("estatus", ignoreCase = true)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("En Ejecución", "Completado", "Planificado").forEach { pill ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (value == pill) MaterialTheme.colorScheme.primary else Color(0xFF1E2E44))
                                            .clickable { cellValues[idx] = pill }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = pill, fontSize = 9.sp, color = if (value == pill) Color.Black else Color.White)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        OutlinedTextField(
                            value = value,
                            onValueChange = { cellValues[idx] = it },
                            placeholder = { Text("Ingresar $colName", color = Color.White.copy(alpha = 0.25f), fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color(0xFF1E2E44)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF14243B))
                        .clickable { isConfidential = !isConfidential }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isConfidential,
                        onCheckedChange = { isConfidential = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "MARCAR COMO CLASIFICADO",
                            color = if (isConfidential) Color(0xFFFFA0A0) else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Oculta los datos con cifrado militar.",
                            color = Color(0xFFA1AFBF),
                            fontSize = 8.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCELAR", color = Color(0xFFA1AFBF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val cells = tab.columns.indices.map { cellValues[it] ?: "" }
                            onSave(cells, isConfidential)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("REGISTRAR", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EditRowDialog(
    tab: TabEntity,
    row: RowEntity,
    onDismiss: () -> Unit,
    onSave: (RowEntity) -> Unit,
    onCellUpdate: (Int, String) -> Unit
) {
    val updatedCells = remember { mutableStateListOf<String>().apply { addAll(row.cells) } }

    LaunchedEffect(tab.id) {
        while (updatedCells.size < tab.columns.size) {
            updatedCells.add("")
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E36))
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "MODIFICAR FIAL EN: ${tab.name.uppercase()}",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                tab.columns.forEachIndexed { idx, colName ->
                    val value = updatedCells.getOrNull(idx) ?: ""
                    Column {
                        Text(
                            text = colName.uppercase(),
                            color = Color(0xFFA1AFBF),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        if (colName.contains("estado", ignoreCase = true) || colName.contains("estatus", ignoreCase = true)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("En Ejecución", "Completado", "Planificado").forEach { pill ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (value == pill) MaterialTheme.colorScheme.primary else Color(0xFF1E2E44))
                                            .clickable {
                                                updatedCells[idx] = pill
                                                onCellUpdate(idx, pill)
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = pill, fontSize = 9.sp, color = if (value == pill) Color.Black else Color.White)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        OutlinedTextField(
                            value = value,
                            onValueChange = { newValue ->
                                if (idx < updatedCells.size) {
                                    updatedCells[idx] = newValue
                                }
                                onCellUpdate(idx, newValue)
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color(0xFF1E2E44)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            onSave(row.copy(cells = updatedCells.toList()))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("CONCLUIR EDICIÓN", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ViewRowDetailDialog(
    tab: TabEntity,
    row: RowEntity,
    isUnlocked: Boolean,
    onDismiss: () -> Unit,
    onEditTrigger: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E36)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DETALLE DE REGISTRO",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "SECCIÓN / TABLA: ${tab.name.uppercase()}",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                
                Spacer(modifier = Modifier.height(14.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                        .background(Color(0xFF070B12).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        tab.columns.forEachIndexed { idx, colName ->
                            val cellValue = row.cells.getOrNull(idx) ?: "N/D"
                            val isConfidential = row.isConfidential
                            val isBlurred = isConfidential && !isUnlocked

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0D1424), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = colName.uppercase(),
                                    color = Color(0xFFA1AFBF),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (isBlurred) {
                                    Text(
                                        text = "[CONTENIDO CONFIDENCIAL CIFRADO]",
                                        color = Color(0xFFEF4444),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                } else {
                                    Text(
                                        text = if (cellValue.isBlank()) "(Vacío)" else cellValue,
                                        color = if (cellValue.isBlank()) Color(0xFF475569) else Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CERRAR", color = Color(0xFFA1AFBF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = onEditTrigger,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("EDITAR REGISTRO", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.combinedClickable(
    onClick: () -> Unit,
    onLongClick: () -> Unit
): Modifier = this.combinedClickable(
    onClick = onClick,
    onLongClick = onLongClick,
    onDoubleClick = null,
    onLongClickLabel = null,
    onClickLabel = null,
    role = null,
    indication = LocalIndication.current,
    interactionSource = remember { MutableInteractionSource() }
)
