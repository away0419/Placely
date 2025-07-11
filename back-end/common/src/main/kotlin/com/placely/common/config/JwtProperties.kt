package com.placely.common.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * JWT 토큰 관련 설정 Properties
 * application.yml jwt 설정을 가져와 변수에 주입. 없을 경우 하드 코딩 값으로 셋팅.
 */
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secret: String = "placely-jwt-secret-key-for-development-2025-change-in-production-length-64-over-please",
    val accessTokenExpiration: Long = 1800000, // 30분 (밀리초)
    val refreshTokenExpiration: Long = 604800000, // 7일 (밀리초)
    val issuer: String = "placely-auth-service"
) 