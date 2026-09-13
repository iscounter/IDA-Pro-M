package com.idapro.mobile.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.idapro.mobile.data.database.entities.AnnotationEntity

@Dao
interface AnnotationDao {
    @Query("SELECT * FROM annotations WHERE fileId = :fileId ORDER BY address ASC")
    fun getAnnotationsByFile(fileId: String): Flow<List<AnnotationEntity>>
    
    @Query("SELECT * FROM annotations WHERE fileId = :fileId AND address = :address")
    suspend fun getAnnotationByAddress(fileId: String, address: Long): AnnotationEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnotation(annotation: AnnotationEntity)
    
    @Update
    suspend fun updateAnnotation(annotation: AnnotationEntity)
    
    @Delete
    suspend fun deleteAnnotation(annotation: AnnotationEntity)
    
    @Query("DELETE FROM annotations WHERE fileId = :fileId")
    suspend fun deleteAnnotationsByFile(fileId: String)
}
