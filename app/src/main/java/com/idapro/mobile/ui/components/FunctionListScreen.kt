package com.idapro.mobile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsState
import com.idapro.mobile.data.model.Function
import com.idapro.mobile.ui.theme.MonospaceFont
import com.idapro.mobile.viewmodel.MainViewModel

@Composable
fun FunctionListScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedFile by viewModel.selectedBinaryFile.collectAsState()
    val functions by viewModel.functions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    // Load functions when file is selected
    LaunchedEffect(selectedFile) {
        selectedFile?.let { file ->
            viewModel.loadFunctions(file.id)
        }
    }
    
    // Filter functions based on search query
    val filteredFunctions = remember(functions, searchQuery) {
        if (searchQuery.isEmpty()) {
            functions
        } else {
            functions.filter { function ->
                function.name.contains(searchQuery, ignoreCase = true) ||
                function.signature.contains(searchQuery, ignoreCase = true) ||
                String.format("%08X", function.address).contains(searchQuery.uppercase())
            }
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
                        Icons.Default.Functions,
                        contentDescription = "Functions",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Functions: ${file.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${functions.size} functions detected",
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
                label = { Text("Search functions") },
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Functions,
                                contentDescription = "No file",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Select a binary file to view functions",
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
                        Text("Analyzing functions...")
                    }
                }
            }
            
            filteredFunctions.isEmpty() && searchQuery.isNotEmpty() -> {
                // No search results
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = "No results",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No functions found for '$searchQuery'",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            functions.isEmpty() -> {
                // No functions detected
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No functions detected in this binary",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            else -> {
                // Functions list
                LazyColumn {
                    item {
                        // Statistics card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatisticItem(
                                    label = "Total",
                                    value = functions.size.toString()
                                )
                                StatisticItem(
                                    label = "Exported",
                                    value = functions.count { it.isExported }.toString()
                                )
                                StatisticItem(
                                    label = "Imported",
                                    value = functions.count { it.isImported }.toString()
                                )
                                StatisticItem(
                                    label = "Internal",
                                    value = functions.count { !it.isExported && !it.isImported }.toString()
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    items(filteredFunctions) { function ->
                        FunctionItem(
                            function = function,
                            onClick = { viewModel.navigateToFunction(function) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticItem(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FunctionItem(
    function: Function,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Function type icon
            Icon(
                imageVector = when {
                    function.isExported -> Icons.Default.CallMade
                    function.isImported -> Icons.Default.CallReceived
                    else -> Icons.Default.Code
                },
                contentDescription = when {
                    function.isExported -> "Exported"
                    function.isImported -> "Imported"
                    else -> "Internal"
                },
                tint = when {
                    function.isExported -> MaterialTheme.colorScheme.primary
                    function.isImported -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.tertiary
                },
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                // Function name
                Text(
                    text = function.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                // Function signature
                if (function.signature.isNotEmpty()) {
                    Text(
                        text = function.signature,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = MonospaceFont,
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Address and size
                Row {
                    Text(
                        text = String.format("0x%08X", function.address),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = MonospaceFont
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    if (function.size > 0) {
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "${function.size} bytes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Navigation arrow
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View function",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
