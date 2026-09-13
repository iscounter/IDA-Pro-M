package com.idapro.mobile

import com.idapro.mobile.utils.BinaryUtils
import com.idapro.mobile.utils.FileUtils
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*
import java.io.File

/**
 * Unit tests for binary analysis functionality
 */
class BinaryAnalysisTest {

    @Test
    fun testBinaryUtils_detectArchitecture() {
        // Test ELF 32-bit
        val elf32 = byteArrayOf(0x7F, 'E'.code.toByte(), 'L'.code.toByte(), 'F'.code.toByte(), 1)
        assertEquals("x86 (32-bit)", BinaryUtils.detectArchitecture(elf32))
        
        // Test ELF 64-bit
        val elf64 = byteArrayOf(0x7F, 'E'.code.toByte(), 'L'.code.toByte(), 'F'.code.toByte(), 2)
        assertEquals("x86-64 (64-bit)", BinaryUtils.detectArchitecture(elf64))
        
        // Test PE
        val pe = byteArrayOf('M'.code.toByte(), 'Z'.code.toByte())
        assertEquals("PE (Windows)", BinaryUtils.detectArchitecture(pe))
        
        // Test unknown
        val unknown = byteArrayOf(0x00, 0x01, 0x02, 0x03)
        assertEquals("Unknown", BinaryUtils.detectArchitecture(unknown))
    }

    @Test
    fun testBinaryUtils_detectFileType() {
        // Test ELF
        val elf = byteArrayOf(0x7F, 'E'.code.toByte(), 'L'.code.toByte(), 'F'.code.toByte())
        assertEquals("ELF", BinaryUtils.detectFileType(elf))
        
        // Test PE
        val pe = byteArrayOf('M'.code.toByte(), 'Z'.code.toByte())
        assertEquals("PE", BinaryUtils.detectFileType(pe))
        
        // Test Mach-O (little endian)
        val machO = byteArrayOf(0xFE.toByte(), 0xED.toByte(), 0xFA.toByte(), 0xCE.toByte())
        assertEquals("Mach-O", BinaryUtils.detectFileType(machO))
        
        // Test unknown
        val unknown = byteArrayOf(0x00, 0x01)
        assertEquals("Binary", BinaryUtils.detectFileType(unknown))
    }

    @Test
    fun testBinaryUtils_calculateSHA256() {
        val testData = "Hello World".toByteArray()
        val expectedHash = "a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e"
        assertEquals(expectedHash, BinaryUtils.calculateSHA256(testData))
    }

    @Test
    fun testBinaryUtils_bytesToHex() {
        val bytes = byteArrayOf(0x01, 0x23, 0x45, 0x67, 0x89.toByte(), 0xAB.toByte(), 0xCD.toByte(), 0xEF.toByte())
        assertEquals("0123456789abcdef", BinaryUtils.bytesToHex(bytes))
    }

    @Test
    fun testBinaryUtils_hexToBytes() {
        val hex = "0123456789ABCDEF"
        val expectedBytes = byteArrayOf(0x01, 0x23, 0x45, 0x67, 0x89.toByte(), 0xAB.toByte(), 0xCD.toByte(), 0xEF.toByte())
        assertArrayEquals(expectedBytes, BinaryUtils.hexToBytes(hex))
    }

    @Test
    fun testBinaryUtils_formatFileSize() {
        assertEquals("512 B", BinaryUtils.formatFileSize(512))
        assertEquals("1.0 KB", BinaryUtils.formatFileSize(1024))
        assertEquals("1.5 KB", BinaryUtils.formatFileSize(1536))
        assertEquals("1.0 MB", BinaryUtils.formatFileSize(1024 * 1024))
        assertEquals("2.5 GB", BinaryUtils.formatFileSize((2.5 * 1024 * 1024 * 1024).toLong()))
    }

