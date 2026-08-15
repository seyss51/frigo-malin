package com.julien.frigomalin.util

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

object CrashHandler {

    private const val NOM_FICHIER = "dernier_crash.txt"

    fun installer(context: Context) {
        val gestionnaireParDefaut = Thread.getDefaultUncaughtExceptionHandler()
        val appContext = context.applicationContext

        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            try {
                val ecrivain = StringWriter()
                exception.printStackTrace(PrintWriter(ecrivain))
                File(appContext.filesDir, NOM_FICHIER).writeText(ecrivain.toString())
            } catch (e: Exception) {
                // rien à faire si même l'écriture du crash échoue
            }
            gestionnaireParDefaut?.uncaughtException(thread, exception)
                ?: exitProcess(1)
        }
    }

    fun lireDernierCrash(context: Context): String? {
        val fichier = File(context.filesDir, NOM_FICHIER)
        return if (fichier.exists()) fichier.readText() else null
    }

    fun effacerDernierCrash(context: Context) {
        File(context.filesDir, NOM_FICHIER).delete()
    }
}