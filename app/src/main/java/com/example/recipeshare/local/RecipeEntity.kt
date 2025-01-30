package com.example.recipeshare.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String = "",  // 🔄 Change from Int to String
    val title: String,
    val description: String,
    val imageUrl: String,
    val createdAt: Long,
    val userId: String  // ✅ Store userId here
)
