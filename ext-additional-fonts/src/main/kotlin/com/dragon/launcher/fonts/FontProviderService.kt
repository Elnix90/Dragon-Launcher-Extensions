package com.dragon.launcher.fonts

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import java.net.URL

/**
 * Service pour fournir des polices supplémentaires au Dragon Launcher.
 * 
 * Sécurité & Offline : La liste des polices est pré-générée mensuellement 
 * par GitHub Actions (google-fonts-cache.json) pour éviter l'utilisation
 * d'une clé API en local sur le téléphone.
 */
class FontProviderService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "com.dragon.launcher.ACTION_GET_FONTS") {
            val fontName = intent.getStringExtra("FONT_NAME") ?: "Roboto"
            downloadFontFromCachedList(fontName)
        }
        return START_NOT_STICKY
    }

    private fun downloadFontFromCachedList(fontName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Essayer de charger le fichier JSON local (pré-embarqué)
                var jsonString: String? = try {
                    assets.open("google-fonts-cache.json").bufferedReader().use { it.readText() }
                } catch (e: Exception) {
                    null
                }

                // 2. Fallback : Télécharger la version la plus récente depuis GitHub
                if (jsonString == null) {
                    try {
                        val githubUrl = "https://raw.githubusercontent.com/Elnix90/Dragon-Launcher-Extensions/main/ext-additional-fonts/google-fonts-cache.json"
                        jsonString = URL(githubUrl).readText()
                        Log.d("FontProvider", "Liste récupérée depuis GitHub (Fallback)")
                    } catch (e: Exception) {
                        Log.e("FontProvider", "Impossible de récupérer la liste JSON : ${e.message}")
                    }
                }

                if (jsonString != null) {
                    val fontsArray = JSONObject(jsonString).getJSONArray("fonts")
                    var downloadUrl: String? = null
                    
                    for (i in 0 until fontsArray.length()) {
                        val font = fontsArray.getJSONObject(i)
                        if (font.getString("family").equals(fontName, ignoreCase = true)) {
                            downloadUrl = font.getString("url")
                            break
                        }
                    }

                    if (downloadUrl != null) {
                        val fontFile = File(cacheDir, "$fontName.ttf")
                        URL(downloadUrl).openStream().use { input ->
                            fontFile.outputStream().use { output -> input.copyTo(output) }
                        }
                        
                        val responseIntent = Intent("com.dragon.launcher.FONTS_UPDATED").apply {
                            putExtra("FONT_PATH", fontFile.absolutePath)
                            putExtra("FONT_NAME", fontName)
                        }
                        sendBroadcast(responseIntent)
                    }
                }
            } catch (e: Exception) {
                Log.e("FontProvider", "Erreur : ${e.message}")
            }
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}

