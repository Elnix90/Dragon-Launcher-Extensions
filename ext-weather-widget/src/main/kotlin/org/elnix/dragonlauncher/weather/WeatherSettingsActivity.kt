package org.elnix.dragonlauncher.weather

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class WeatherSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
        val rainAlertCheck = findViewById<CheckBox>(R.id.check_rain_alert)
        val unitsGroup = findViewById<RadioGroup>(R.id.group_units)
        val saveButton = findViewById<Button>(R.id.btn_save)

        // Load current state
        rainAlertCheck.isChecked = prefs.getBoolean("rain_alerts_enabled", true)
        if (prefs.getString("temp_unit", "celsius") == "fahrenheit") {
            findViewById<RadioButton>(R.id.radio_fahrenheit).isChecked = true
        }

        saveButton.setOnClickListener {
            // Save preferences
            prefs.edit()
                .putBoolean("rain_alerts_enabled", rainAlertCheck.isChecked)
                .putString("temp_unit", if (unitsGroup.checkedRadioButtonId == R.id.radio_celsius) "celsius" else "fahrenheit")
                .apply()

            // Request permissions as before
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }

            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                1001
            )
            
            Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
