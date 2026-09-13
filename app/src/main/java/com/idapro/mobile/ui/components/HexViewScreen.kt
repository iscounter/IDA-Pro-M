package com.idapro.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsState
import com.idapro.mobile.ui.theme.MonospaceFont
import com.idapro.mobile.viewmodel.MainViewModel

@Composable
fun HexViewScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedFile by viewModel.selectedBinaryFile.collectAsState()
    val hexData by viewModel.hexData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    // Load hex data when file is selected
    LaunchedEffect(selectedFile) {
        selectedFile?.let { file ->
            viewModel.loadHexData(file.id)
        }
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Header with file info
        selectedFile?.let { file ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ViewModule,
                        contentDescription = "Hex View",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Hex View: ${file.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${file.size} bytes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Search bar
        if (selectedFile != null) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search (hex or ASCII)") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Content
        when {
            selectedFile == null -> {
                // No file selected
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ViewModule,
                                contentDescription = "No file",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Select a binary file to view hex data",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            isLoading -> {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading hex data...")
                    }
                }
            }
            
            hexData.isEmpty() -> {
                // No data
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hex data available",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            else -> {
                // Hex data view
                HexDataView(
                    hexData = hexData,
                    searchQuery = searchQuery,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun HexDataView(
    hexData: ByteArray,
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val bytesPerRow = 16
    val rows = hexData.chunked(bytesPerRow)
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Offset header
                    Text(
                        text = "Offset",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = MonospaceFont,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.width(80.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Hex bytes header (00-0F)
                    repeat(16) { i ->
                        Text(
                            text = String.format("%02X", i),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = MonospaceFont,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.width(24.dp)
                        )
                        if (i < 15) Spacer(modifier = Modifier.width(4.dp))
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // ASCII header
                    Text(
                        text = "ASCII",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = MonospaceFont,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            
            Divider()
            
            // Hex data rows
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                items(rows.withIndex().toList()) { (rowIndex, rowBytes) ->
                    HexDataRow(
                        offset = rowIndex * bytesPerRow,
                        bytes = rowBytes.toByteArray(),
                        searchQuery = searchQuery,
                        isHighlighted = shouldHighlightRow(rowBytes.toByteArray(), searchQuery)
                    )
                }
            }
        }
    }
}

@Composable
fun HexDataRow(
    offset: Int,
    bytes: ByteArray,
    searchQuery: String,
    isHighlighted: Boolean
) {
    SelectionContainer {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = if (isHighlighted) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                Color.Transparent
            }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Offset
                Text(
                    text = String.format("%08X", offset),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = MonospaceFont,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(80.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Hex bytes
                repeat(16) { i ->
                    if (i < bytes.size) {
                        val byte = bytes[i]
                        val isSearchMatch = isByteSearchMatch(byte, searchQuery, i)
                        Text(
                            text = String.format("%02X", byte),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = MonospaceFont,
                                fontSize = 11.sp
                            ),
                            color = if (isSearchMatch) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier
                                .width(24.dp)
                                .then(
                                    if (isSearchMatch) {
                                        Modifier.background(
                                            MaterialTheme.colorScheme.errorContainer,
                                            androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                        )
                    } else {
                        Spacer(modifier = Modifier.width(24.dp))
                    }
                    if (i < 15) Spacer(modifier = Modifier.width(4.dp))
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // ASCII representation
                Text(
                    text = bytes.map { byte ->
                        if (byte in 32..126) byte.toInt().toChar() else '.'
                    }.joinToString(""),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = MonospaceFont,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun shouldHighlightRow(bytes: ByteArray, searchQuery: String): Boolean {
    if (searchQuery.isEmpty()) return false
    
    // Search in hex representation
    val hexString = bytes.joinToString("") { String.format("%02X", it) }
    if (hexString.contains(searchQuery.uppercase(), ignoreCase = true)) {
        return true
    }
    
    // Search in ASCII representation
    val asciiString = bytes.map { byte ->
        if (byte in 32..126) byte.toInt().toChar() else '.'
    }.joinToString("")
    
    return asciiString.contains(searchQuery, ignoreCase = true)
}

private fun isByteSearchMatch(byte: Byte, searchQuery: String, position: Int): Boolean {
    if (searchQuery.isEmpty()) return false
    
    val hexByte = String.format("%02X", byte)
    return hexByte.contains(searchQuery.uppercase(), ignoreCase = true)
}
