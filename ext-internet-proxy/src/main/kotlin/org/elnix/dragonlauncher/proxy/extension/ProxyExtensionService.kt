package org.elnix.dragonlauncher.proxy.extension

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Service Proxy Internet pour Dragon Launcher.
 * Permet à l'application principale (sans permission INTERNET) de faire des requêtes via cette extension.
 */
class ProxyExtensionService : Service() {

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "org.elnix.dragonlauncher.ACTION_PROXY_FETCH" -> {
                val url = intent.getStringExtra("url") ?: ""
                val requestId = intent.getStringExtra("request_id") ?: ""
                performNetworkRequest(url, requestId)
            }
        }
        return START_NOT_STICKY
    }

    private fun performNetworkRequest(url: String, requestId: String) {
        Thread {
            try {
                // Simule une requête réseau (grâce à la permission INTERNET de l'extension)
                val responseData = "Contenu de $url récupéré via l'extension proxy"
                
                sendResponseToMainApp(requestId, responseData)
            } catch (e: Exception) {
                sendResponseToMainApp(requestId, "Erreur: ${e.message}")
            }
        }.start()
    }

    private fun sendResponseToMainApp(requestId: String, data: String) {
        val responseIntent = Intent("org.elnix.dragonlauncher.ACTION_PROXY_RESPONSE").apply {
            putExtra("request_id", requestId)
            putExtra("payload", data)
            `package` = "org.elnix.dragonlauncher"
        }
        startService(responseIntent)
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
