package com.placely.auth.service

import com.placely.auth.dto.LoginRequest
import com.placely.auth.dto.LoginResponse
import com.placely.auth.dto.UserInfo
import com.placely.auth.repository.TokenRepository
import com.placely.auth.repository.UserRepository
import com.placely.common.redis.RedisUtil
import com.placely.common.security.crypto.CryptoUtil
import com.placely.common.security.jwt.JwtType
import com.placely.common.security.jwt.JwtUtil
import com.placely.common.security.jwt.JwtInfo
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

private val log = KotlinLogging.logger {}

/**
 * 인증 서비스
 */
@Service
@Transactional
class AuthService(
    private val userRepository: UserRepository,
    private val tokenRepository: TokenRepository,
    private val jwtUtil: JwtUtil,
    private val redisUtil: RedisUtil,
    private val tokenService: TokenService,
    private val cryptoUtil: CryptoUtil,
) {

    /**
     * 로그인 처리
     */
    fun login(request: LoginRequest): LoginResponse {

        val hashing = cryptoUtil.hashing(request.password)
        log.debug { "hashing: $hashing" }

        // 1. 사용자 조회 (사용자명 또는 이메일로)
        val user = userRepository.findByUsernameOrEmailForLogin(request.username)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다") }

        // 2. 현재 시간
        val nowDate = Date()
        val nowInstant = Instant.ofEpochMilli(nowDate.time)
        val nowLocalDateTime = LocalDateTime.ofInstant(nowInstant, ZoneId.systemDefault())

        // 3. JWT 토큰 생성
        val accessToken = jwtUtil.generateAccessToken(user.userId.toString(), nowDate)
        val refreshToken = jwtUtil.generateRefreshToken(user.userId.toString(), nowDate)

        // 4. 토큰 DB 저장
        tokenService.saveTokenToDatabase(user.userId, accessToken, JwtType.ACCESS, nowLocalDateTime)
        val refreshTokenEntity =
            tokenService.saveTokenToDatabase(user.userId, refreshToken, JwtType.REFRESH, nowLocalDateTime)

        // 5. Redis에 토큰 정보 저장
        val tokenInfo = JwtInfo(refreshToken, "ip", "agent")
        redisUtil.save(JwtType.REFRESH.name + refreshToken, tokenInfo, refreshTokenEntity.expiresAt)

        // 6. 마지막 로그인 시간 업데이트
        userRepository.updateLastLoginTime(user.userId, nowLocalDateTime)

        // 7. 응답 생성
        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtUtil.getAccessTokenExpirationSeconds(),
            user = UserInfo(
                userId = user.userId,
                username = user.username,
                email = user.email,
                fullName = user.fullName,
                phone = user.phone
            )
        )
    }

    /**
     * 로그아웃 처리
     */
    fun logout(refreshToken: String) {
        val userId = jwtUtil.getUserIdFromToken(refreshToken)

        // 1. Redis 정보 삭제
        redisUtil.delete(JwtType.REFRESH.name + refreshToken)


        // 2. 사용자의 모든 토큰 무효화 (DB 처리)
        tokenRepository.revokeAllTokensByUserId(userId)
    }

}