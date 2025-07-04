package com.placely.auth.service

import com.placely.auth.entity.TokenEntity
import com.placely.auth.repository.TokenRepository
import com.placely.common.security.jwt.JwtUtil
import com.placely.common.security.jwt.JwtType
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
@Transactional
class TokenService(
    private val tokenRepository: TokenRepository,
    private val jwtTokenUtil: JwtUtil,
) {

    /**
     * 토큰 발급 이력 DB 저장
     */
    fun saveTokenToDatabase(
        userId: Long,
        token: String,
        tokenType: JwtType,
        nowLocalDateTime: LocalDateTime
    ): TokenEntity {
        val expiresAt = when (tokenType) {
            JwtType.ACCESS -> nowLocalDateTime.plusSeconds(jwtTokenUtil.getAccessTokenExpirationSeconds())
            JwtType.REFRESH -> nowLocalDateTime.plusSeconds(jwtTokenUtil.getRefreshTokenExpirationSeconds())
        }

        val tokenEntity = TokenEntity(
            tokenId = 0, // 시퀀스에서 자동 생성
            userId = userId,
            tokenType = tokenType,
            tokenValue = token,
            expiresAt = expiresAt,
            revokedAt = null,
            createdAt = nowLocalDateTime
        )

        return tokenRepository.save(tokenEntity)
    }

//    /**
//     * 토큰 유효성 검증
//     */
//    fun validateToken(token: String): Boolean {
//        try {
//
//            // 2. JWT 토큰 유효성 검증
//            if (!jwtTokenUtil.isTokenValid(token)) {
//                log.warn { "유효하지 않은 JWT 토큰입니다" }
//                return false
//            }
//
//            // 3. Redis에서 토큰 존재 여부 확인
//            if (!tokenRedisService.isTokenExists(token)) {
//                log.warn { "Redis에 존재하지 않는 토큰입니다" }
//                return false
//            }
//
//            return true
//        } catch (e: Exception) {
//            log.error(e) { "토큰 검증 중 오류 발생" }
//            return false
//        }
//    }
}