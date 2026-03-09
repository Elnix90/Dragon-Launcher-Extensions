package org.elnix.dragonlauncher.fonts

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
        Log.d("FontProvider", "onStartCommand: action=${intent?.action}")
        if (intent?.action == "org.elnix.dragonlauncher.ACTION_GET_FONTS") {
            val fontName = intent.getStringExtra("FONT_NAME") ?: "Roboto"
            Log.d("FontProvider", "Fetching font: $fontName")
            downloadFontFromCachedList(fontName)
        }
        return START_NOT_STICKY
    }

    private fun downloadFontFromCachedList(fontName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("FontProvider", "Starting download check for $fontName")
                // 1. Essayer de charger le fichier JSON local (pré-embarqué)
                var jsonString: String? = try {
                    assets.open("google-fonts-cache.json").bufferedReader().use { it.readText() }
                } catch (e: Exception) {
                    Log.e("FontProvider", "Assets load failed: ${e.message}")
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
                    val jsonObject = JSONObject(jsonString)
                    val fontsArray = jsonObject.getJSONArray("fonts")
                    var downloadUrl: String? = null
                    
                    for (i in 0 until fontsArray.length()) {
                        val font = fontsArray.getJSONObject(i)
                        if (font.getString("family").equals(fontName, ignoreCase = true)) {
                            downloadUrl = font.getString("url")
                            break
                        }
                    }

                    if (downloadUrl != null) {
                        Log.d("FontProvider", "Downloading from: $downloadUrl")
                        val fontFile = File(cacheDir, "${fontName.replace(" ", "_")}.ttf")
                        URL(downloadUrl).openStream().use { input ->
                            fontFile.outputStream().use { output -> input.copyTo(output) }
                        }
                        
                        Log.d("FontProvider", "Font saved to: ${fontFile.absolutePath}")
                        val responseIntent = Intent("org.elnix.dragonlauncher.ACTION_FONTS_RESULT").apply {
                            putExtra("FONT_PATH", fontFile.absolutePath)
                            putExtra("FONT_NAME", fontName)
                            `package` = "org.elnix.dragonlauncher"
                        }
                        sendBroadcast(responseIntent)
                        Log.d("FontProvider", "Broadcast sent to org.elnix.dragonlauncher")
                    } else {
                        Log.w("FontProvider", "Font $fontName not found in registry")
                    }
                }
            } catch (e: Exception) {
                Log.e("FontProvider", "Erreur globale : ${e.message}", e)
            }
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}

