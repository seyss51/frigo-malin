package com.julien.frigomalin.data

import android.content.Context
import androidx.room.Room

/**
 * Fournit l'instance singleton de la base de données.
 * Séparé de AppDatabase.kt : une classe annotée @Database contenant
 * elle-même du code exécutable (companion object avec logique) fait
 * échouer la résolution de types de KSP (erreur MissingType trompeuse).
 */
object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "frigo_malin_database"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}