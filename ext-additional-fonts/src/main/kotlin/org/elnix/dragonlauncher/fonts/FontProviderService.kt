package org.elnix.dragonlauncher.fonts

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import java.net.URL

/**
 * Service pour fournir des polices supplémentaires au Dragon Launcher.
 */
class FontProviderService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "font_downloads"
    private var isJobRunning = false

    companion object {
        const val ACTION_GET_FONTS = "org.elnix.dragonlauncher.ACTION_GET_FONTS"
        const val ACTION_DOWNLOAD_ALL = "org.elnix.dragonlauncher.ACTION_DOWNLOAD_ALL"
        const val ACTION_DOWNLOAD_PROGRESS = "org.elnix.dragonlauncher.ACTION_DOWNLOAD_PROGRESS"
        const val ACTION_DOWNLOAD_COMPLETE = "org.elnix.dragonlauncher.ACTION_DOWNLOAD_COMPLETE"
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("FontProvider", ">>> onStartCommand RECEIVED ACTION: ${intent?.action}")
        
        when (intent?.action) {
            ACTION_GET_FONTS -> {
                val fontName = intent.getStringExtra("FONT_NAME") ?: "Roboto"
                Log.d("FontProvider", ">>> REQUESTED FONT: $fontName")
                handleFontRequest(fontName)
            }
            ACTION_DOWNLOAD_ALL -> {
                Log.d("FontProvider", ">>> STARTING BATCH DOWNLOAD (PARALLEL)")
                createNotificationChannel()
                startForeground(NOTIFICATION_ID, createNotification("Starting download...", 0, 100))
                handleDownloadAll()
            }
            else -> {
                Log.w("FontProvider", ">>> UNKNOWN ACTION: ${intent?.action}")
            }
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Font Downloads"
            val descriptionText = "Shows progress of font downloads"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String, progress: Int, total: Int, indeterminate: Boolean = false): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Dragon Launcher Fonts")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(total, progress, indeterminate)
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification(contentText: String, progress: Int, total: Int, indeterminate: Boolean = false) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(contentText, progress, total, indeterminate))
    }

    private fun handleDownloadAll() {
        if (isJobRunning) {
            Log.w("FontProvider", ">>> DOWNLOAD ALREADY IN PROGRESS, SKIPPING")
            return
        }
        isJobRunning = true

        serviceScope.launch {
            try {
                Log.d("FontProvider", ">>> STEP 1: Opening google-fonts-cache.json from assets")
                val jsonString = try {
                    assets.open("google-fonts-cache.json").bufferedReader().use { it.readText() }
                } catch (e: Exception) {
                    Log.e("FontProvider", ">>> ERROR during STEP 1: ${e.message}")
                    null
                } ?: run {
                    isJobRunning = false
                    return@launch
                }

                Log.d("FontProvider", ">>> STEP 2: Parsing JSON and listing assets/fonts")
                val jsonObject = JSONObject(jsonString)
                val fontsArray = if (jsonObject.has("items")) jsonObject.getJSONArray("items") else jsonObject.getJSONArray("fonts")
                val assetsList = assets.list("fonts")?.toSet() ?: emptySet()

                updateNotification("Scanning fonts...", 0, 0, true)

                Log.d("FontProvider", ">>> STEP 3: Filtering fonts to download")
                val toDownload = mutableListOf<JSONObject>()
                for (i in 0 until fontsArray.length()) {
                    val font = fontsArray.getJSONObject(i)
                    val family = font.getString("family")
                    if (family.lowercase().contains("icons")) continue

                    val fileName = "${family.replace(" ", "_")}.ttf"
                    if (assetsList.contains(fileName)) continue

                    val cacheFile = File(cacheDir, "preview_$fileName")
                    if (cacheFile.exists() && cacheFile.length() > 0) continue

                    toDownload.add(font)
                }

                val total = toDownload.size
                Log.d("FontProvider", ">>> STEP 4: Filters done. $total fonts to download.")
                if (total == 0) {
                    Log.d("FontProvider", ">>> STEP 4b: No fonts to download, stopping service.")
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    isJobRunning = false
                    return@launch
                }

                var downloadedCount = 0
                var failureCount = 0
                val dispatcher = Dispatchers.IO.limitedParallelism(3)
                
                Log.d("FontProvider", ">>> STEP 5: Launching parallel downloads (max 3 workers)")
                updateNotification("Preparing $total fonts...", 0, total)

                val jobs = toDownload.map { font ->
                    val family = font.getString("family")
                    val fileName = "${family.replace(" ", "_")}.ttf"
                    val cacheFile = File(cacheDir, "preview_$fileName")
                    val url = if (font.has("files")) font.getJSONObject("files").optString("regular") else font.optString("url")
                    
                    launch(dispatcher) {
                        if (!isActive || failureCount > 10) return@launch
                        if (url.isNullOrEmpty()) {
                            Log.w("FontProvider", ">>> SKIP: No URL found for $family")
                            return@launch
                        }
                        try {
                            Log.d("FontProvider", ">>> START DOWNLOAD: $family from $url")
                            URL(url).openConnection().apply {
                                connectTimeout = 5000
                                readTimeout = 5000
                            }.getInputStream().use { input ->
                                cacheFile.outputStream().use { output ->
                                    val buffer = ByteArray(8 * 1024)
                                    var bytes = input.read(buffer)
                                    while (bytes >= 0) {
                                        output.write(buffer, 0, bytes)
                                        bytes = input.read(buffer)
                                    }
                                }
                            }
                            synchronized(this@FontProviderService) {
                                downloadedCount++
                                failureCount = 0 // Reset failures on success
                                Log.d("FontProvider", ">>> PROGRESS: $family saved ($downloadedCount/$total)")
                                sendProgressBroadcast(family, downloadedCount, total)
                                updateNotification("Downloading: $family ($downloadedCount/$total)", downloadedCount, total)
                            }
                        } catch (e: Exception) {
                            Log.e("FontProvider", ">>> ERROR DOWNLOADING $family: ${e.message}")
                            if (cacheFile.exists()) cacheFile.delete()
                            synchronized(this@FontProviderService) {
                                failureCount++
                                if (failureCount > 10) {
                                    Log.e("FontProvider", ">>> TOO MANY FAILURES ($failureCount), ABORTING BATCH")
                                    this@launch.cancel()
                                }
                            }
                        }
                    }
                }
                
                jobs.joinAll()
                Log.d("FontProvider", ">>> STEP 6: Finished all downloads ($downloadedCount/$total)")
                
                if (failureCount > 10) {
                    updateNotification("Download failed (No Internet)", downloadedCount, total)
                } else {
                    updateNotification("Download complete ($downloadedCount fonts)", total, total)
                }
                
                delay(2000)
                Log.d("FontProvider", ">>> STEP 7: Stopping foreground status and notifying launcher")
                stopForeground(STOP_FOREGROUND_REMOVE)
                isJobRunning = false
                
                val resultIntent = Intent(ACTION_DOWNLOAD_COMPLETE).apply {
                    setPackage("org.elnix.dragonlauncher")
                    putExtra("TOTAL", total)
                    putExtra("DOWNLOADED", downloadedCount)
                    putExtra("FAILED", failureCount > 10)
                }
                sendBroadcast(resultIntent)
                
            } catch (e: Exception) {
                Log.e("FontProvider", ">>> FATAL ERROR in handleDownloadAll: ${e.message}")
                stopForeground(STOP_FOREGROUND_REMOVE)
                isJobRunning = false
            }
        }
    }

    private fun sendProgressBroadcast(currentFont: String, currentCount: Int, total: Int) {
        val intent = Intent(ACTION_DOWNLOAD_PROGRESS).apply {
            setPackage("org.elnix.dragonlauncher")
            putExtra("FONT_NAME", currentFont)
            putExtra("CURRENT", currentCount)
            putExtra("TOTAL", total)
            putExtra("PROGRESS", (currentCount.toFloat() / total.toFloat() * 100).toInt())
        }
        sendBroadcast(intent)
    }

    private fun handleFontRequest(fontName: String) {
        serviceScope.launch {
            try {
                val assetFileName = "${fontName.replace(" ", "_")}.ttf"
                val assetsList = assets.list("fonts") ?: emptyArray()

                // 1. Assets
                if (assetsList.contains(assetFileName)) {
                    val cacheFile = File(cacheDir, assetFileName)
                    if (!cacheFile.exists()) {
                        assets.open("fonts/$assetFileName").use { it.copyTo(cacheFile.outputStream()) }
                    }
                    val category = findCategoryInJson(fontName)
                    sendFontResult(fontName, cacheFile, category)
                    return@launch
                }

                // 2. Cache
                val cachedFile = File(cacheDir, "preview_$assetFileName")
                if (cachedFile.exists() && cachedFile.length() > 0) {
                    val category = findCategoryInJson(fontName)
                    sendFontResult(fontName, cachedFile, category)
                    return@launch
                }

                // 3. Download on demand
                val jsonString = try {
                    assets.open("google-fonts-cache.json").bufferedReader().use { it.readText() }
                } catch (e: Exception) { null }

                if (jsonString != null) {
                    val jsonObject = JSONObject(jsonString)
                    val fontsArray = if (jsonObject.has("items")) jsonObject.getJSONArray("items") else jsonObject.getJSONArray("fonts")
                    
                    for (i in 0 until fontsArray.length()) {
                        val font = fontsArray.getJSONObject(i)
                        if (font.getString("family").equals(fontName, ignoreCase = true)) {
                            val url = if (font.has("files")) font.getJSONObject("files").optString("regular") else font.optString("url")
                            val category = font.optString("category", "unknown")
                            
                            if (url.isNotEmpty()) {
                                try {
                                    URL(url).openStream().use { input ->
                                        cachedFile.outputStream().use { output -> input.copyTo(output) }
                                    }
                                    sendFontResult(fontName, cachedFile, category)
                                    return@launch
                                } catch (e: Exception) {
                                    Log.e("FontProvider", "Download failed for $fontName", e)
                                    if (cachedFile.exists()) cachedFile.delete()
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FontProvider", "Error in handleFontRequest: ${e.message}")
            }
        }
    }

    private fun findCategoryInJson(fontName: String): String {
        return try {
            val jsonString = assets.open("google-fonts-cache.json").bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            val fontsArray = if (jsonObject.has("items")) jsonObject.getJSONArray("items") else jsonObject.getJSONArray("fonts")
            for (i in 0 until fontsArray.length()) {
                val font = fontsArray.getJSONObject(i)
                if (font.getString("family").equals(fontName, ignoreCase = true)) {
                    return font.optString("category", "unknown")
                }
            }
            "unknown"
        } catch (e: Exception) { "unknown" }
    }

    private fun sendFontResult(fontName: String, fontFile: File, category: String) {
        val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", fontFile)
        val intent = Intent("org.elnix.dragonlauncher.ACTION_FONTS_RESULT").apply {
            putExtra("FONT_NAME", fontName)
            putExtra("FONT_PATH", uri.toString())
            putExtra("FONT_CATEGORY", category)
            setPackage("org.elnix.dragonlauncher")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        grantUriPermission("org.elnix.dragonlauncher", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        sendBroadcast(intent)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
