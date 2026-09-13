package com.idapro.mobile.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "binary_files")
data class BinaryFileEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val path: String,
    val size: Long,
    val architecture: String,
    val fileType: String,
    val uploadedAt: Long,
    val checksum: String
)
