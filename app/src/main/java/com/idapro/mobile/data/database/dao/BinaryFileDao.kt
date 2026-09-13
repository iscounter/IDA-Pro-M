package com.idapro.mobile.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.idapro.mobile.data.database.entities.BinaryFileEntity

@Dao
interface BinaryFileDao {
    @Query("SELECT * FROM binary_files ORDER BY uploadedAt DESC")
    fun getAllFiles(): Flow<List<BinaryFileEntity>>
    
    @Query("SELECT * FROM binary_files WHERE id = :id")
    suspend fun getFileById(id: String): BinaryFileEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: BinaryFileEntity)
    
    @Delete
    suspend fun deleteFile(file: BinaryFileEntity)
    
    @Query("DELETE FROM binary_files WHERE id = :id")
    suspend fun deleteFileById(id: String)
}
