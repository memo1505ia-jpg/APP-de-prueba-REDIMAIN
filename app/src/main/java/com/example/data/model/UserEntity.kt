package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val name: String,
    val registeredAt: Long = System.currentTimeMillis(),

    // Sistema de roles RBAC
    // Valores: SUPER_ADMIN | ADMIN_PERSONAL | ADMIN_COMMS | ADMIN_FUEL | VIEWER
    val role: String = UserRole.VIEWER.name,

    // Permisos granulares por sección (formato: "personal:WRITE,comms:READ,fuel:NONE,agenda:READ")
    // Si está vacío, se usan los permisos por defecto del rol.
    val customPermissions: String = ""
)
