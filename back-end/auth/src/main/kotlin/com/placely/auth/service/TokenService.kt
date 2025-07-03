package com.placely.auth.service

import com.placely.auth.entity.Token
import com.placely.auth.repository.TokenRepository
import com.placely.common.security.jwt.JwtTokenUtil
import com.placely.common.security.jwt.TokenType
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
@Transactional
class TokenService(
    private val tokenRepository: TokenRepository,
    private val jwtTokenUtil: JwtTokenUtil,
) {

    /**
     * 토큰 발급 이력 DB 저장
     */
    fun saveTokenToDatabase(
        userId: Long,
        token: String,
        tokenType: TokenType,
        nowLocalDateTime: LocalDateTime
    ) {
        val expiresAt = when (tokenType) {
            TokenType.ACCESS -> nowLocalDateTime.plusSeconds(jwtTokenUtil.getAccessTokenExpirationSeconds())
            TokenType.REFRESH -> nowLocalDateTime.plusSeconds(jwtTokenUtil.getRefreshTokenExpirationSeconds())
        }

        val tokenEntity = Token(
            tokenId = 0, // 시퀀스에서 자동 생성
            userId = userId,
            tokenType = tokenType,
            tokenValue = token,
            expiresAt = expiresAt,
            revokedAt = null,
            createdAt = nowLocalDateTime
        )

        tokenRepository.save(tokenEntity)
    }
}