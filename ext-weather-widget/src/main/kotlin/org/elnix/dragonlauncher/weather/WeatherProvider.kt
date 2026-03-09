package org.elnix.dragonlauncher.weather

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

@Serializable
data class WeatherForecast(
    val current: CurrentWeather,
    val hourly: HourlyForecast,
    val daily: DailyForecast,
    val rainAlert: Boolean = false
)

@Serializable
data class CurrentWeather(
    val temperature: Double,
    val weatherCode: Int,
    val isDay: Boolean,
    val relativeHumidity: Int,
    val windSpeed: Double
)

@Serializable
data class HourlyForecast(
    val times: List<String>,
    val temperatures: List<Double>,
    val precipitationProbability: List<Int>,
    val weatherCodes: List<Int>
)

@Serializable
data class DailyForecast(
    val times: List<String>,
    val maxTemps: List<Double>,
    val minTemps: List<Double>,
    val weatherCodes: List<Int>
)

class WeatherProvider {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    fun fetchFullWeather(lat: Double, lon: Double, useFahrenheit: Boolean = false): WeatherForecast? {
        val unitParam = if (useFahrenheit) "&temperature_unit=fahrenheit" else ""
        val url = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=$lat&longitude=$lon" +
                "&current=temperature_2m,relative_humidity_2m,is_day,weather_code,wind_speed_10m" +
                "&hourly=temperature_2m,precipitation_probability,weather_code" +
                "&daily=weather_code,temperature_2m_max,temperature_2m_min" +
                "&timezone=auto" + unitParam
        
        val request = Request.Builder().url(url).build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                val root = json.parseToJsonElement(body).jsonObject
                
                val currentObj = root["current"]?.jsonObject ?: return null
                val hourlyObj = root["hourly"]?.jsonObject ?: return null
                val dailyObj = root["daily"]?.jsonObject ?: return null

                val rainProb = hourlyObj["precipitation_probability"]?.jsonArray?.map { it.jsonPrimitive.int } ?: emptyList()
                val isRainingSoon = rainProb.take(2).any { it > 30 }

                WeatherForecast(
                    current = CurrentWeather(
                        temperature = currentObj["temperature_2m"]?.jsonPrimitive?.double ?: 0.0,
                        weatherCode = currentObj["weather_code"]?.jsonPrimitive?.int ?: 0,
                        isDay = (currentObj["is_day"]?.jsonPrimitive?.int ?: 1) == 1,
                        relativeHumidity = currentObj["relative_humidity_2m"]?.jsonPrimitive?.int ?: 0,
                        windSpeed = currentObj["wind_speed_10m"]?.jsonPrimitive?.double ?: 0.0
                    ),
                    hourly = HourlyForecast(
                        times = hourlyObj["time"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
                        temperatures = hourlyObj["temperature_2m"]?.jsonArray?.map { it.jsonPrimitive.double } ?: emptyList(),
                        precipitationProbability = rainProb,
                        weatherCodes = hourlyObj["weather_code"]?.jsonArray?.map { it.jsonPrimitive.int } ?: emptyList()
                    ),
                    daily = DailyForecast(
                        times = dailyObj["time"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
                        maxTemps = dailyObj["temperature_2m_max"]?.jsonArray?.map { it.jsonPrimitive.double } ?: emptyList(),
                        minTemps = dailyObj["temperature_2m_min"]?.jsonArray?.map { it.jsonPrimitive.double } ?: emptyList(),
                        weatherCodes = dailyObj["weather_code"]?.jsonArray?.map { it.jsonPrimitive.int } ?: emptyList()
                    ),
                    rainAlert = isRainingSoon
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}
