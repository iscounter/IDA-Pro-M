package com.idapro.mobile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.idapro.mobile.IdaProMobileApplication
import com.idapro.mobile.viewmodel.MainViewModel
import com.idapro.mobile.viewmodel.MainViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val application = context.applicationContext as IdaProMobileApplication
    val viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(application.database)
    )
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Files", "Disassembly", "Hex View", "Functions")
    
    Column(modifier = modifier) {
        // Top App Bar
        TopAppBar(
            title = { Text("IDA Pro Mobile") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    icon = {
                        when (index) {
                            0 -> Icon(Icons.Default.Folder, contentDescription = null)
                            1 -> Icon(Icons.Default.Code, contentDescription = null)
                            2 -> Icon(Icons.Default.ViewModule, contentDescription = null)
                            3 -> Icon(Icons.Default.Functions, contentDescription = null)
                        }
                    }
                )
            }
        }
        
        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> FileUploadScreen(viewModel = viewModel)
                1 -> DisassemblyScreen(viewModel = viewModel)
                2 -> HexViewScreen(viewModel = viewModel)
                3 -> FunctionListScreen(viewModel = viewModel)
            }
        }
    }
}
