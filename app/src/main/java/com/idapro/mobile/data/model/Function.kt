package com.idapro.mobile.data.model

data class Function(
    val name: String,
    val address: Long,
    val size: Int,
    val signature: String,
    val isExported: Boolean,
    val isImported: Boolean,
    val callConvention: String? = null,
    val returnType: String? = null,
    val parameters: List<Parameter> = emptyList()
)

data class Parameter(
    val name: String,
    val type: String,
    val register: String? = null,
    val stackOffset: Int? = null
)
