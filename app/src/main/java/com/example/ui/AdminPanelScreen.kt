package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.viewmodel.REDIMAINViewModel

@Composable
fun AdminPanelScreen(viewModel: REDIMAINViewModel) {
    val users by viewModel.usersState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = "Admin", tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
            Column {
                Text(text = "PANEL DE ADMINISTRACIÓN", color = Color(0xFFF59E0B), fontSize = 10.sp, fontWeight = FontWeight.Black)
                Text(text = "Gestión de Accesos y Roles", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        if (users.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFF59E0B))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(users) { user ->
                    UserRoleRow(user = user, onRoleChange = { newRole ->
                        viewModel.updateUserRole(user, UserRole.valueOf(newRole), {}, {})
                    })
                }
            }
        }
    }
}

@Composable
fun UserRoleRow(user: UserEntity, onRoleChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val roles = UserRole.values().map { it.name }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151D30)),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color(0xFF94A3B8))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = user.email, color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
            
            // Role Selector
            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, if (user.role == UserRole.SUPER_ADMIN.name) Color(0xFFEF4444) else Color(0xFF3B82F6))
                ) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(12.dp), tint = if (user.role == UserRole.SUPER_ADMIN.name) Color(0xFFEF4444) else Color(0xFF3B82F6))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = user.role, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(14.dp))
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color(0xFF1E293B))
                ) {
                    roles.forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role, color = Color.White, fontSize = 11.sp) },
                            onClick = {
                                expanded = false
                                onRoleChange(role)
                            }
                        )
                    }
                }
            }
        }
    }
}
