package com.placely.auth.service

import com.placely.auth.dto.LoginRequest
import com.placely.auth.dto.LoginResponse
import com.placely.auth.dto.UserInfo
import com.placely.auth.repository.TokenRepository
import com.placely.auth.repository.UserRepository
import com.placely.common.security.jwt.JwtTokenUtil
import com.placely.common.security.jwt.TokenType
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
    private val jwtTokenUtil: JwtTokenUtil,
    private val tokenService: TokenService,
) {

    /**
     * 로그인 처리
     */
    fun login(request: LoginRequest): LoginResponse {
        log.info { "로그인 기능 시작" }

        // 1. 사용자 조회 (사용자명 또는 이메일로)
        val user = userRepository.findByUsernameOrEmailForLogin(request.username)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다") }

        // 2. 현재 시간
        val nowDate = Date()
        val nowInstant = Instant.ofEpochMilli(nowDate.time)
        val nowLocalDateTime = LocalDateTime.ofInstant(nowInstant, ZoneId.systemDefault())

        // 3. JWT 토큰 생성
        val accessToken = jwtTokenUtil.generateAccessToken(user.userId.toString(), nowDate)
        val refreshToken = jwtTokenUtil.generateRefreshToken(user.userId.toString(), nowDate)

        // 4. 토큰 DB 저장
        tokenService.saveTokenToDatabase(user.userId, accessToken, TokenType.ACCESS, nowLocalDateTime)
        tokenService.saveTokenToDatabase(user.userId, refreshToken, TokenType.REFRESH, nowLocalDateTime)

        // 5. 마지막 로그인 시간 업데이트
        userRepository.updateLastLoginTime(user.userId, nowLocalDateTime)

        // 6. 응답 생성
        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtTokenUtil.getAccessTokenExpirationSeconds(),
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
    fun logout(userId: Long) {
        // 사용자의 모든 토큰 무효화
        tokenRepository.revokeAllTokensByUserId(userId)
    }

}