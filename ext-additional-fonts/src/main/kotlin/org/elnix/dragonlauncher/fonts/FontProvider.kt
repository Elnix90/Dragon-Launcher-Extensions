package org.elnix.dragonlauncher.fonts

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.content.Intent
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.io.InputStreamReader

class FontProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    private fun isWifiConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val context = context ?: return null
        val cursor = MatrixCursor(arrayOf("name", "uri", "is_asset", "category"))

        try {
            // Lecture du cache JSON
            val assetManager = context.assets
            val inputStream = assetManager.open("google-fonts-cache.json")
            val jsonContent = InputStreamReader(inputStream).readText()
            val jsonObject = JSONObject(jsonContent)
            val fontsArray = jsonObject.getJSONArray("fonts")

            // Liste des fichiers TTF déjà présents en assets
            val assetsFonts = assetManager.list("fonts")?.toSet() ?: emptySet()

            for (i in 0 until fontsArray.length()) {
                val f = fontsArray.getJSONObject(i)
                val family = f.getString("family")
                val category = f.optString("category", "unknown")
                val fileName = "${family.replace(" ", "_")}.ttf"
                
                // On prépare le curseur avec les infos, mais pas forcément l'URI tout de suite
                // si la police n'est pas encore téléchargée/présente en assets
                val isAsset = assetsFonts.contains(fileName)
                var fontUri: String? = null

                if (isAsset) {
                    val cacheFile = File(context.cacheDir, "fonts/$fileName")
                    if (!cacheFile.exists()) {
                        cacheFile.parentFile?.mkdirs()
                        context.assets.open("fonts/$fileName").use { input ->
                            cacheFile.outputStream().use { output -> input.copyTo(output) }
                        }
                    }
                    fontUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", cacheFile).toString()
                } else {
                    val cacheFile = File(context.cacheDir, "preview_$fileName")
                    if (cacheFile.exists()) {
                        fontUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", cacheFile).toString()
                    }
                }

                cursor.addRow(arrayOf(family, fontUri ?: "", if (isAsset) 1 else 0, category))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return cursor
    }

    // Le Launcher appelle insert pour "demander" le traitement d'une police
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val context = context ?: return null
        val family = values?.getAsString("family") ?: return null
        
        // Simuler le traitement (ici on cherche le fichier correspondant)
        val fileName = "${family.replace(" ", "_")}.ttf"
        
        // Vérifier d'abord dans les assets
        val assetsFonts = context.assets.list("fonts")?.toSet() ?: emptySet()
        val isAsset = assetsFonts.contains(fileName)
        
        var targetFile: File? = null
        if (isAsset) {
            val cacheFile = File(context.cacheDir, "fonts/$fileName")
            if (!cacheFile.exists()) {
                cacheFile.parentFile?.mkdirs()
                context.assets.open("fonts/$fileName").use { input ->
                    cacheFile.outputStream().use { output -> input.copyTo(output) }
                }
            }
            targetFile = cacheFile
        } else {
            val previewFile = File(context.cacheDir, "preview_$fileName")
            if (previewFile.exists()) {
                targetFile = previewFile
            }
        }

        if (targetFile != null && targetFile.exists()) {
            val fontUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", targetFile)
            
            // Accorder l'accès au Launcher explicitement (Optionnel si FileProvider.grant est géré par l'Intent)
            context.grantUriPermission("org.elnix.dragonlauncher", fontUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

            // Envoyer le broadcast de résultat au Launcher
            val intent = Intent("org.elnix.dragonlauncher.ACTION_FONTS_RESULT").apply {
                putExtra("FONT_NAME", family)
                putExtra("FONT_URI", fontUri.toString())
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                // Pour Android 11+
                setPackage("org.elnix.dragonlauncher")
            }
            context.sendBroadcast(intent)
            
            return Uri.parse("content://org.elnix.dragonlauncher.fonts.provider/status/$family")
        }
        
        return null
    }

    override fun getType(uri: Uri): String? = "vnd.android.cursor.dir/vnd.elnix.font"
    override fun update(uri: Uri, values: ContentValues?, sel: String?, args: Array<out String>?) = 0
    override fun delete(uri: Uri, sel: String?, args: Array<out String>?) = 0
}
