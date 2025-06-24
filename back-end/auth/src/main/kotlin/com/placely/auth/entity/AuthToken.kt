package com.placely.auth.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * JWT 토큰 관리 Entity
 */
@Entity
@Table(name = "AUTH_TOKENS")
class AuthToken(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auth_token_seq")
    @SequenceGenerator(name = "auth_token_seq", sequenceName = "SEQ_AUTH_TOKENS", allocationSize = 1)
    @Column(name = "TOKEN_ID")
    val tokenId: Long = 0,
    
    @Column(name = "USER_ID", nullable = false)
    var userId: Long,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "TOKEN_TYPE", nullable = false, length = 20)
    var tokenType: TokenType,
    
    @Column(name = "TOKEN_HASH", nullable = false, length = 255)
    var tokenHash: String,
    
    @Column(name = "EXPIRES_AT", nullable = false)
    var expiresAt: LocalDateTime,
    
    @Column(name = "REVOKED_AT")
    var revokedAt: LocalDateTime? = null,
    
    @Column(name = "CREATED_AT", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {
    
    /**
     * 토큰 폐기
     */
    fun revoke() {
        this.revokedAt = LocalDateTime.now()
    }
    
    /**
     * 토큰 유효성 확인
     */
    fun isValid(): Boolean {
        return revokedAt == null && expiresAt.isAfter(LocalDateTime.now())
    }
    
    /**
     * 토큰 만료 여부 확인
     */
    fun isExpired(): Boolean {
        return expiresAt.isBefore(LocalDateTime.now())
    }
}

/**
 * 토큰 타입 열거형
 */
enum class TokenType {
    ACCESS,     // 액세스 토큰
    REFRESH     // 리프레시 토큰
} 