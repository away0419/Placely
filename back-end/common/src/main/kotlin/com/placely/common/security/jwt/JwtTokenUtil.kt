package com.placely.common.security.jwt

import com.placely.common.config.JwtProperties
import com.placely.common.security.exception.SecurityCustomErrorCode
import com.placely.common.security.exception.SecurityCustomException
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

/**
 * JWT 토큰 유틸리티
 */
@Component
class JwtTokenUtil(
    private val jwtProperties: JwtProperties
) {

    private val key: Key = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())

    /**
     * 헤더에서 token 추출
     * @param header String
     * @return String
     */
    fun getTokenFromHeader(header: String): String {
        // 만약 개발자가 지정한 토큰 타입이 아닌 경우 에러 발생
        if (!header.startsWith(JwtConstants.TOKEN_TYPE)) {
            throw SecurityCustomException(SecurityCustomErrorCode.JWT_TOKEN_TYPE_MISMATCH)
        }
        return header.split(" ")[1]
    }

    /**
     * token에서 claims 추출
     * @param token String
     * @return Claims
     */
    fun getClaimsFromToken(token: String): Claims {

        return try {
            Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
        } catch (e: MalformedJwtException) {
            throw SecurityCustomException(SecurityCustomErrorCode.JWT_TOKEN_MALFORMED)
        } catch (e: ExpiredJwtException) {
            throw SecurityCustomException(SecurityCustomErrorCode.JWT_TOKEN_EXPIRED)
        } catch (e: UnsupportedJwtException) {
            throw SecurityCustomException(SecurityCustomErrorCode.JWT_TAMPERED_INVALID)
        } catch (e: IllegalArgumentException) {
            throw SecurityCustomException(SecurityCustomErrorCode.JWT_TOKEN_ILLEGAL_ARGUMENT)
        } catch (e: NullPointerException) {
            throw SecurityCustomException(SecurityCustomErrorCode.JWT_TOKEN_IS_NULL)
        }
    }

    /**
     * 토큰에서 사용자 ID 추출
     * @param token String
     * @return Long
     */
    fun getUserIdFromToken(token: String): String {
        val claims = getClaimsFromToken(token)
        return claims.subject
    }

    /**
     * 토큰에서 사용자 역할 추출
     * @param token String
     * @return String?
     */
    fun getUserRoleFromToken(token: String): String? {
        val claims = getClaimsFromToken(token)
        return claims["role"] as? String
    }

    /**
     * 토큰 타입 확인 (ACCESS/REFRESH)
     * @param token String
     * @return String?
     */
    fun getTokenType(token: String): String? {
        val claims = getClaimsFromToken(token)
        return claims["type"] as? String
    }

    /**
     * 토큰 유효성 검증
     * @param token String
     * @return true: 통과, false: 실패
     */
    fun validateToken(token: String): Boolean {
        try {
            getClaimsFromToken(token)
            return true
        } catch (ex: Exception) {
            return false
        }
    }

    /**
     * refresh token 남은 유효 기간이 7일 이하 판단.
     * @param refreshToken String
     * @return true: 7일 이하, false: 7일 초과
     */
    fun isNeedToUpdateRefreshToken(refreshToken: String): Boolean {
        val claims = getClaimsFromToken(refreshToken)
        val expiresAt = claims.expiration ?: return true  // null이면 만료 간주

        val now = Date()
        val thresholdDate = Date(now.time + 7 * 24 * 60 * 60 * 1000L)  // 7일 후 시점

        return expiresAt.before(thresholdDate)
    }

} 