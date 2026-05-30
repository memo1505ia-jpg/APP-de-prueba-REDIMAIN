package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "officers")
data class OfficerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val cedula: String,
    val phone: String,
    val rankCargo: String,
    val photoUri: String? = null, // Preloaded avatar key or custom user photo
    val status: String,           // Custom status like "Activo", "Permiso especial", etc.
    val statusFromDate: String,   // "YYYY-MM-DD" style
    val statusToDate: String,     // "YYYY-MM-DD" style
    val hasAlarm: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    
    // 2. Base de datos y categorías de Tripulación (DIRECTRICES)
    val category: String = "OFICIALES", // Options: OFICIALES, OFICIALES TÉCNICOS, OFICIAL DE TROPA, OFICIAL ASIMILADO, TROPA PROFESIONAL, TROPA ALISTADA
    
    // 3. Componente de Perfil Completo (Expediente)
    val direccionHabitacion: String = "",
    val direccionResidencia: String = "",
    val conyuge: String = "",
    val padres: String = "",
    val hijosJson: String = "[]", // Serialized JSON of children e.g. [{"sexo":"Masc","edad":10}]
    val grupoSanguineo: String = "",
    val alergias: String = "",
    val contactoEmergencia: String = "",
    val tallaCamisa: String = "",
    val tallaGuerrera: String = "",
    val tallaBotas: String = "",
    val tallaGorra: String = "",
    val tallaQuepi: String = "",
    val tallaZapatos: String = "",
    val tallaPantalon: String = ""
)
