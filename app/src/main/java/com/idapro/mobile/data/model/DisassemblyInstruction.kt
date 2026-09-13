package com.idapro.mobile.data.model

data class DisassemblyInstruction(
    val address: Long,
    val bytes: ByteArray,
    val mnemonic: String,
    val operands: List<String>,
    val comment: String? = null,
    val isFunction: Boolean = false,
    val isJump: Boolean = false,
    val jumpTarget: Long? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DisassemblyInstruction

        if (address != other.address) return false
        if (!bytes.contentEquals(other.bytes)) return false
        if (mnemonic != other.mnemonic) return false
        if (operands != other.operands) return false
        if (comment != other.comment) return false
        if (isFunction != other.isFunction) return false
        if (isJump != other.isJump) return false
        if (jumpTarget != other.jumpTarget) return false

        return true
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + bytes.contentHashCode()
        result = 31 * result + mnemonic.hashCode()
        result = 31 * result + operands.hashCode()
        result = 31 * result + (comment?.hashCode() ?: 0)
        result = 31 * result + isFunction.hashCode()
        result = 31 * result + isJump.hashCode()
        result = 31 * result + (jumpTarget?.hashCode() ?: 0)
        return result
    }
}
