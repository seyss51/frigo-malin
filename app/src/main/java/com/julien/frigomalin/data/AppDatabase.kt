package com.julien.frigomalin.data

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromTypeRepas(value: TypeRepas): String = value.name

    @TypeConverter
    fun toTypeRepas(value: String): TypeRepas = TypeRepas.valueOf(value)
}

@Database(
    entities = [
        Ingredient::class,
        Recette::class,
        RecetteIngredient::class,
        PlanningJour::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun recetteDao(): RecetteDao
    abstract fun planningDao(): PlanningDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "frigo_malin_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}