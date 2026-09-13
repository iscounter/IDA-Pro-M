package com.idapro.mobile.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "annotations",
    foreignKeys = [
        ForeignKey(
            entity = BinaryFileEntity::class,
            parentColumns = ["id"],
            childColumns = ["fileId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AnnotationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileId: String,
    val address: Long,
    val comment: String,
    val createdAt: Long
)