    @Test
    fun testBinaryUtils_formatAddress() {
        assertEquals("0x00001000", BinaryUtils.formatAddress(0x1000))
        assertEquals("0x1234ABCD", BinaryUtils.formatAddress(0x1234ABCD))
        assertEquals("0x000A", BinaryUtils.formatAddress(0xA, 4))
    }

    @Test
    fun testBinaryUtils_isValidAddress() {
        assertTrue(BinaryUtils.isValidAddress(0x1000))
        assertTrue(BinaryUtils.isValidAddress(0x12345678))
        assertTrue(BinaryUtils.isValidAddress(0))
        assertFalse(BinaryUtils.isValidAddress(-1))
        assertFalse(BinaryUtils.isValidAddress(0x100000000L)) // Beyond 32-bit range
    }

    @Test
    fun testBinaryUtils_extractStrings() {
        val data = byteArrayOf(
            'H'.code.toByte(), 'e'.code.toByte(), 'l'.code.toByte(), 'l'.code.toByte(), 'o'.code.toByte(),
            0x00, 0x01, 0x02,
            'W'.code.toByte(), 'o'.code.toByte(), 'r'.code.toByte(), 'l'.code.toByte(), 'd'.code.toByte(),
            0xFF.toByte(),
            'T'.code.toByte(), 'e'.code.toByte(), 's'.code.toByte(), 't'.code.toByte()
        )
        
        val strings = BinaryUtils.extractStrings(data, 4)
        assertEquals(3, strings.size)
        assertEquals("Hello", strings[0])
        assertEquals("World", strings[1])
        assertEquals("Test", strings[2])
    }

    @Test
    fun testFileUtils_isBinaryExecutable() {
        // Create temporary files for testing
        val elfFile = File.createTempFile("test", ".elf")
        val elfBytes = byteArrayOf(0x7F, 'E'.code.toByte(), 'L'.code.toByte(), 'F'.code.toByte())
        elfFile.writeBytes(elfBytes)
        assertTrue(FileUtils.isBinaryExecutable(elfFile))
        elfFile.delete()

        val peFile = File.createTempFile("test", ".exe")
        val peBytes = byteArrayOf('M'.code.toByte(), 'Z'.code.toByte(), 0x00, 0x00)
        peFile.writeBytes(peBytes)
        assertTrue(FileUtils.isBinaryExecutable(peFile))
        peFile.delete()

        val textFile = File.createTempFile("test", ".txt")
        textFile.writeText("This is not a binary file")
        assertFalse(FileUtils.isBinaryExecutable(textFile))
        textFile.delete()
    }

    @Test
    fun testNativeBinaryAnalyzer_integration() {
        // Note: This test requires native library to be loaded
        // In a real scenario, you would mock the native calls or use actual test binaries
        
        // For now, we'll test the data classes used for JNI communication
        val disassemblyData = com.idapro.mobile.native.DisassemblyData(
            address = 0x1000L,
            bytes = byteArrayOf(0x55, 0x89.toByte(), 0xE5.toByte()),
            mnemonic = "push",
            operands = "ebp",
            isFunction = true,
            isJump = false,
            jumpTarget = 0L
        )
        
        val instruction = disassemblyData.toDisassemblyInstruction()
        assertEquals(0x1000L, instruction.address)
        assertEquals("push", instruction.mnemonic)
        assertEquals(listOf("ebp"), instruction.operands)
        assertTrue(instruction.isFunction)
        assertFalse(instruction.isJump)
        
        val functionData = com.idapro.mobile.native.FunctionData(
            name = "main",
            address = 0x1000L,
            size = 128,
            signature = "int main(int argc, char** argv)",
            isExported = true,
            isImported = false
        )
        
        val function = functionData.toFunction()
        assertEquals("main", function.name)
        assertEquals(0x1000L, function.address)
        assertEquals(128, function.size)
        assertTrue(function.isExported)
        assertFalse(function.isImported)
    }
}
