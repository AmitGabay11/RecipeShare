package com.example.recipeshare.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RecipeEntity::class], version = 3, exportSchema = false)
abstract class RecipeDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile
        private var INSTANCE: RecipeDatabase? = null

        fun getInstance(context: Context): RecipeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecipeDatabase::class.java, "recipe_database"
                )
                    .fallbackToDestructiveMigration() // ✅ Ensures app doesn't crash on version change
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
