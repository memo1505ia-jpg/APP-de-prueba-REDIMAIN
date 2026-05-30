package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_transactions")
data class FuelTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val personName: String,
    val vehicleName: String,
    val plate: String,
    val liters: Double,
    val fuelType: String, // "Gasolina" or "Diésel"
    val dateString: String, // "YYYY-MM-DD" style
    val timestamp: Long = System.currentTimeMillis()
)
