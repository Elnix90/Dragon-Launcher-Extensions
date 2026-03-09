package org.elnix.dragonlauncher.fonts

import android.content.Context
import android.graphics.Typeface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import java.io.InputStreamReader
import java.net.URL
import java.util.*

class FontListActivity : AppCompatActivity() {

    private data class FontItem(
        val family: String,
        val url: String,
        val category: String,
        var typeface: Typeface? = null
    )

    private val allFonts = mutableListOf<FontItem>()
    private val filteredFonts = mutableListOf<FontItem>()
    private lateinit var adapter: FontAdapter
    private var selectedCategory = "All"
    private var searchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_font_list)

        val tvStats = findViewById<TextView>(R.id.tv_stats)
        val lvFonts = findViewById<ListView>(R.id.lv_fonts)
        val etSearch = findViewById<EditText>(R.id.et_search)
        val spinnerCategory = findViewById<Spinner>(R.id.spinner_category)

        adapter = FontAdapter(this, filteredFonts)
        lvFonts.adapter = adapter

        loadFonts(tvStats, spinnerCategory)

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s.toString().lowercase()
                applyFilters()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        lvFonts.setOnItemClickListener { _, _, position, _ ->
            val font = filteredFonts[position]
            Toast.makeText(this, "Downloading ${font.family}...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, FontProviderService::class.java).apply {
                action = "org.elnix.dragonlauncher.ACTION_GET_FONTS"
                putExtra("FONT_NAME", font.family)
            }
            startService(intent)
        }
    }

    private fun loadFonts(tvStats: TextView, spinner: Spinner) {
        try {
            val inputStream = assets.open("google-fonts-cache.json")
            val jsonContent = InputStreamReader(inputStream).readText()
            val jsonObject = JSONObject(jsonContent)
            val fontsArray = jsonObject.getJSONArray("fonts")

            val categories = mutableSetOf("All")
            for (i in 0 until fontsArray.length()) {
                val f = fontsArray.getJSONObject(i)
                val category = f.optString("category", "unknown")
                val item = FontItem(
                    f.getString("family"),
                    f.getString("url"),
                    category
                )
                allFonts.add(item)
                categories.add(category)
            }

            tvStats.text = "Loaded ${allFonts.size} fonts"
            
            val categoryList = categories.toList().sorted()
            val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryList)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = spinnerAdapter

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    selectedCategory = categoryList[pos]
                    applyFilters()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            applyFilters()
        } catch (e: Exception) {
            Log.e("FontListActivity", "Error loading fonts", e)
        }
    }

    private fun applyFilters() {
        filteredFonts.clear()
        for (font in allFonts) {
            val matchCat = selectedCategory == "All" || font.category == selectedCategory
            val matchSearch = font.family.lowercase().contains(searchQuery)
            if (matchCat && matchSearch) {
                filteredFonts.add(font)
            }
        }
        adapter.notifyDataSetChanged()
    }

    private inner class FontAdapter(context: Context, private val items: List<FontItem>) : 
        ArrayAdapter<FontItem>(context, 0, items) {

        private val cacheDir = context.cacheDir

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_font, parent, false)
            val font = items[position]

            val tvFamily = view.findViewById<TextView>(R.id.tv_font_family)
            val tvPreview = view.findViewById<TextView>(R.id.tv_font_preview)
            val tvCategory = view.findViewById<TextView>(R.id.tv_font_category)

            tvFamily.text = font.family
            tvCategory.text = font.category.uppercase()
            
            // Preview logic
            if (font.typeface != null) {
                tvPreview.typeface = font.typeface
            } else {
                tvPreview.typeface = Typeface.DEFAULT
                loadPreview(font)
            }

            return view
        }

        private fun loadPreview(font: FontItem) {
            val file = File(cacheDir, "preview_${font.family.replace(" ", "_")}.ttf")
            if (file.exists()) {
                try {
                    font.typeface = Typeface.createFromFile(file)
                    notifyDataSetChanged()
                    return
                } catch (e: Exception) { file.delete() }
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    URL(font.url).openStream().use { input ->
                        file.outputStream().use { output -> input.copyTo(output) }
                    }
                    val tf = Typeface.createFromFile(file)
                    withContext(Dispatchers.Main) {
                        font.typeface = tf
                        notifyDataSetChanged()
                    }
                } catch (e: Exception) {
                    Log.e("FontAdapter", "Failed to preview ${font.family}")
                }
            }
        }
    }
}
