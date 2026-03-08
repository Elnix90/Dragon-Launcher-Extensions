package dragon.launcher.update.extension

import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.net.URL
import org.json.JSONObject

/**
 * Extension gérant la mise à jour automatique.
 * Vérifie les dernières versions (Tags/Releases) sur GitHub pour le lanceur et ses extensions.
 */
class UpdateManagerService : Service() {

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "org.dragon.launcher.ACTION_CHECK_UPDATES" -> {
                val repo = intent.getStringExtra("github_repo") ?: "Elnix90/Dragon-Launcher"
                checkForUpdates(repo)
            }
        }
        return START_NOT_STICKY
    }

    private fun checkForUpdates(repoPath: String) {
        Thread {
            try {
                // Appel API GitHub pour récupérer la dernière release
                val url = URL("https://api.github.com/repos/$repoPath/releases/latest")
                val connection = url.openConnection()
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                
                val response = connection.getInputStream().bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                
                val latestVersion = json.getString("tag_name")
                val downloadUrl = json.getJSONArray("assets").getJSONObject(0).getString("browser_download_url")

                notifyMainApp(repoPath, latestVersion, downloadUrl)
            } catch (e: Exception) {
                notifyError(repoPath, e.message ?: "Unknown error")
            }
        }.start()
    }

    private fun notifyMainApp(repo: String, version: String, url: String) {
        val intent = Intent("org.dragon.launcher.ACTION_UPDATE_AVAILABLE").apply {
            putExtra("repo", repo)
            putExtra("version", version)
            putExtra("download_url", url)
            `package` = "org.dragon.launcher"
        }
        startService(intent)
    }

    private fun notifyError(repo: String, error: String) {
        val intent = Intent("org.dragon.launcher.ACTION_UPDATE_ERROR").apply {
            putExtra("repo", repo)
            putExtra("error", error)
            `package` = "org.dragon.launcher"
        }
        startService(intent)
    }
}
