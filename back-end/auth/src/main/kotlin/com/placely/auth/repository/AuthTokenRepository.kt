package com.placely.auth.repository

import com.placely.auth.entity.AuthToken
import com.placely.auth.entity.TokenType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

/**
 * AuthToken Repository
 */
@Repository
interface AuthTokenRepository : JpaRepository<AuthToken, Long> {
    
    /**
     * 토큰 해시로 유효한 토큰 조회
     */
    @Query("""
        SELECT t FROM AuthToken t 
        WHERE t.tokenHash = :tokenHash 
        AND t.revokedAt IS NULL 
        AND t.expiresAt > :now
    """)
    fun findValidTokenByHash(
        @Param("tokenHash") tokenHash: String,
        @Param("now") now: LocalDateTime = LocalDateTime.now()
    ): Optional<AuthToken>
    
    /**
     * 사용자의 특정 타입 토큰 조회
     */
    fun findByUserIdAndTokenTypeAndRevokedAtIsNull(
        userId: Long, 
        tokenType: TokenType
    ): List<AuthToken>
    
    /**
     * 사용자의 모든 유효한 토큰 조회
     */
    @Query("""
        SELECT t FROM AuthToken t 
        WHERE t.userId = :userId 
        AND t.revokedAt IS NULL 
        AND t.expiresAt > :now
    """)
    fun findValidTokensByUserId(
        @Param("userId") userId: Long,
        @Param("now") now: LocalDateTime = LocalDateTime.now()
    ): List<AuthToken>
    
    /**
     * 사용자의 모든 토큰 폐기
     */
    @Modifying
    @Query("""
        UPDATE AuthToken t 
        SET t.revokedAt = :revokedAt 
        WHERE t.userId = :userId 
        AND t.revokedAt IS NULL
    """)
    fun revokeAllTokensByUserId(
        @Param("userId") userId: Long,
        @Param("revokedAt") revokedAt: LocalDateTime = LocalDateTime.now()
    )
    
    /**
     * 만료된 토큰 삭제 (정리 작업용)
     */
    @Modifying
    @Query("""
        DELETE FROM AuthToken t 
        WHERE t.expiresAt < :expiredBefore
    """)
    fun deleteExpiredTokens(@Param("expiredBefore") expiredBefore: LocalDateTime)
    
    /**
     * 폐기된 토큰 삭제 (정리 작업용)
     */
    @Modifying
    @Query("""
        DELETE FROM AuthToken t 
        WHERE t.revokedAt IS NOT NULL 
        AND t.revokedAt < :revokedBefore
    """)
    fun deleteRevokedTokens(@Param("revokedBefore") revokedBefore: LocalDateTime)
} 