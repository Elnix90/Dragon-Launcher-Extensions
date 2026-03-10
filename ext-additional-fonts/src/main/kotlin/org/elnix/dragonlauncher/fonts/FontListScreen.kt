package org.elnix.dragonlauncher.fonts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.GenericFontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.graphics.Typeface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontListScreen(
    viewModel: FontViewModel,
    onFontClick: (FontItem) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Google Fonts") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Search and Category Filter
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                onSelectedCategoryChange = { viewModel.updateSelectedCategory(it) },
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                Text(
                    text = "Loaded ${uiState.filteredFonts.size} fonts",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.LightGray
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.filteredFonts) { font ->
                        FontListItem(
                            font = font, 
                            onClick = { onFontClick(font) },
                            onAppear = { viewModel.loadPreview(font) }
                        )
                        Divider(color = Color.DarkGray)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSelectedCategoryChange: (String) -> Unit,
    categories: List<String>,
    selectedCategory: String
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search fonts", color = Color.White) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White) },
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color.White,
                cursorColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
            ) {
                Text("Category: $selectedCategory")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            onSelectedCategoryChange(category)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FontListItem(
    font: FontItem,
    onClick: () -> Unit,
    onAppear: () -> Unit
) {
    LaunchedEffect(font.family) {
        onAppear()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = font.family,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontFamily = font.typeface?.let { FontFamily(it) } ?: FontFamily.Default
        )
        Text(
            text = "The quick brown fox jumps over the lazy dog",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.LightGray,
            fontFamily = font.typeface?.let { FontFamily(it) } ?: FontFamily.Default,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        Text(
            text = font.category,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}
