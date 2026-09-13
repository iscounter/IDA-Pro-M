package com.idapro.mobile

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.idapro.mobile.data.database.AppDatabase
import com.idapro.mobile.data.database.entities.AnnotationEntity
import com.idapro.mobile.data.database.entities.BinaryFileEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Instrumented tests for Room database functionality
 */
@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var database: AppDatabase

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testBinaryFileDao_insertAndRetrieve() = runBlocking {
        val binaryFileDao = database.binaryFileDao()

        val testFile = BinaryFileEntity(
            id = "test-id-1",
            name = "test.exe",
            path = "/data/data/com.idapro.mobile/files/test.exe",
            size = 1024L,
            architecture = "x86",
            fileType = "PE",
            uploadedAt = System.currentTimeMillis(),
            checksum = "abc123def456"
        )

        // Insert file
        binaryFileDao.insertFile(testFile)

        // Retrieve all files
        val files = binaryFileDao.getAllFiles().first()
        assertEquals(1, files.size)
        assertEquals(testFile.id, files[0].id)
        assertEquals(testFile.name, files[0].name)
        assertEquals(testFile.architecture, files[0].architecture)

        // Retrieve by ID
        val retrievedFile = binaryFileDao.getFileById(testFile.id)
        assertNotNull(retrievedFile)
        assertEquals(testFile.name, retrievedFile?.name)
    }

    @Test
    fun testBinaryFileDao_deleteFile() = runBlocking {
        val binaryFileDao = database.binaryFileDao()

        val testFile = BinaryFileEntity(
            id = "test-id-2",
            name = "test2.elf",
            path = "/data/data/com.idapro.mobile/files/test2.elf",
            size = 2048L,
            architecture = "x86-64",
            fileType = "ELF",
            uploadedAt = System.currentTimeMillis(),
            checksum = "def789ghi012"
        )

        // Insert and verify
        binaryFileDao.insertFile(testFile)
        var files = binaryFileDao.getAllFiles().first()
        assertEquals(1, files.size)

        // Delete by ID
        binaryFileDao.deleteFileById(testFile.id)
        files = binaryFileDao.getAllFiles().first()
        assertEquals(0, files.size)
    }

    @Test
    fun testAnnotationDao_insertAndRetrieve() = runBlocking {
        val binaryFileDao = database.binaryFileDao()
        val annotationDao = database.annotationDao()

        // First insert a binary file
        val testFile = BinaryFileEntity(
            id = "test-file-1",
            name = "annotated.exe",
            path = "/data/data/com.idapro.mobile/files/annotated.exe",
            size = 512L,
            architecture = "x86",
            fileType = "PE",
            uploadedAt = System.currentTimeMillis(),
            checksum = "annotation123"
        )
        binaryFileDao.insertFile(testFile)

        // Insert annotations
        val annotation1 = AnnotationEntity(
            fileId = testFile.id,
            address = 0x1000L,
            comment = "This is the main function",
            createdAt = System.currentTimeMillis()
        )
        
        val annotation2 = AnnotationEntity(
            fileId = testFile.id,
            address = 0x1010L,
            comment = "Function prologue",
            createdAt = System.currentTimeMillis()
        )

        annotationDao.insertAnnotation(annotation1)
        annotationDao.insertAnnotation(annotation2)

        // Retrieve annotations
        val annotations = annotationDao.getAnnotationsByFile(testFile.id).first()
        assertEquals(2, annotations.size)
        
        // Check order (should be sorted by address)
        assertEquals(0x1000L, annotations[0].address)
        assertEquals(0x1010L, annotations[1].address)
        assertEquals("This is the main function", annotations[0].comment)
        assertEquals("Function prologue", annotations[1].comment)
    }

    @Test
    fun testAnnotationDao_updateAnnotation() = runBlocking {
        val binaryFileDao = database.binaryFileDao()
        val annotationDao = database.annotationDao()

        // Insert binary file
        val testFile = BinaryFileEntity(
            id = "test-file-2",
            name = "update_test.elf",
            path = "/data/data/com.idapro.mobile/files/update_test.elf",
            size = 256L,
            architecture = "ARM",
            fileType = "ELF",
            uploadedAt = System.currentTimeMillis(),
            checksum = "update456"
        )
        binaryFileDao.insertFile(testFile)

        // Insert annotation
        val annotation = AnnotationEntity(
            fileId = testFile.id,
            address = 0x2000L,
            comment = "Original comment",
            createdAt = System.currentTimeMillis()
        )
        annotationDao.insertAnnotation(annotation)

        // Retrieve and verify
        var retrieved = annotationDao.getAnnotationByAddress(testFile.id, 0x2000L)
        assertNotNull(retrieved)
        assertEquals("Original comment", retrieved?.comment)

        // Update annotation
        val updatedAnnotation = retrieved!!.copy(
            comment = "Updated comment",
            createdAt = System.currentTimeMillis()
        )
        annotationDao.updateAnnotation(updatedAnnotation)

        // Verify update
        retrieved = annotationDao.getAnnotationByAddress(testFile.id, 0x2000L)
        assertNotNull(retrieved)
        assertEquals("Updated comment", retrieved?.comment)
    }

    @Test
    fun testForeignKeyConstraint_cascadeDelete() = runBlocking {
        val binaryFileDao = database.binaryFileDao()
        val annotationDao = database.annotationDao()

        // Insert binary file
        val testFile = BinaryFileEntity(
            id = "cascade-test",
            name = "cascade.bin",
            path = "/data/data/com.idapro.mobile/files/cascade.bin",
            size = 128L,
            architecture = "x86",
            fileType = "Binary",
            uploadedAt = System.currentTimeMillis(),
            checksum = "cascade789"
        )
        binaryFileDao.insertFile(testFile)

        // Insert annotations
        val annotation1 = AnnotationEntity(
            fileId = testFile.id,
            address = 0x3000L,
            comment = "First annotation",
            createdAt = System.currentTimeMillis()
        )
        
        val annotation2 = AnnotationEntity(
            fileId = testFile.id,
            address = 0x3010L,
            comment = "Second annotation",
            createdAt = System.currentTimeMillis()
        )

        annotationDao.insertAnnotation(annotation1)
        annotationDao.insertAnnotation(annotation2)

        // Verify annotations exist
        var annotations = annotationDao.getAnnotationsByFile(testFile.id).first()
        assertEquals(2, annotations.size)

        // Delete binary file (should cascade delete annotations)
        binaryFileDao.deleteFileById(testFile.id)

        // Verify annotations are also deleted
        annotations = annotationDao.getAnnotationsByFile(testFile.id).first()
        assertEquals(0, annotations.size)

        // Verify binary file is deleted
        val files = binaryFileDao.getAllFiles().first()
        assertEquals(0, files.size)
    }

    @Test
    fun testMultipleBinaryFiles() = runBlocking {
        val binaryFileDao = database.binaryFileDao()

        val file1 = BinaryFileEntity(
            id = "multi-1",
            name = "file1.exe",
            path = "/path/file1.exe",
            size = 1000L,
            architecture = "x86",
            fileType = "PE",
            uploadedAt = 1000L,
            checksum = "checksum1"
        )

        val file2 = BinaryFileEntity(
            id = "multi-2",
            name = "file2.elf",
            path = "/path/file2.elf",
            size = 2000L,
            architecture = "x86-64",
            fileType = "ELF",
            uploadedAt = 2000L,
            checksum = "checksum2"
        )

        val file3 = BinaryFileEntity(
            id = "multi-3",
            name = "file3.dylib",
            path = "/path/file3.dylib",
            size = 1500L,
            architecture = "ARM",
            fileType = "Mach-O",
            uploadedAt = 1500L,
            checksum = "checksum3"
        )

        // Insert all files
        binaryFileDao.insertFile(file1)
        binaryFileDao.insertFile(file2)
        binaryFileDao.insertFile(file3)

        // Verify all files are stored
        val files = binaryFileDao.getAllFiles().first()
        assertEquals(3, files.size)

        // Verify ordering (should be by uploadedAt DESC)
        assertEquals("file2.elf", files[0].name) // uploadedAt = 2000
        assertEquals("file3.dylib", files[1].name) // uploadedAt = 1500
        assertEquals("file1.exe", files[2].name) // uploadedAt = 1000
    }
}
