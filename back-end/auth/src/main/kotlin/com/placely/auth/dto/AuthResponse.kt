package com.placely.auth.dto

import com.placely.auth.entity.UserStatus
import com.placely.auth.entity.UserType
import java.time.LocalDateTime

/**
 * 로그인 응답 DTO
 */
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val user: UserInfo
)

/**
 * 사용자 정보 DTO
 */
data class UserInfo(
    val userId: Long,
    val username: String,
    val email: String,
    val fullName: String?,
    val phoneNumber: String?,
    val userType: UserType,
    val status: UserStatus,
    val lastLoginAt: LocalDateTime?
)

/**
 * 토큰 갱신 응답 DTO
 */
data class RefreshTokenResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long
)

/**
 * 회원가입 응답 DTO
 */
data class SignUpResponse(
    val userId: Long,
    val username: String,
    val email: String,
    val message: String = "회원가입이 완료되었습니다."
)

/**
 * API 응답 공통 포맷
 */
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun <T> success(data: T, message: String = "요청이 성공적으로 처리되었습니다."): ApiResponse<T> {
            return ApiResponse(true, message, data)
        }
        
        fun <T> success(message: String = "요청이 성공적으로 처리되었습니다."): ApiResponse<T> {
            return ApiResponse(true, message, null)
        }
        
        fun <T> error(message: String): ApiResponse<T> {
            return ApiResponse(false, message, null)
        }
    }
}

/**
 * 에러 응답 DTO
 */
data class ErrorResponse(
    val error: String,
    val message: String,
    val details: List<String>? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

/**
 * 비밀번호 강도 검증 응답 DTO
 */
data class PasswordStrengthResponse(
    val isValid: Boolean,
    val message: String,
    val score: Int // 0-100 점수
)

/**
 * 사용자 존재 여부 확인 응답 DTO
 */
data class UserExistsResponse(
    val exists: Boolean,
    val field: String, // username, email, phoneNumber
    val message: String
) 