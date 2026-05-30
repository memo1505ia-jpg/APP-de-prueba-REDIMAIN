package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comm_traffic")
data class CommTrafficEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val documentNumber: String,
    val subject: String,
    val origin: String,
    val recipientRankName: String,
    val dateReceived: String,
    val deadlineDate: String,
    val imagePath: String? = null,
    val hasDeadlineAlarm: Boolean = false,
    val status: String = "Pendiente", // "Pendiente", "Ejecutando", "Cumplido", "Expirado"
    val timestamp: Long = System.currentTimeMillis()
)
