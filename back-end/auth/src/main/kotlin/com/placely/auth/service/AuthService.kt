//package com.placely.auth.service
//
//import com.placely.auth.dto.LoginRequest
//import com.placely.auth.dto.LoginResponse
//import com.placely.auth.dto.UserInfo
//import com.placely.auth.entity.Token
//import com.placely.auth.entity.TokenType
//import com.placely.auth.repository.TokenRepository
//import com.placely.auth.repository.UserRepository
//import com.placely.auth.util.RefreshCookieUtil
//import com.placely.common.security.jwt.JwtTokenUtil
//import org.springframework.stereotype.Service
//import org.springframework.transaction.annotation.Transactional
//import java.time.LocalDateTime
//
///**
// * 인증 서비스
// */
//@Service
//@Transactional
//class AuthService(
//    private val userRepository: UserRepository,
//    private val tokenRepository: TokenRepository,
//    private val jwtTokenUtil: JwtTokenUtil,
//    private val refreshCookieUtil: RefreshCookieUtil,
//) {
//
//    /**
//     * 로그인 처리
//     */
//    fun login(request: LoginRequest): LoginResponse {
//        // 1. 사용자 조회 (사용자명 또는 이메일로)
//        val user = userRepository.findByUsernameOrEmailForLogin(request.username)
//            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다") }
//
//        // 3. JWT 토큰 생성
//        val accessToken = jwtTokenUtil.generateAccessToken(user.userId.toString())
//        val refreshToken = jwtTokenUtil.generateRefreshToken(user.userId.toString())
//
//        // 4. 토큰 DB 저장
//        saveTokenToDatabase(user.userId, accessToken, TokenType.ACCESS)
//        saveTokenToDatabase(user.userId, refreshToken, TokenType.REFRESH)
//
//        // 5. 마지막 로그인 시간 업데이트
//        userRepository.updateLastLoginTime(user.userId, LocalDateTime.now())
//
//        // 6. 응답 생성
//        return LoginResponse(
//            accessToken = accessToken,
//            refreshToken = refreshToken,
//            expiresIn = jwtTokenUtil.getAccessTokenExpirationSeconds(),
//            user = UserInfo(
//                userId = user.userId,
//                username = user.username,
//                email = user.email,
//                fullName = user.fullName,
//                phone = user.phone
//            )
//        )
//    }
//
//    /**
//     * 로그아웃 처리
//     */
//    fun logout(userId: Long) {
//        // 사용자의 모든 토큰 무효화
//        tokenRepository.revokeAllTokensByUserId(userId)
//    }
//
//    /**
//     * 토큰 유효성 검증
//     */
//    fun validateToken(token: String): Boolean {
//        return try {
//            // JWT 토큰 유효성 검증
//            val isJwtValid = jwtTokenUtil.validateToken(token)
//
//            if (!isJwtValid) {
//                return false
//            }
//
//            // DB에서 토큰 상태 확인
//            val tokenHash = generateTokenHash(token)
//            val dbToken = tokenRepository.findValidTokenByHash(tokenHash)
//
//            dbToken.isPresent
//        } catch (e: Exception) {
//            false
//        }
//    }
//
//    /**
//     * 토큰에서 사용자 ID 추출
//     */
//    fun getUserIdFromToken(token: String): Long {
//        val userIdString = jwtTokenUtil.getUserIdFromToken(token)
//        return userIdString.toLong()
//    }
//
//    /**
//     * 토큰을 데이터베이스에 저장
//     */
//    private fun saveTokenToDatabase(userId: Long, token: String, tokenType: TokenType) {
//        val expiresAt = when (tokenType) {
//            TokenType.ACCESS -> LocalDateTime.now().plusSeconds(jwtTokenUtil.getAccessTokenExpirationSeconds())
//            TokenType.REFRESH -> LocalDateTime.now().plusSeconds(jwtTokenUtil.getRefreshTokenExpirationSeconds())
//        }
//
//        val tokenEntity = Token(
//            tokenId = 0, // 시퀀스에서 자동 생성
//            userId = userId,
//            tokenType = tokenType,
//            tokenHash = tokenHash,
//            expiresAt = expiresAt,
//            revokedAt = null,
//            createdAt = LocalDateTime.now()
//        )
//
//        tokenRepository.save(tokenEntity)
//    }
//
//}