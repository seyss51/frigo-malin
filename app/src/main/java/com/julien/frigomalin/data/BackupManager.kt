package com.julien.frigomalin.data

import android.content.Context
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupManager {

    private const val DOSSIER_PHOTOS = "recette_photos"
    private val COLLECTIONS = listOf("ingredients", "recettes", "planning")

    suspend fun exporterSauvegarde(context: Context): File {
        val db = FirebaseFirestore.getInstance()
        val dossierExport = File(context.cacheDir, "export").apply { mkdirs() }
        val fichierZip = File(dossierExport, "frigo_malin_sauvegarde.zip")

        ZipOutputStream(FileOutputStream(fichierZip)).use { zip ->
            for (nomCollection in COLLECTIONS) {
                val snapshot = db.collection(nomCollection).get().await()
                val tableau = JSONArray()
                for (doc in snapshot.documents) {
                    val objet = JSONObject(doc.data ?: emptyMap<String, Any>())
                    objet.put("__id", doc.id)
                    tableau.put(objet)
                }
                ajouterTexteAuZip(zip, "db/$nomCollection.json", tableau.toString())
            }

            val dossierPhotos = File(context.filesDir, DOSSIER_PHOTOS)
            if (dossierPhotos.exists()) {
                dossierPhotos.listFiles()?.forEach { photo ->
                    ajouterFichierAuZip(zip, photo, "$DOSSIER_PHOTOS/${photo.name}")
                }
            }
        }

        return fichierZip
    }

    suspend fun importerSauvegarde(context: Context, source: Uri): Boolean {
        return try {
            val db = FirebaseFirestore.getInstance()
            val dossierPhotos = File(context.filesDir, DOSSIER_PHOTOS).apply { mkdirs() }
            val jsonParCollection = mutableMapOf<String, String>()

            context.contentResolver.openInputStream(source)?.use { entree ->
                ZipInputStream(entree).use { zip ->
                    var entry: ZipEntry? = zip.nextEntry
                    while (entry != null) {
                        val nom = entry.name
                        when {
                            nom.startsWith("db/") && nom.endsWith(".json") -> {
                                val nomCollection = nom.removePrefix("db/").removeSuffix(".json")
                                jsonParCollection[nomCollection] = zip.readBytes().toString(Charsets.UTF_8)
                            }
                            nom.startsWith("$DOSSIER_PHOTOS/") && !entry.isDirectory -> {
                                val destination = File(dossierPhotos, nom.removePrefix("$DOSSIER_PHOTOS/"))
                                destination.parentFile?.mkdirs()
                                FileOutputStream(destination).use { sortie -> zip.copyTo(sortie) }
                            }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            }

            for ((nomCollection, contenuJson) in jsonParCollection) {
                val tableau = JSONArray(contenuJson)
                val collectionRef = db.collection(nomCollection)

                // Supprime les documents existants avant de restaurer
                val existant = collectionRef.get().await()
                for (doc in existant.documents) {
                    collectionRef.document(doc.id).delete().await()
                }

                for (i in 0 until tableau.length()) {
                    val objet = tableau.getJSONObject(i)
                    val id = objet.optString("__id")
                    objet.remove("__id")
                    val donnees = jsonVersMap(objet)
                    collectionRef.document(id).set(donnees).await()
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun ajouterTexteAuZip(zip: ZipOutputStream, nomDansZip: String, contenu: String) {
        zip.putNextEntry(ZipEntry(nomDansZip))
        zip.write(contenu.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun ajouterFichierAuZip(zip: ZipOutputStream, fichier: File, nomDansZip: String) {
        zip.putNextEntry(ZipEntry(nomDansZip))
        FileInputStream(fichier).use { it.copyTo(zip) }
        zip.closeEntry()
    }

    private fun jsonVersMap(objet: JSONObject): Map<String, Any?> {
        val resultat = mutableMapOf<String, Any?>()
        val cles = objet.keys()
        while (cles.hasNext()) {
            val cle = cles.next()
            resultat[cle] = jsonVersValeur(objet.get(cle))
        }
        return resultat
    }

    private fun jsonVersValeur(valeur: Any?): Any? = when (valeur) {
        is JSONObject -> jsonVersMap(valeur)
        is JSONArray -> (0 until valeur.length()).map { jsonVersValeur(valeur.get(it)) }
        JSONObject.NULL -> null
        else -> valeur
    }
}