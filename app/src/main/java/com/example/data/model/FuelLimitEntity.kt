package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_limits")
data class FuelLimitEntity(
    @PrimaryKey val plateOrPerson: String, // Can be the license plate or the person name
    val weeklyLimitLiters: Double
)
