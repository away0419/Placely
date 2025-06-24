package com.placely.auth.exception

/**
 * 인증 관련 예외
 */
class AuthException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * 토큰 관련 예외
 */
class TokenException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * 사용자 계정 관련 예외
 */
class UserAccountException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)