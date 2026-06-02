package com.example.data.model

/**
 * Roles del sistema REDIMAIN.
 * El SUPER_ADMIN puede ver y modificar todo, y asignar roles al resto.
 */
enum class UserRole(val displayName: String) {
    SUPER_ADMIN("Super Administrador"),
    ADMIN_PERSONAL("Admin de Personal"),
    ADMIN_COMMS("Admin de Comunicaciones"),
    ADMIN_FUEL("Admin de Combustible"),
    VIEWER("Solo Lectura")
}

/**
 * Permisos individuales por sección de la app.
 * Cada permiso tiene un nivel: READ, WRITE o NONE.
 */
enum class PermissionLevel { NONE, READ, WRITE }

/**
 * Mapa de permisos por sección para un usuario.
 * Las secciones son: personal, comms, fuel, agenda, chat.
 */
data class UserPermissions(
    val personal: PermissionLevel = PermissionLevel.NONE,
    val comms:    PermissionLevel = PermissionLevel.NONE,
    val fuel:     PermissionLevel = PermissionLevel.NONE,
    val agenda:   PermissionLevel = PermissionLevel.NONE,
    val chat:     PermissionLevel = PermissionLevel.READ
) {
    companion object {
        /** Super Admin: acceso total a todo */
        fun superAdmin() = UserPermissions(
            personal = PermissionLevel.WRITE,
            comms    = PermissionLevel.WRITE,
            fuel     = PermissionLevel.WRITE,
            agenda   = PermissionLevel.WRITE,
            chat     = PermissionLevel.WRITE
        )

        /** Solo lectura en todo */
        fun viewerOnly() = UserPermissions(
            personal = PermissionLevel.READ,
            comms    = PermissionLevel.READ,
            fuel     = PermissionLevel.READ,
            agenda   = PermissionLevel.READ,
            chat     = PermissionLevel.READ
        )

        /** Admin de una sola sección */
        fun adminPersonal() = UserPermissions(personal = PermissionLevel.WRITE, comms = PermissionLevel.READ, fuel = PermissionLevel.READ, agenda = PermissionLevel.READ)
        fun adminComms()    = UserPermissions(personal = PermissionLevel.READ,  comms = PermissionLevel.WRITE, fuel = PermissionLevel.READ, agenda = PermissionLevel.READ)
        fun adminFuel()     = UserPermissions(personal = PermissionLevel.READ,  comms = PermissionLevel.READ,  fuel = PermissionLevel.WRITE, agenda = PermissionLevel.READ)

        /** Permisos por defecto según rol */
        fun fromRole(role: UserRole): UserPermissions = when (role) {
            UserRole.SUPER_ADMIN     -> superAdmin()
            UserRole.ADMIN_PERSONAL  -> adminPersonal()
            UserRole.ADMIN_COMMS     -> adminComms()
            UserRole.ADMIN_FUEL      -> adminFuel()
            UserRole.VIEWER          -> viewerOnly()
        }
    }
}

/**
 * Evalúa si el usuario puede realizar una acción sobre una sección.
 */
object UserPermissionsManager {
    fun canWrite(permissions: UserPermissions, section: String): Boolean {
        val level = getSectionLevel(permissions, section)
        return level == PermissionLevel.WRITE
    }

    fun canRead(permissions: UserPermissions, section: String): Boolean {
        val level = getSectionLevel(permissions, section)
        return level != PermissionLevel.NONE
    }

    private fun getSectionLevel(p: UserPermissions, section: String): PermissionLevel =
        when (section.lowercase()) {
            "personal" -> p.personal
            "comms"    -> p.comms
            "fuel"     -> p.fuel
            "agenda"   -> p.agenda
            "chat"     -> p.chat
            else       -> PermissionLevel.NONE
        }
}
