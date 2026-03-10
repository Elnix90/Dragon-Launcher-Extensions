package org.elnix.dragonlauncher.fonts

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

class FontListActivity : ComponentActivity() {

    private val viewModel: FontViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    FontListScreen(
                        viewModel = viewModel,
                        onFontClick = { font ->
                            downloadFont(font)
                        }
                    )
                }
            }
        }
    }

    private fun downloadFont(font: FontItem) {
        Toast.makeText(this, "Downloading ${font.family}...", Toast.LENGTH_SHORT).show()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val fileName = "${font.family.replace(" ", "_")}.ttf"
                val file = File(downloadsDir, fileName)

                URL(font.url).openStream().use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FontListActivity, "Saved to Downloads: $fileName", Toast.LENGTH_LONG).show()
                }

                // Also notify the service if needed
                val intent = Intent(this@FontListActivity, FontProviderService::class.java).apply {
                    action = "org.elnix.dragonlauncher.ACTION_GET_FONTS"
                    putExtra("FONT_NAME", font.family)
                }
                startService(intent)
            } catch (e: Exception) {
                Log.e("FontListActivity", "Download failed", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FontListActivity, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
