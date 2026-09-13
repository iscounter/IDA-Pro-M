package com.idapro.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.idapro.mobile.IdaProMobileApplication
import com.idapro.mobile.data.model.DisassemblyInstruction
import com.idapro.mobile.ui.theme.*
import com.idapro.mobile.viewmodel.DisassemblyViewModel
import com.idapro.mobile.viewmodel.DisassemblyViewModelFactory
import com.idapro.mobile.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisassemblyScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val application = context.applicationContext as IdaProMobileApplication
    val disassemblyViewModel: DisassemblyViewModel = viewModel(
        factory = DisassemblyViewModelFactory(application.database)
    )
    
    val selectedFile by viewModel.selectedBinaryFile.collectAsState()
    val instructions by disassemblyViewModel.instructions.collectAsState()
    val isLoading by disassemblyViewModel.isLoading.collectAsState()
    val errorMessage by disassemblyViewModel.errorMessage.collectAsState()
    
    var showAnnotationDialog by remember { mutableStateOf(false) }
    var selectedInstruction by remember { mutableStateOf<DisassemblyInstruction?>(null) }
    
    // Load instructions when file is selected
    LaunchedEffect(selectedFile) {
        selectedFile?.let { file ->
            disassemblyViewModel.loadDisassembly(file.id)
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
                        Icons.Default.Code,
                        contentDescription = "Disassembly",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Disassembly: ${file.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${file.architecture} • ${instructions.size} instructions",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
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
                                Icons.Default.Code,
                                contentDescription = "No file",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Select a binary file to view disassembly",
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
                        Text("Analyzing binary...")
                    }
                }
            }
            
            errorMessage != null -> {
                // Error state
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            instructions.isEmpty() -> {
                // No instructions
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No disassembly data available",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            else -> {
                // Instructions list
                val listState = rememberLazyListState()
                
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    items(instructions) { instruction ->
                        DisassemblyInstructionRow(
                            instruction = instruction,
                            onClick = {
                                selectedInstruction = instruction
                                showAnnotationDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Annotation dialog
    if (showAnnotationDialog && selectedInstruction != null) {
        AnnotationDialog(
            instruction = selectedInstruction!!,
            onDismiss = { showAnnotationDialog = false },
            onSave = { comment ->
                disassemblyViewModel.addAnnotation(
                    selectedInstruction!!.address,
                    comment
                )
                showAnnotationDialog = false
            }
        )
    }
}

@Composable
fun DisassemblyInstructionRow(
    instruction: DisassemblyInstruction,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 1.dp),
        onClick = onClick,
        color = if (instruction.isFunction) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        } else {
            Color.Transparent
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Address
            Text(
                text = String.format("%08X", instruction.address),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = MonospaceFont,
                    fontSize = 11.sp
                ),
                color = AsmAddress,
                modifier = Modifier.width(80.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Bytes
            Text(
                text = instruction.bytes.take(8).joinToString("") { "%02X".format(it) }
                    .padEnd(16, ' '),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = MonospaceFont,
                    fontSize = 11.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(120.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Mnemonic and operands
            ClickableText(
                text = buildAssemblyText(instruction),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = MonospaceFont,
                    fontSize = 11.sp
                ),
                onClick = { onClick() },
                modifier = Modifier.weight(1f)
            )
            
            // Comment/annotation
            instruction.comment?.let { comment ->
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "; $comment",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = MonospaceFont,
                        fontSize = 11.sp
                    ),
                    color = AsmComment
                )
            }
        }
    }
}

@Composable
fun buildAssemblyText(instruction: DisassemblyInstruction): AnnotatedString {
    return buildAnnotatedString {
        // Mnemonic
        withStyle(SpanStyle(color = AsmKeyword, fontWeight = FontWeight.Bold)) {
            append(instruction.mnemonic.padEnd(8))
        }
        
        // Operands with syntax highlighting
        instruction.operands.forEachIndexed { index, operand ->
            if (index > 0) append(", ")
            
            when {
                operand.startsWith("0x") -> {
                    withStyle(SpanStyle(color = AsmImmediate)) {
                        append(operand)
                    }
                }
                operand.matches(Regex("[a-z]+[0-9]*")) -> {
                    withStyle(SpanStyle(color = AsmRegister)) {
                        append(operand)
                    }
                }
                else -> {
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                        append(operand)
                    }
                }
            }
        }
    }
}

@Composable
fun AnnotationDialog(
    instruction: DisassemblyInstruction,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var comment by remember { mutableStateOf(instruction.comment ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Annotation")
        },
        text = {
            Column {
                Text(
                    text = "Address: ${String.format("0x%08X", instruction.address)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${instruction.mnemonic} ${instruction.operands.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = MonospaceFont
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(comment.trim()) }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
