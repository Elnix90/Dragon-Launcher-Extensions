package org.elnix.dragonlauncher.weather

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WeatherExtensionService : Service() {
    private val weatherProvider = WeatherProvider()
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val CHANNEL_ID = "weather_alerts"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == "org.elnix.dragonlauncher.ACTION_WEATHER_UPDATE") {
            updateWeather()
        }
        return START_NOT_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun updateWeather() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location: Location? ->
            if (location != null) {
                fetchAndReportWeather(location.latitude, location.longitude)
            }
        }
    }

    private fun fetchAndReportWeather(lat: Double, lon: Double) {
        val prefs = getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
        val useFahrenheit = prefs.getString("temp_unit", "celsius") == "fahrenheit"
        val rainAlertsEnabled = prefs.getBoolean("rain_alerts_enabled", true)

        serviceScope.launch {
            val forecast = weatherProvider.fetchFullWeather(lat, lon, useFahrenheit)
            if (forecast != null) {
                // 1. Send detailed data to launcher
                val intent = Intent("org.elnix.dragonlauncher.ACTION_WEATHER_RESULT").apply {
                    putExtra("full_forecast", Json.encodeToString(forecast))
                    putExtra("temp", forecast.current.temperature)
                    putExtra("temp_unit", if (useFahrenheit) "°F" else "°C")
                    putExtra("code", forecast.current.weatherCode)
                    putExtra("is_day", forecast.current.isDay)
                    `package` = "org.elnix.dragonlauncher"
                }
                sendBroadcast(intent)

                // 2. Handle Rain Notification
                if (rainAlertsEnabled && forecast.rainAlert) {
                    showRainNotification()
                }
            }
        }
    }

    private fun showRainNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_report_image)
            .setContentTitle("Alerte Pluie")
            .setContentText("Risque de pluie dans l'heure à venir !")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Weather Alerts"
            val descriptionText = "Notifications for important weather changes"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
