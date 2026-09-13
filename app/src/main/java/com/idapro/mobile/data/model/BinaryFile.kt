package com.idapro.mobile.data.model

data class BinaryFile(
    val id: String,
    val name: String,
    val path: String,
    val size: Long,
    val architecture: String,
    val fileType: String,
    val uploadedAt: Long,
    val checksum: String
)
