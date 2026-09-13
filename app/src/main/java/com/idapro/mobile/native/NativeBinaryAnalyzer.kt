package com.idapro.mobile.native

import com.idapro.mobile.data.model.DisassemblyInstruction
import com.idapro.mobile.data.model.Function

/**
 * JNI interface for native binary analysis operations
 */
class NativeBinaryAnalyzer {
    
    /**
     * Load and analyze a binary file
     * @param filePath Path to the binary file
     * @return True if loaded successfully
     */
    external fun loadBinary(filePath: String): Boolean
    
    /**
     * Get basic file information
     * @param filePath Path to the binary file
     * @return Array containing [architecture, fileType, entryPoint]
     */
    external fun getFileInfo(filePath: String): Array<String>
    
    /**
     * Disassemble instructions from the binary
     * @param startAddress Start address for disassembly
     * @param count Number of instructions to disassemble
     * @return Array of disassembly data
     */
    external fun disassemble(startAddress: Long, count: Int): Array<DisassemblyData>
    
    /**
     * Detect functions in the binary
     * @return Array of function data
     */
    external fun detectFunctions(): Array<FunctionData>
    
    /**
     * Read raw bytes from the binary
     * @param address Start address
     * @param size Number of bytes to read
     * @return Byte array
     */
    external fun readBytes(address: Long, size: Int): ByteArray
    
    /**
     * Get the full binary data as hex
     * @return Byte array of the entire file
     */
    external fun getBinaryData(): ByteArray
    
    /**
     * Calculate file checksum
     * @param filePath Path to the binary file
     * @return SHA-256 checksum as hex string
     */
    external fun calculateChecksum(filePath: String): String
    
    /**
     * Free resources for the loaded binary
     */
    external fun cleanup()
    
    companion object {
        init {
            System.loadLibrary("binary_analyzer")
        }
    }
}

/**
 * Data class for passing disassembly information from native code
 */
data class DisassemblyData(
    val address: Long,
    val bytes: ByteArray,
    val mnemonic: String,
    val operands: String,
    val isFunction: Boolean = false,
    val isJump: Boolean = false,
    val jumpTarget: Long = 0
) {
    fun toDisassemblyInstruction(comment: String? = null): DisassemblyInstruction {
        return DisassemblyInstruction(
            address = address,
            bytes = bytes,
            mnemonic = mnemonic,
            operands = if (operands.isNotEmpty()) operands.split(", ") else emptyList(),
            comment = comment,
            isFunction = isFunction,
            isJump = isJump,
            jumpTarget = if (jumpTarget != 0L) jumpTarget else null
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DisassemblyData

        if (address != other.address) return false
        if (!bytes.contentEquals(other.bytes)) return false
        if (mnemonic != other.mnemonic) return false
        if (operands != other.operands) return false

        return true
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + bytes.contentHashCode()
        result = 31 * result + mnemonic.hashCode()
        result = 31 * result + operands.hashCode()
        return result
    }
}

/**
 * Data class for passing function information from native code
 */
data class FunctionData(
    val name: String,
    val address: Long,
    val size: Int,
    val signature: String,
    val isExported: Boolean,
    val isImported: Boolean
) {
    fun toFunction(): Function {
        return Function(
            name = name,
            address = address,
            size = size,
            signature = signature,
            isExported = isExported,
            isImported = isImported
        )
    }
}
