package com.placely.auth.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 토큰 엔티티 (AUTH_TOKENS 테이블)
 */
@Entity
@Table(name = "AUTH_TOKENS")
data class Token(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_auth_tokens")
    @SequenceGenerator(name = "seq_auth_tokens", sequenceName = "SEQ_AUTH_TOKENS", allocationSize = 1)
    @Column(name = "TOKEN_ID")
    val tokenId: Long = 0,

    @Column(name = "USER_ID", nullable = false)
    val userId: Long = 0,

    @Column(name = "TOKEN_TYPE", length = 20)
    @Enumerated(EnumType.STRING)
    val tokenType: TokenType = TokenType.ACCESS,

    @Column(name = "TOKEN_VALUE", nullable = false, length = 255)
    val tokenValue: String = "",

    @Column(name = "EXPIRES_AT", nullable = false)
    val expiresAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "REVOKED_AT")
    val revokedAt: LocalDateTime? = null,

    @Column(name = "CREATED_AT")
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    // JPA를 위한 기본 생성자
    constructor() : this(
        tokenId = 0,
        userId = 0,
        tokenType = TokenType.ACCESS,
        tokenValue = "",
        expiresAt = LocalDateTime.now(),
        revokedAt = null,
        createdAt = LocalDateTime.now()
    )
}

/**
 * 토큰 타입
 */
enum class TokenType {
    ACCESS, REFRESH
} 