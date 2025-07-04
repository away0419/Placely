package com.placely.auth.repository

import com.placely.auth.entity.TokenEntity
import com.placely.common.security.jwt.JwtType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

/**
 * 토큰 Repository
 */
@Repository
interface TokenRepository : JpaRepository<TokenEntity, Long> {

    /**
     * 토큰 해시로 토큰 조회 (만료되지 않은 토큰만)
     */
    @Query(
        """
        SELECT t FROM TokenEntity t 
        WHERE t.tokenValue = :tokenValue 
        AND t.expiresAt > :currentTime 
        AND t.revokedAt IS NULL
    """
    )
    fun findValidTokenByHash(
        @Param("tokenValue") tokenValue: String,
        @Param("currentTime") currentTime: LocalDateTime = LocalDateTime.now()
    ): Optional<TokenEntity>

    /**
     * 사용자의 특정 타입 토큰 조회 (유효한 토큰만)
     */
    @Query(
        """
        SELECT t FROM TokenEntity t 
        WHERE t.userId = :userId 
        AND t.tokenType = :tokenType 
        AND t.expiresAt > :currentTime 
        AND t.revokedAt IS NULL
    """
    )
    fun findValidTokensByUserIdAndType(
        @Param("userId") userId: Long,
        @Param("tokenType") tokenType: JwtType,
        @Param("currentTime") currentTime: LocalDateTime = LocalDateTime.now()
    ): List<TokenEntity>

    /**
     * 사용자의 모든 토큰 무효화 (로그아웃용)
     */
    @Modifying
    @Query(
        """
        UPDATE TokenEntity t 
        SET t.revokedAt = :revokedAt 
        WHERE t.userId = :userId 
        AND t.revokedAt IS NULL
    """
    )
    fun revokeAllTokensByUserId(
        @Param("userId") userId: Long,
        @Param("revokedAt") revokedAt: LocalDateTime = LocalDateTime.now()
    ): Int

    /**
     * 특정 토큰 값으로 토큰 무효화
     */
    @Modifying
    @Query(
        """
        UPDATE TokenEntity t 
        SET t.revokedAt = :revokedAt 
        WHERE t.tokenValue = :tokenValue 
        AND t.revokedAt IS NULL
    """
    )
    fun revokeTokenByValue(
        @Param("tokenValue") tokenValue: String,
        @Param("revokedAt") revokedAt: LocalDateTime = LocalDateTime.now()
    ): Int

    /**
     * 만료된 토큰 삭제 (정리용)
     */
    @Modifying
    @Query(
        """
        DELETE FROM TokenEntity t 
        WHERE t.expiresAt < :currentTime
    """
    )
    fun deleteExpiredTokens(@Param("currentTime") currentTime: LocalDateTime): Int
} 