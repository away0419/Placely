package com.placely.common.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

/**
 * JWT 토큰 관련 설정 Properties
 */
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties @ConstructorBinding constructor(
    val secret: String = "placely-default-secret-key-for-development-only-change-in-production",
    val accessTokenExpiration: Long = 1800000, // 30분 (밀리초)
    val refreshTokenExpiration: Long = 604800000, // 7일 (밀리초)
    val issuer: String = "placely-auth-service"
) 