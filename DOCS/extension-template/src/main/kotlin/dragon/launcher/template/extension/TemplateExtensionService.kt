package dragon.launcher.template.extension

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Extension Template for Dragon Launcher.
 * Follow this structure to create new extensions.
 */
class TemplateExtensionService : Service() {

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "org.dragon.launcher.ACTION_TEMPLATE_COMMAND" -> {
                val input = intent.getStringExtra("input_data") ?: ""
                handleCommand(input)
            }
        }
        return START_NOT_STICKY
    }

    private fun handleCommand(input: String) {
        // Core logic of your extension goes here
        
        // Return result to Dragon Launcher
        val resultIntent = Intent("org.dragon.launcher.ACTION_TEMPLATE_RESULT").apply {
            putExtra("output_data", "Processed: $input")
            `package` = "org.dragon.launcher"
        }
        startService(resultIntent)
    }
}
