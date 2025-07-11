package com.placely.auth.service

import com.placely.auth.dto.LoginRequest
import com.placely.auth.dto.LoginResponse
import com.placely.auth.dto.UserInfo
import com.placely.auth.repository.TokenRepository
import com.placely.auth.repository.UserRepository
import com.placely.auth.repository.UserRoleRepository
import com.placely.common.redis.RedisUtil
import com.placely.common.security.crypto.CryptoUtil
import com.placely.common.security.jwt.JwtInfo
import com.placely.common.security.jwt.JwtType
import com.placely.common.security.jwt.JwtUtil
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
    private val userRoleRepository: UserRoleRepository,
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
        log.info {"fun login 시작"}

        // 1. 사용자 조회 (사용자명 또는 이메일로)
        val user = userRepository.findByUsernameOrEmailForLogin(request.username)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다") }

        // 2. 비밀번호 검증
        val isPasswordValid = cryptoUtil.verifyHash(request.password, user.passwordHash)
        if (!isPasswordValid) {
            log.warn { "비밀번호가 일치하지 않습니다. 사용자: ${user.username}" }
            throw IllegalArgumentException("비밀번호가 일치하지 않습니다")
        }

        // 3. 사용자 역할 조회
        val userRole = userRoleRepository.findRoleNamesByUserId(user.userId)

        // 3. 현재 시간
        val nowDate = Date()
        val nowInstant = Instant.ofEpochMilli(nowDate.time)
        val nowLocalDateTime = LocalDateTime.ofInstant(nowInstant, ZoneId.systemDefault())

        // 4. JWT 토큰 생성 (role 포함)
        val accessToken = jwtUtil.generateAccessToken(user.userId.toString(), userRole, nowDate)
        val refreshToken = jwtUtil.generateRefreshToken(user.userId.toString(), nowDate)

        // 5. 토큰 DB 저장
        tokenService.saveTokenToDatabase(user.userId, accessToken, JwtType.ACCESS, nowLocalDateTime)
        val refreshTokenEntity =
            tokenService.saveTokenToDatabase(user.userId, refreshToken, JwtType.REFRESH, nowLocalDateTime)

        // 6. Redis에 토큰 정보 저장
        val redisKey = JwtType.REFRESH.name + refreshToken
        val jwtInfo = JwtInfo(redisKey, "ip", "clientAgent")
        redisUtil.save(redisKey, jwtInfo, refreshTokenEntity.expiresAt)

        // 7. 마지막 로그인 시간 업데이트
        userRepository.updateLastLoginTime(user.userId, nowLocalDateTime)

        // 8. 응답 생성
        val response = LoginResponse(
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

        log.info { "로그인 성공 완료 - 사용자: ${user.username}, ID: ${user.userId}" }
        return response
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