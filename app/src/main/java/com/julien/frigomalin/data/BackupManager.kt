package com.julien.frigomalin.data

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupManager {

    private const val NOM_DB = "frigo_malin_database"
    private const val DOSSIER_PHOTOS = "recette_photos"

    fun exporterSauvegarde(context: Context): File {
        DatabaseProvider.fermerBaseDeDonnees()

        val dossierExport = File(context.cacheDir, "export").apply { mkdirs() }
        val fichierZip = File(dossierExport, "frigo_malin_sauvegarde.zip")

        ZipOutputStream(FileOutputStream(fichierZip)).use { zip ->
            val dossierDb = context.getDatabasePath(NOM_DB).parentFile
            listOf(NOM_DB, "$NOM_DB-wal", "$NOM_DB-shm").forEach { nomFichier ->
                val fichier = File(dossierDb, nomFichier)
                if (fichier.exists()) {
                    ajouterFichierAuZip(zip, fichier, "db/$nomFichier")
                }
            }

            val dossierPhotos = File(context.filesDir, DOSSIER_PHOTOS)
            if (dossierPhotos.exists()) {
                dossierPhotos.listFiles()?.forEach { photo ->
                    ajouterFichierAuZip(zip, photo, "$DOSSIER_PHOTOS/${photo.name}")
                }
            }
        }

        // Rouvre immédiatement la base pour que l'app reste utilisable
        DatabaseProvider.getDatabase(context)
        return fichierZip
    }

    fun importerSauvegarde(context: Context, source: Uri): Boolean {
        return try {
            DatabaseProvider.fermerBaseDeDonnees()

            val dossierDb = context.getDatabasePath(NOM_DB).parentFile!!
            val dossierPhotos = File(context.filesDir, DOSSIER_PHOTOS).apply { mkdirs() }

            context.contentResolver.openInputStream(source)?.use { entree ->
                ZipInputStream(entree).use { zip ->
                    var entry: ZipEntry? = zip.nextEntry
                    while (entry != null) {
                        val nom = entry.name
                        val destination = when {
                            nom.startsWith("db/") -> File(dossierDb, nom.removePrefix("db/"))
                            nom.startsWith("$DOSSIER_PHOTOS/") -> File(dossierPhotos, nom.removePrefix("$DOSSIER_PHOTOS/"))
                            else -> null
                        }
                        if (destination != null && !entry.isDirectory) {
                            destination.parentFile?.mkdirs()
                            FileOutputStream(destination).use { sortie -> zip.copyTo(sortie) }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun ajouterFichierAuZip(zip: ZipOutputStream, fichier: File, nomDansZip: String) {
        zip.putNextEntry(ZipEntry(nomDansZip))
        FileInputStream(fichier).use { it.copyTo(zip) }
        zip.closeEntry()
    }
}