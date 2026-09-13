package com.idapro.mobile.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.idapro.mobile.data.database.AppDatabase
import com.idapro.mobile.data.model.BinaryFile
import com.idapro.mobile.data.model.Function
import com.idapro.mobile.data.repository.BinaryAnalysisRepository
import com.idapro.mobile.native.NativeBinaryAnalyzer
import com.idapro.mobile.utils.BinaryUtils
import com.idapro.mobile.utils.FileUtils
import java.util.*

class MainViewModel(private val database: AppDatabase) : ViewModel() {
    
    private val repository = BinaryAnalysisRepository(database)
    private val nativeAnalyzer = NativeBinaryAnalyzer()
    
    private val _binaryFiles = MutableStateFlow<List<BinaryFile>>(emptyList())
    val binaryFiles: StateFlow<List<BinaryFile>> = _binaryFiles.asStateFlow()
    
    private val _selectedBinaryFile = MutableStateFlow<BinaryFile?>(null)
    val selectedBinaryFile: StateFlow<BinaryFile?> = _selectedBinaryFile.asStateFlow()
    
    private val _functions = MutableStateFlow<List<Function>>(emptyList())
    val functions: StateFlow<List<Function>> = _functions.asStateFlow()
    
    private val _hexData = MutableStateFlow<ByteArray>(byteArrayOf())
    val hexData: StateFlow<ByteArray> = _hexData.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        loadBinaryFiles()
    }
    
    private fun loadBinaryFiles() {
        viewModelScope.launch {
            repository.getAllBinaryFiles().collect { files ->
                _binaryFiles.value = files
            }
        }
    }
    
    fun loadBinaryFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // Copy file to app storage
                val fileName = FileUtils.getFileName(context, uri) ?: "unknown_binary"
                val localFile = FileUtils.copyToAppStorage(context, uri, fileName)
                
                if (localFile == null) {
                    _errorMessage.value = "Failed to copy file to app storage"
                    return@launch
                }
                
                // Analyze file with native code
                val fileInfo = nativeAnalyzer.getFileInfo(localFile.absolutePath)
                val checksum = nativeAnalyzer.calculateChecksum(localFile.absolutePath)
                
                if (fileInfo.size < 3) {
                    _errorMessage.value = "Failed to analyze binary file"
                    return@launch
                }
                
                // Create BinaryFile object
                val binaryFile = BinaryFile(
                    id = UUID.randomUUID().toString(),
                    name = fileName,
                    path = localFile.absolutePath,
                    size = localFile.length(),
                    architecture = fileInfo[0],
                    fileType = fileInfo[1],
                    uploadedAt = System.currentTimeMillis(),
                    checksum = checksum
                )
                
                // Save to database
                repository.insertBinaryFile(binaryFile)
                
                // Select the newly loaded file
                _selectedBinaryFile.value = binaryFile
                
            } catch (e: Exception) {
                _errorMessage.value = "Error loading binary file: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun selectBinaryFile(binaryFile: BinaryFile) {
        _selectedBinaryFile.value = binaryFile
        // Clear previous data
        _functions.value = emptyList()
        _hexData.value = byteArrayOf()
        _errorMessage.value = null
    }
    
    fun deleteBinaryFile(fileId: String) {
        viewModelScope.launch {
            try {
                // If this is the selected file, deselect it
                if (_selectedBinaryFile.value?.id == fileId) {
                    _selectedBinaryFile.value = null
                    _functions.value = emptyList()
                    _hexData.value = byteArrayOf()
                }
                
                // Delete from database
                repository.deleteBinaryFile(fileId)
                
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting file: ${e.message}"
            }
        }
    }
    
    fun loadFunctions(fileId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val binaryFile = repository.getBinaryFileById(fileId)
                if (binaryFile == null) {
                    _errorMessage.value = "Binary file not found"
                    return@launch
                }
                
                // Load binary and detect functions
                if (!nativeAnalyzer.loadBinary(binaryFile.path)) {
                    _errorMessage.value = "Failed to load binary file"
                    return@launch
                }
                
                val functionData = nativeAnalyzer.detectFunctions()
                val functions = functionData.map { it.toFunction() }
                
                _functions.value = functions
                
            } catch (e: Exception) {
                _errorMessage.value = "Error analyzing functions: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadHexData(fileId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val binaryFile = repository.getBinaryFileById(fileId)
                if (binaryFile == null) {
                    _errorMessage.value = "Binary file not found"
                    return@launch
                }
                
                // Load binary data
                if (!nativeAnalyzer.loadBinary(binaryFile.path)) {
                    _errorMessage.value = "Failed to load binary file"
                    return@launch
                }
                
                val hexData = nativeAnalyzer.getBinaryData()
                _hexData.value = hexData
                
            } catch (e: Exception) {
                _errorMessage.value = "Error loading hex data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun navigateToFunction(function: Function) {
        // This could trigger navigation to disassembly view at function address
        // For now, we'll just select the function (implementation depends on navigation setup)
    }
    
    override fun onCleared() {
        super.onCleared()
        nativeAnalyzer.cleanup()
    }
}

class MainViewModelFactory(private val database: AppDatabase) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
