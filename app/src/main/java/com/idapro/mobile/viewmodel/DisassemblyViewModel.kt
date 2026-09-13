package com.idapro.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.idapro.mobile.data.database.AppDatabase
import com.idapro.mobile.data.model.Annotation
import com.idapro.mobile.data.model.DisassemblyInstruction
import com.idapro.mobile.data.repository.BinaryAnalysisRepository
import com.idapro.mobile.native.NativeBinaryAnalyzer

class DisassemblyViewModel(private val database: AppDatabase) : ViewModel() {
    
    private val repository = BinaryAnalysisRepository(database)
    private val nativeAnalyzer = NativeBinaryAnalyzer()
    
    private val _instructions = MutableStateFlow<List<DisassemblyInstruction>>(emptyList())
    val instructions: StateFlow<List<DisassemblyInstruction>> = _instructions.asStateFlow()
    
    private val _annotations = MutableStateFlow<Map<Long, String>>(emptyMap())
    val annotations: StateFlow<Map<Long, String>> = _annotations.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private var currentFileId: String? = null
    
    fun loadDisassembly(fileId: String, startAddress: Long = 0, count: Int = 1000) {
        if (currentFileId == fileId && _instructions.value.isNotEmpty()) {
            return // Already loaded
        }
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                currentFileId = fileId
                
                val binaryFile = repository.getBinaryFileById(fileId)
                if (binaryFile == null) {
                    _errorMessage.value = "Binary file not found"
                    return@launch
                }
                
                // Load binary
                if (!nativeAnalyzer.loadBinary(binaryFile.path)) {
                    _errorMessage.value = "Failed to load binary file"
                    return@launch
                }
                
                // Load annotations for this file
                repository.getAnnotationsByFile(fileId).collect { annotationList ->
                    val annotationMap = annotationList.associate { 
                        it.address to it.comment 
                    }
                    _annotations.value = annotationMap
                }
                
                // Disassemble instructions
                val disassemblyData = nativeAnalyzer.disassemble(startAddress, count)
                val instructions = disassemblyData.map { data ->
                    data.toDisassemblyInstruction(_annotations.value[data.address])
                }
                
                _instructions.value = instructions
                
            } catch (e: Exception) {
                _errorMessage.value = "Error during disassembly: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun addAnnotation(address: Long, comment: String) {
        val fileId = currentFileId ?: return
        
        viewModelScope.launch {
            try {
                val annotation = Annotation(
                    fileId = fileId,
                    address = address,
                    comment = comment,
                    createdAt = System.currentTimeMillis()
                )
                
                repository.insertAnnotation(annotation)
                
                // Update local annotations map
                val updatedAnnotations = _annotations.value.toMutableMap()
                updatedAnnotations[address] = comment
                _annotations.value = updatedAnnotations
                
                // Update instructions with new comment
                val updatedInstructions = _instructions.value.map { instruction ->
                    if (instruction.address == address) {
                        instruction.copy(comment = comment)
                    } else {
                        instruction
                    }
                }
                _instructions.value = updatedInstructions
                
            } catch (e: Exception) {
                _errorMessage.value = "Error saving annotation: ${e.message}"
            }
        }
    }
    
    fun removeAnnotation(address: Long) {
        val fileId = currentFileId ?: return
        
        viewModelScope.launch {
            try {
                // Remove from database
                repository.getAnnotationsByFile(fileId).collect { annotations ->
                    val annotation = annotations.find { it.address == address }
                    // Implementation would need deleteAnnotation method in repository
                }
                
                // Update local state
                val updatedAnnotations = _annotations.value.toMutableMap()
                updatedAnnotations.remove(address)
                _annotations.value = updatedAnnotations
                
                // Update instructions
                val updatedInstructions = _instructions.value.map { instruction ->
                    if (instruction.address == address) {
                        instruction.copy(comment = null)
                    } else {
                        instruction
                    }
                }
                _instructions.value = updatedInstructions
                
            } catch (e: Exception) {
                _errorMessage.value = "Error removing annotation: ${e.message}"
            }
        }
    }
    
    fun jumpToAddress(address: Long) {
        // Load disassembly starting from the specified address
        currentFileId?.let { fileId ->
            loadDisassembly(fileId, address)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        nativeAnalyzer.cleanup()
    }
}

class DisassemblyViewModelFactory(private val database: AppDatabase) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DisassemblyViewModel::class.java)) {
            return DisassemblyViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
