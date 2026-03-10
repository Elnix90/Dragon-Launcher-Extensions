package org.elnix.dragonlauncher.fonts

import android.app.Application
import android.graphics.Typeface
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.InputStreamReader
import java.net.URL

data class FontItem(
    val family: String,
    val url: String,
    val category: String,
    var typeface: Typeface? = null
)

data class FontUiState(
    val allFonts: List<FontItem> = emptyList(),
    val filteredFonts: List<FontItem> = emptyList(),
    val categories: List<String> = listOf("All"),
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

class FontViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(FontUiState())
    val uiState: StateFlow<FontUiState> = _uiState.asStateFlow()

    init {
        loadFonts()
    }

    private fun loadFonts() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = getApplication<Application>().assets.open("google-fonts-cache.json")
                val jsonContent = InputStreamReader(inputStream).readText()
                val jsonObject = JSONObject(jsonContent)
                val fontsArray = jsonObject.getJSONArray("fonts")

                val fonts = mutableListOf<FontItem>()
                val categories = mutableSetOf("All")

                for (i in 0 until fontsArray.length()) {
                    val f = fontsArray.getJSONObject(i)
                    val category = f.optString("category", "unknown")
                    val item = FontItem(
                        f.getString("family"),
                        f.getString("url"),
                        category
                    )
                    fonts.add(item)
                    categories.add(category)
                }

                _uiState.value = _uiState.value.copy(
                    allFonts = fonts,
                    filteredFonts = fonts,
                    categories = categories.toList().sorted(),
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun updateSelectedCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        applyFilters()
    }

    private fun applyFilters() {
        val currentState = _uiState.value
        val filtered = currentState.allFonts.filter { font ->
            val matchesCategory = currentState.selectedCategory == "All" || font.category == currentState.selectedCategory
            val matchesSearch = font.family.lowercase().contains(currentState.searchQuery.lowercase())
            matchesCategory && matchesSearch
        }
        _uiState.value = currentState.copy(filteredFonts = filtered)
    }

    fun loadPreview(font: FontItem) {
        if (font.typeface != null) return

        val cacheDir = getApplication<Application>().cacheDir
        val file = File(cacheDir, "preview_${font.family.replace(" ", "_")}.ttf")
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!file.exists()) {
                    URL(font.url).openStream().use { input ->
                        file.outputStream().use { output -> input.copyTo(output) }
                    }
                }
                val tf = Typeface.createFromFile(file)
                withContext(Dispatchers.Main) {
                    font.typeface = tf
                    // Force update to refresh list
                    _uiState.value = _uiState.value.copy(
                        filteredFonts = _uiState.value.filteredFonts.toList()
                    )
                }
            } catch (e: Exception) {
                // Ignore preview failure
            }
        }
    }
}