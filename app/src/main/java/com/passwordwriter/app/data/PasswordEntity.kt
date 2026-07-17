package com.passwordwriter.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "passwords")
data class PasswordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String = "General",
    val name: String,
    val username: String = "",
    val encryptedPassword: String,
    val notes: String = "",
    val categoryColor: Int = 0,
    val categoryIcon: String = "other",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
