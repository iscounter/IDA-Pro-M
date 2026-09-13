package com.idapro.mobile.data.model

data class Annotation(
    val id: Long = 0,
    val fileId: String,
    val address: Long,
    val comment: String,
    val createdAt: Long
)
