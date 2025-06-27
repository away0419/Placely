package com.placely.auth.dto

/**
 * 로그인 응답 DTO
 */
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long, // 토큰 만료 시간 (초)
    val user: UserInfo
)

/**
 * 사용자 정보 DTO
 */
data class UserInfo(
    val userId: Long,
    val username: String,
    val email: String,
    val fullName: String,
    val phone: String? = null
) 