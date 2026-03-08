package dragon.launcher.shizuku.extension

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

/**
 * Extension for installing APKs (extensions or other apps) via Shizuku (ADB).
 * This service handles installation requests from Dragon Launcher.
 */
class ShizukuInstallerService : Service() {

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "org.dragon.launcher.ACTION_INSTALL_APK" -> {
                val apkPath = intent.getStringExtra("apk_path") ?: ""
                val packageName = intent.getStringExtra("package_name") ?: ""
                installApkViaShizuku(apkPath, packageName)
            }
        }
        return START_NOT_STICKY
    }

    private fun installApkViaShizuku(apkPath: String, packageName: String) {
        Thread {
            try {
                // Warning: This is a conceptual implementation. 
                // Actual Shizuku integration requires the rikka.shizuku library.
                // We use 'sh' via Shizuku's privileged shell to run 'pm install'.
                
                Log.d("ShizukuInstaller", "Starting installation of $packageName from $apkPath")
                
                // Example of command that would be run through Shizuku:
                // "/usr/bin/pm install -r $apkPath"
                
                val success = simulateShizukuInstallation(apkPath)
                
                notifyResult(packageName, success)
            } catch (e: Exception) {
                notifyError(packageName, e.message ?: "Installation failed")
            }
        }.start()
    }

    private fun simulateShizukuInstallation(path: String): Boolean {
        // Here you would check Shizuku.checkSelfPermission() and use Shizuku.newProcess()
        // to execute "pm install -r $path"
        Thread.sleep(2000) // Simulating install time
        return true
    }

    private fun notifyResult(packageName: String, success: Boolean) {
        val intent = Intent("org.dragon.launcher.ACTION_INSTALL_RESULT").apply {
            putExtra("package_name", packageName)
            putExtra("success", success)
            `package` = "org.dragon.launcher"
        }
        startService(intent)
    }

    private fun notifyError(packageName: String, error: String) {
        val intent = Intent("org.dragon.launcher.ACTION_INSTALL_ERROR").apply {
            putExtra("package_name", packageName)
            putExtra("error", error)
            `package` = "org.dragon.launcher"
        }
        startService(intent)
    }
}
