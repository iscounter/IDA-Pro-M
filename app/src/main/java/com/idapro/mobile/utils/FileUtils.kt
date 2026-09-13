package com.idapro.mobile.utils

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object FileUtils {
    
    /**
     * Get the file name from a Uri
     */
    fun getFileName(context: Context, uri: Uri): String? {
        var fileName: String? = null
        
        when (uri.scheme) {
            "content" -> {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val displayNameIndex = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                        if (displayNameIndex >= 0) {
                            fileName = it.getString(displayNameIndex)
                        }
                    }
                }
            }
            "file" -> {
                fileName = File(uri.path!!).name
            }
        }
        
        return fileName
    }
    
    /**
     * Copy file from Uri to app's internal storage
     */
    fun copyToAppStorage(context: Context, uri: Uri, fileName: String): File? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                return null
            }
            
            val internalDir = File(context.filesDir, "binaries")
            if (!internalDir.exists()) {
                internalDir.mkdirs()
            }
            
            val outputFile = File(internalDir, fileName)
            val outputStream = FileOutputStream(outputFile)
            
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            return outputFile
            
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Get file size from Uri
     */
    fun getFileSize(context: Context, uri: Uri): Long {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val sizeIndex = it.getColumnIndex(MediaStore.MediaColumns.SIZE)
                    if (sizeIndex >= 0) {
                        return it.getLong(sizeIndex)
                    }
                }
            }
            -1L
        } catch (e: Exception) {
            -1L
        }
    }
    
    /**
     * Check if file is a binary executable
     */
    fun isBinaryExecutable(file: File): Boolean {
        if (!file.exists() || file.length() < 4) {
            return false
        }
        
        try {
            val bytes = file.readBytes()
            
            // Check for common binary file signatures
            return when {
                // ELF files (Linux)
                bytes.size >= 4 && 
                bytes[0] == 0x7F.toByte() && 
                bytes[1] == 'E'.code.toByte() && 
                bytes[2] == 'L'.code.toByte() && 
                bytes[3] == 'F'.code.toByte() -> true
                
                // PE files (Windows)
                bytes.size >= 2 && 
                bytes[0] == 'M'.code.toByte() && 
                bytes[1] == 'Z'.code.toByte() -> true
                
                // Mach-O files (macOS)
                bytes.size >= 4 && (
                    (bytes[0] == 0xFE.toByte() && bytes[1] == 0xED.toByte() && 
                     bytes[2] == 0xFA.toByte() && bytes[3] == 0xCE.toByte()) ||
                    (bytes[0] == 0xFE.toByte() && bytes[1] == 0xED.toByte() && 
                     bytes[2] == 0xFA.toByte() && bytes[3] == 0xCF.toByte()) ||
                    (bytes[0] == 0xCE.toByte() && bytes[1] == 0xFA.toByte() && 
                     bytes[2] == 0xED.toByte() && bytes[3] == 0xFE.toByte()) ||
                    (bytes[0] == 0xCF.toByte() && bytes[1] == 0xFA.toByte() && 
                     bytes[2] == 0xED.toByte() && bytes[3] == 0xFE.toByte())
                ) -> true
                
                else -> false
            }
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * Delete file and cleanup
     */
    fun deleteFile(file: File): Boolean {
        return try {
            if (file.exists()) {
                file.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}
