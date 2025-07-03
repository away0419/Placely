package com.placely.auth.util

import com.placely.common.security.exception.SecurityCustomErrorCode
import com.placely.common.security.exception.SecurityCustomException
import com.placely.common.security.jwt.TokenType
import jakarta.servlet.http.Cookie
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component

@Component
class RefreshCookieUtil {
    /**
     * cookie 생성
     * @param refreshToken String
     * @return ResponseCookie
     */
    fun generateRefreshTokenCookie(refreshToken: String): ResponseCookie {
        return ResponseCookie.from(TokenType.REFRESH.name, refreshToken)
            .httpOnly(true)
            .sameSite("None")
            .path("/")
            .maxAge(60 * 60 * 24 * 30) // 30일
            .build()
    }

    /**
     * cookie에서 refresh token 추출
     * @param cookies Array<Cookie>
     * @return String
     */
    fun getRefreshToken(cookies: Array<Cookie>): String {
        return cookies.firstOrNull { it.name == TokenType.REFRESH.name }?.value
            ?: throw SecurityCustomException(SecurityCustomErrorCode.JWT_TOKEN_NOT_FOUND)
    }
}