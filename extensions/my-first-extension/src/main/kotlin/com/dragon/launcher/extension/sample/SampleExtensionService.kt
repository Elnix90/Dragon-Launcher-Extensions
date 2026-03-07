package com.dragon.launcher.extension.sample

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Un service d'exemple pour une extension Dragon Launcher.
 */
class SampleExtensionService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        // Retournez ici l'instance de votre Binder (AIDL implémenté)
        return null
    }

    override fun onCreate() {
        super.onCreate()
        println("Extension Service démarré.")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("Extension Service arrêté.")
    }
}
