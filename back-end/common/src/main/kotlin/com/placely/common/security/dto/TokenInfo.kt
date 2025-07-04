package com.placely.common.security.dto

import java.time.LocalDateTime

/**
 * 토큰 정보 DTO
 * - Redis에 저장할 토큰 정보
 */
data class TokenInfo(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val userEmail: String,
    val issuedAt: LocalDateTime,
    val accessTokenExpiresAt: LocalDateTime,
    val refreshTokenExpiresAt: LocalDateTime
) 