package com.placely.common.security.crypto

enum class CryptoConstants(
    val description: String,
    val algorithm: String,
    val transformation: String? = null
) {
    ENCRYPTION("암호화", "AES", "AES/GCM/NoPadding"),
    DECRYPTION("복호화", "AES", "AES/GCM/NoPadding"),
    HASHING("해싱", "SHA-256", null)
}