package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rows")
data class RowEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tabId: Int,
    val cells: List<String>,      // Values matching the columns in the tab
    val isConfidential: Boolean = false, // Confidentiality indicator for maritime-military security
    val timestamp: Long = System.currentTimeMillis()
)
