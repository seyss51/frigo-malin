package com.julien.frigomalin.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

object PhotoStorage {
    private const val DOSSIER = "recette_photos"

    fun copierDansStockageInterne(context: Context, uri: Uri): String? {
        return try {
            val dossier = File(context.filesDir, DOSSIER).apply { mkdirs() }
            val nomFichier = "${UUID.randomUUID()}.jpg"
            val destination = File(dossier, nomFichier)
            context.contentResolver.openInputStream(uri)?.use { entree ->
                destination.outputStream().use { sortie -> entree.copyTo(sortie) }
            }
            nomFichier
        } catch (e: Exception) {
            null
        }
    }

    fun fichierPhoto(context: Context, nomFichier: String?): File? {
        if (nomFichier == null) return null
        val fichier = File(File(context.filesDir, DOSSIER), nomFichier)
        return if (fichier.exists()) fichier else null
    }
}