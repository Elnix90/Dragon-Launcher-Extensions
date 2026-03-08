package dragon.launcher.update.extension

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.net.URL
import org.json.JSONArray
import org.json.JSONObject

/**
 * Extension gérant la mise à jour automatique.
 * Vérifie le registre des extensions et met à jour celles installées.
 */
class UpdateManagerService : Service() {

    companion object {
        private const val REGISTRY_URL = "https://raw.githubusercontent.com/Elnix90/Dragon-Launcher-Extensions/main/extensions-registry.json"
        private const val TAG = "UpdateManager"
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "org.dragon.launcher.ACTION_CHECK_UPDATES" -> {
                checkExtensionsRegistry()
            }
        }
        return START_NOT_STICKY
    }

    private fun checkExtensionsRegistry() {
        Thread {
            try {
                val response = URL(REGISTRY_URL).readText()
                val registry = JSONArray(response)
                
                for (i in 0 until registry.length()) {
                    val ext = registry.getJSONObject(i)
                    val packageName = ext.getString("package")
                    val latestVersionCode = ext.getInt("versionCode")
                    val downloadUrl = ext.getString("download_url")

                    if (isUpdateNeeded(packageName, latestVersionCode)) {
                        Log.d(TAG, "Update needed for $packageName: $latestVersionCode")
                        downloadAndInstall(packageName, downloadUrl)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking registry", e)
                notifyError("registry", e.message ?: "Unknown error")
            }
        }.start()
    }

    private fun isUpdateNeeded(packageName: String, latestVersionCode: Int): Boolean {
        return try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            val currentVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode
            }
            latestVersionCode > currentVersionCode
        } catch (e: PackageManager.NameNotFoundException) {
            // Pas installée, on peut choisir de l'installer ou non. 
            // Ici on ignore si pas déjà présente pour éviter de forcer toutes les extensions.
            false
        }
    }

    private fun downloadAndInstall(packageName: String, url: String) {
        try {
            val destination = File(cacheDir, "$packageName.apk")
            URL(url).openStream().use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            installApk(packageName, destination)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download $packageName", e)
        }
    }

    private fun installApk(packageName: String, file: File) {
        if (isShizukuAvailable()) {
            installViaShizuku(packageName, file)
        } else {
            installStandard(file)
        }
    }

    private fun isShizukuAvailable(): Boolean {
        return try {
            // On vérifie si l'extension Shizuku est là et répond
            packageManager.getPackageInfo("org.dragon.launcher.shizukustaller", 0)
            // Note: En conditions réelles, on utiliserait Shizuku.pingBinder()
            true 
        } catch (e: Exception) {
            false
        }
    }

    private fun installViaShizuku(packageName: String, file: File) {
        val intent = Intent("org.dragon.launcher.ACTION_INSTALL_APK").apply {
            putExtra("apk_path", file.absolutePath)
            putExtra("package_name", packageName)
            `package` = "org.dragon.launcher.shizukustaller"
        }
        startService(intent)
    }

    private fun installStandard(file: File) {
        val uri = FileProvider.getUriForFile(this, "org.dragon.launcher.autoupdate.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
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
