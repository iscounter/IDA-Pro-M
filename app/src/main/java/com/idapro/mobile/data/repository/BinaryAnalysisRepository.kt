package com.idapro.mobile.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.idapro.mobile.data.database.AppDatabase
import com.idapro.mobile.data.database.entities.AnnotationEntity
import com.idapro.mobile.data.database.entities.BinaryFileEntity
import com.idapro.mobile.data.model.Annotation
import com.idapro.mobile.data.model.BinaryFile
import java.util.*

class BinaryAnalysisRepository(private val database: AppDatabase) {
    
    fun getAllBinaryFiles(): Flow<List<BinaryFile>> {
        return database.binaryFileDao().getAllFiles().map { entities ->
            entities.map { entity ->
                BinaryFile(
                    id = entity.id,
                    name = entity.name,
                    path = entity.path,
                    size = entity.size,
                    architecture = entity.architecture,
                    fileType = entity.fileType,
                    uploadedAt = entity.uploadedAt,
                    checksum = entity.checksum
                )
            }
        }
    }
    
    suspend fun getBinaryFileById(id: String): BinaryFile? {
        return database.binaryFileDao().getFileById(id)?.let { entity ->
            BinaryFile(
                id = entity.id,
                name = entity.name,
                path = entity.path,
                size = entity.size,
                architecture = entity.architecture,
                fileType = entity.fileType,
                uploadedAt = entity.uploadedAt,
                checksum = entity.checksum
            )
        }
    }
    
    suspend fun insertBinaryFile(binaryFile: BinaryFile) {
        val entity = BinaryFileEntity(
            id = binaryFile.id,
            name = binaryFile.name,
            path = binaryFile.path,
            size = binaryFile.size,
            architecture = binaryFile.architecture,
            fileType = binaryFile.fileType,
            uploadedAt = binaryFile.uploadedAt,
            checksum = binaryFile.checksum
        )
        database.binaryFileDao().insertFile(entity)
    }
    
    suspend fun deleteBinaryFile(id: String) {
        database.binaryFileDao().deleteFileById(id)
        database.annotationDao().deleteAnnotationsByFile(id)
    }
    
    fun getAnnotationsByFile(fileId: String): Flow<List<Annotation>> {
        return database.annotationDao().getAnnotationsByFile(fileId).map { entities ->
            entities.map { entity ->
                Annotation(
                    id = entity.id,
                    fileId = entity.fileId,
                    address = entity.address,
                    comment = entity.comment,
                    createdAt = entity.createdAt
                )
            }
        }
    }
    
    suspend fun insertAnnotation(annotation: Annotation) {
        val entity = AnnotationEntity(
            id = annotation.id,
            fileId = annotation.fileId,
            address = annotation.address,
            comment = annotation.comment,
            createdAt = annotation.createdAt
        )
        database.annotationDao().insertAnnotation(entity)
    }
    
    suspend fun updateAnnotation(annotation: Annotation) {
        val entity = AnnotationEntity(
            id = annotation.id,
            fileId = annotation.fileId,
            address = annotation.address,
            comment = annotation.comment,
            createdAt = annotation.createdAt
        )
        database.annotationDao().updateAnnotation(entity)
    }
}
