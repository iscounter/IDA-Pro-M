package com.idapro.mobile.utils

import java.security.MessageDigest

object BinaryUtils {
    
    /**
     * Calculate SHA-256 checksum of a byte array
     */
    fun calculateSHA256(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data)
        return hash.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Convert byte array to hex string
     */
    fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Convert hex string to byte array
     */
    fun hexToBytes(hex: String): ByteArray {
        val cleanHex = hex.replace("\\s".toRegex(), "").replace("0x", "")
        return cleanHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
    
    /**
     * Format bytes as human readable size
     */
    fun formatFileSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return "%.1f KB".format(kb)
        val mb = kb / 1024.0
        if (mb < 1024) return "%.1f MB".format(mb)
        val gb = mb / 1024.0
        return "%.1f GB".format(gb)
    }
    
    /**
     * Detect architecture from binary signature
     */
    fun detectArchitecture(bytes: ByteArray): String {
        if (bytes.size < 4) return "Unknown"
        
        return when {
            // ELF files
            bytes[0] == 0x7F.toByte() && bytes[1] == 'E'.code.toByte() && 
            bytes[2] == 'L'.code.toByte() && bytes[3] == 'F'.code.toByte() -> {
                if (bytes.size > 4) {
                    when (bytes[4]) {
                        1.toByte() -> "x86 (32-bit)"
                        2.toByte() -> "x86-64 (64-bit)"
                        else -> "ELF (Unknown)"
                    }
                } else {
                    "ELF"
                }
            }
            
            // PE files
            bytes[0] == 'M'.code.toByte() && bytes[1] == 'Z'.code.toByte() -> {
                "PE (Windows)"
            }
            
            // Mach-O files
            (bytes[0] == 0xFE.toByte() && bytes[1] == 0xED.toByte() && 
             bytes[2] == 0xFA.toByte() && bytes[3] == 0xCE.toByte()) -> "Mach-O (32-bit)"
            
            (bytes[0] == 0xFE.toByte() && bytes[1] == 0xED.toByte() && 
             bytes[2] == 0xFA.toByte() && bytes[3] == 0xCF.toByte()) -> "Mach-O (64-bit)"
            
            else -> "Unknown"
        }
    }
    
    /**
     * Detect file type from binary signature
     */
    fun detectFileType(bytes: ByteArray): String {
        if (bytes.size < 4) return "Unknown"
        
        return when {
            // ELF
            bytes[0] == 0x7F.toByte() && bytes[1] == 'E'.code.toByte() && 
            bytes[2] == 'L'.code.toByte() && bytes[3] == 'F'.code.toByte() -> "ELF"
            
            // PE
            bytes[0] == 'M'.code.toByte() && bytes[1] == 'Z'.code.toByte() -> "PE"
            
            // Mach-O
            (bytes[0] == 0xFE.toByte() && bytes[1] == 0xED.toByte() && 
             bytes[2] == 0xFA.toByte() && (bytes[3] == 0xCE.toByte() || bytes[3] == 0xCF.toByte())) ||
            (bytes[0] == 0xCE.toByte() && bytes[1] == 0xFA.toByte() && 
             bytes[2] == 0xED.toByte() && bytes[3] == 0xFE.toByte()) ||
            (bytes[0] == 0xCF.toByte() && bytes[1] == 0xFA.toByte() && 
             bytes[2] == 0xED.toByte() && bytes[3] == 0xFE.toByte()) -> "Mach-O"
            
            else -> "Binary"
        }
    }
    
    /**
     * Check if address is valid (within reasonable bounds)
     */
    fun isValidAddress(address: Long): Boolean {
        return address >= 0 && address < 0xFFFFFFFFL
    }
    
    /**
     * Format address as hex string with padding
     */
    fun formatAddress(address: Long, padding: Int = 8): String {
        return String.format("0x%0${padding}X", address)
    }
    
    /**
     * Extract printable strings from binary data
     */
    fun extractStrings(data: ByteArray, minLength: Int = 4): List<String> {
        val strings = mutableListOf<String>()
        var currentString = StringBuilder()
        
        for (byte in data) {
            val char = byte.toInt() and 0xFF
            if (char in 32..126) { // Printable ASCII
                currentString.append(char.toChar())
            } else {
                if (currentString.length >= minLength) {
                    strings.add(currentString.toString())
                }
                currentString.clear()
            }
        }
        
        // Add final string if it meets length requirement
        if (currentString.length >= minLength) {
            strings.add(currentString.toString())
        }
        
        return strings
    }
}
