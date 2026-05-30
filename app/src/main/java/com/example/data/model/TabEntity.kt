package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabs")
data class TabEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val columns: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)
