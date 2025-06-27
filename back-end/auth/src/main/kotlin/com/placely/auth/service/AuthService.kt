package com.placely.auth.service

import com.placely.auth.dto.*
import com.placely.auth.entity.*
import com.placely.auth.exception.AuthException
import com.placely.auth.repository.AuthTokenRepository
import com.placely.auth.repository.AuthUserRepository
import com.placely.common.config.JwtProperties
import com.placely.common.security.jwt.JwtTokenUtil
import com.placely.common.security.jwt.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.*

/**
 * 인증 서비스
 */
@Service
@Transactional
class AuthService(
    private val userRepository: AuthUserRepository,
    private val tokenRepository: AuthTokenRepository,
    private val jwtTokenProvider: JwtTokenUtil,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProperties: JwtProperties
) {
    
    /**
     * 로그인 처리
     */
    fun login(request: LoginRequest): LoginResponse {
        // 사용자 조회
        val user = userRepository.findByUsernameOrEmail(request.identifier)
            .orElseThrow { AuthException("사용자를 찾을 수 없습니다.") }
        
        // 계정 상태 확인
        if (!user.isActive()) {
            throw AuthException("비활성화된 계정입니다.")
        }
        
        if (user.isLocked()) {
            throw AuthException("계정이 잠겨있습니다. 잠시 후 다시 시도해주세요.")
        }
        
        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            user.loginFailed()
            userRepository.save(user)
            throw AuthException("비밀번호가 일치하지 않습니다.")
        }
        
        // 로그인 성공 처리
        user.loginSuccess()
        userRepository.save(user)
        
        // 기존 토큰 폐기
        tokenRepository.revokeAllTokensByUserId(user.userId)
        
        // 새 토큰 생성
        val accessToken = jwtTokenProvider.generateAccessToken(user.userId, user.userType.name)
        val refreshToken = jwtTokenProvider.generateRefreshToken(user.userId)
        
        // 토큰 DB에 저장
        saveTokenToDatabase(user.userId, accessToken, TokenType.ACCESS)
        saveTokenToDatabase(user.userId, refreshToken, TokenType.REFRESH)
        
        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtProperties.accessTokenExpiration / 1000,
            user = user.toUserInfo()
        )
    }
    
    /**
     * 회원가입 처리
     */
    fun signUp(request: SignUpRequest): SignUpResponse {
        // 중복 검사
        if (userRepository.existsByUsernameAndIsDeleted(request.username)) {
            throw AuthException("이미 사용 중인 사용자명입니다.")
        }
        
        if (userRepository.existsByEmailAndIsDeleted(request.email)) {
            throw AuthException("이미 사용 중인 이메일입니다.")
        }
        
        // 비밀번호 강도 검증
        if (!passwordEncoder.validatePasswordStrength(request.password)) {
            throw AuthException(passwordEncoder.getPasswordStrengthMessage(request.password))
        }
        
        // 사용자 생성
        val user = AuthUser(
            username = request.username,
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            phoneNumber = request.phoneNumber,
            fullName = request.fullName,
            userType = request.userType,
            passwordChangedAt = LocalDateTime.now()
        )
        
        val savedUser = userRepository.save(user)
        
        return SignUpResponse(
            userId = savedUser.userId,
            username = savedUser.username,
            email = savedUser.email
        )
    }
    
    /**
     * 토큰 갱신 처리
     */
    fun refreshToken(request: RefreshTokenRequest): RefreshTokenResponse {
        // 토큰 유효성 검증
        if (!jwtTokenProvider.validateToken(request.refreshToken)) {
            throw AuthException("유효하지 않은 리프레시 토큰입니다.")
        }
        
        val tokenType = jwtTokenProvider.getTokenType(request.refreshToken)
        if (tokenType != "REFRESH") {
            throw AuthException("리프레시 토큰이 아닙니다.")
        }
        
        val userId = jwtTokenProvider.getUserIdFromToken(request.refreshToken)
        val tokenHash = hashToken(request.refreshToken)
        
        // DB에서 토큰 확인
        val storedToken = tokenRepository.findValidTokenByHash(tokenHash)
            .orElseThrow { AuthException("토큰을 찾을 수 없거나 만료되었습니다.") }
        
        if (storedToken.userId != userId) {
            throw AuthException("토큰 사용자가 일치하지 않습니다.")
        }
        
        // 사용자 조회
        val user = userRepository.findById(userId)
            .orElseThrow { AuthException("사용자를 찾을 수 없습니다.") }
        
        if (!user.isActive()) {
            throw AuthException("비활성화된 계정입니다.")
        }
        
        // 새 액세스 토큰 생성
        val newAccessToken = jwtTokenProvider.generateAccessToken(user.userId, user.userType.name)
        saveTokenToDatabase(user.userId, newAccessToken, TokenType.ACCESS)
        
        return RefreshTokenResponse(
            accessToken = newAccessToken,
            expiresIn = jwtProperties.accessTokenExpiration / 1000
        )
    }
    
    /**
     * 비밀번호 변경 처리
     */
    fun changePassword(userId: Long, request: ChangePasswordRequest) {
        if (!request.isPasswordConfirmed()) {
            throw AuthException("새 비밀번호가 일치하지 않습니다.")
        }
        
        val user = userRepository.findById(userId)
            .orElseThrow { AuthException("사용자를 찾을 수 없습니다.") }
        
        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.currentPassword, user.passwordHash)) {
            throw AuthException("현재 비밀번호가 일치하지 않습니다.")
        }
        
        // 새 비밀번호 강도 검증
        if (!passwordEncoder.validatePasswordStrength(request.newPassword)) {
            throw AuthException(passwordEncoder.getPasswordStrengthMessage(request.newPassword))
        }
        
        // 비밀번호 변경
        user.changePassword(passwordEncoder.encode(request.newPassword))
        userRepository.save(user)
        
        // 모든 토큰 폐기 (재로그인 필요)
        tokenRepository.revokeAllTokensByUserId(userId)
    }
    
    /**
     * 로그아웃 처리
     */
    fun logout(userId: Long) {
        tokenRepository.revokeAllTokensByUserId(userId)
    }
    
    /**
     * 사용자 정보 조회
     */
    @Transactional(readOnly = true)
    fun getUserInfo(userId: Long): UserInfo {
        val user = userRepository.findById(userId)
            .orElseThrow { AuthException("사용자를 찾을 수 없습니다.") }
        
        return user.toUserInfo()
    }
    
    /**
     * 사용자명 중복 확인
     */
    @Transactional(readOnly = true)
    fun checkUsernameExists(username: String): UserExistsResponse {
        val exists = userRepository.existsByUsernameAndIsDeleted(username)
        return UserExistsResponse(
            exists = exists,
            field = "username",
            message = if (exists) "이미 사용 중인 사용자명입니다." else "사용 가능한 사용자명입니다."
        )
    }
    
    /**
     * 이메일 중복 확인
     */
    @Transactional(readOnly = true)
    fun checkEmailExists(email: String): UserExistsResponse {
        val exists = userRepository.existsByEmailAndIsDeleted(email)
        return UserExistsResponse(
            exists = exists,
            field = "email",
            message = if (exists) "이미 사용 중인 이메일입니다." else "사용 가능한 이메일입니다."
        )
    }
    
    /**
     * 비밀번호 강도 검증
     */
    @Transactional(readOnly = true)
    fun validatePasswordStrength(password: String): PasswordStrengthResponse {
        val isValid = passwordEncoder.validatePasswordStrength(password)
        val message = passwordEncoder.getPasswordStrengthMessage(password)
        
        // 간단한 점수 계산 (실제로는 더 복잡한 로직 필요)
        val score = when {
            password.length >= 12 && isValid -> 100
            password.length >= 10 && isValid -> 85
            password.length >= 8 && isValid -> 70
            password.length >= 8 -> 50
            else -> 20
        }
        
        return PasswordStrengthResponse(isValid, message, score)
    }
    
    /**
     * 토큰을 DB에 저장
     */
    private fun saveTokenToDatabase(userId: Long, token: String, tokenType: TokenType) {
        val tokenHash = hashToken(token)
        val expiresAt = when (tokenType) {
            TokenType.ACCESS -> LocalDateTime.now().plusSeconds(jwtProperties.accessTokenExpiration / 1000)
            TokenType.REFRESH -> LocalDateTime.now().plusSeconds(jwtProperties.refreshTokenExpiration / 1000)
        }
        
        val authToken = AuthToken(
            userId = userId,
            tokenType = tokenType,
            tokenHash = tokenHash,
            expiresAt = expiresAt
        )
        
        tokenRepository.save(authToken)
    }
    
    /**
     * 토큰 해싱 (보안을 위해 원본 토큰 대신 해시 저장)
     */
    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}

/**
 * AuthUser -> UserInfo 변환 확장 함수
 */
private fun AuthUser.toUserInfo(): UserInfo {
    return UserInfo(
        userId = this.userId,
        username = this.username,
        email = this.email,
        fullName = this.fullName,
        phoneNumber = this.phoneNumber,
        userType = this.userType,
        status = this.status,
        lastLoginAt = this.lastLoginAt
    )
} 