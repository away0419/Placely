package com.placely.common.security.jwt

/**
 * JWT 관련 상수
 */
object JwtConstants {
    const val TOKEN_TYPE = "Bearer"
    const val AUTHORIZATION_HEADER = "Authorization"
    const val TOKEN_PREFIX = "Bearer "
    const val COOKIE_HEADER = "Set-Cookie"
    const val REFRESH = "REFRESH"
    const val ACCESS = "ACCESS"
}