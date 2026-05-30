package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "status_options")
data class StatusOptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)
