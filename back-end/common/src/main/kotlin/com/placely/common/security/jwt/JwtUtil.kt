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
class JwtUtil(
    private val jwtProperties: JwtProperties
) {

    private val key: Key = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())

    // =========================== token 추출 관련 start ===========================

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
    fun getUserIdFromToken(token: String): Long {
        val claims = getClaimsFromToken(token)
        return claims.subject.toLong()
    }

    /**
     * 토큰에서 사용자 역할 추출
     * @param token String
     * @return String?
     */
    fun getUserRoleFromToken(token: String): List<String> {
        val claims = getClaimsFromToken(token)
        return claims["roles"] as? List<String> ?: emptyList()
    }

    /**
     * 토큰 타입 확인 (ACCESS/REFRESH)
     * @param token String
     * @return String?
     */
    fun getTokenType(token: String): String {
        val claims = getClaimsFromToken(token)
        return claims["type"] as? String ?: ""
    }

    // =========================== token 추출 관련 end ===========================
    // =========================== token 검증 관련 start ===========================

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

    /**
     * Access 토큰 유효 시간 (초)
     * @return Long
     */
    fun getAccessTokenExpirationSeconds(): Long {
        return jwtProperties.accessTokenExpiration / 1000
    }

    /**
     * Refresh 토큰 유효 시간 (초)
     * @return Long
     */
    fun getRefreshTokenExpirationSeconds(): Long {
        return jwtProperties.refreshTokenExpiration / 1000
    }

    // =========================== token 검증 관련 end ===========================
    // =========================== token 생성 관련 start ===========================

    /**
     * Access 토큰 생성 (role 포함)
     * @param userId String 사용자 ID
     * @param roleList List<String> 사용자 역할
     * @param nowDate Date 현재 시간 (밀리초)
     * @return String JWT 토큰
     */
    fun generateAccessToken(userId: String, roleList: List<String>, nowDate: Date): String {
        val claims = mapOf(
            "type" to JwtType.ACCESS.name,
            "iss" to jwtProperties.issuer,
            "roles" to roleList
        )

        return generateToken(userId, claims, nowDate, jwtProperties.accessTokenExpiration)
    }

    /**
     * Refresh 토큰 생성
     * @param userId String 사용자 ID
     * @param nowDate Date 현재 시간 (UTC 시간)
     * @return String JWT 토큰
     */
    fun generateRefreshToken(userId: String, nowDate: Date): String {
        val claims = mapOf(
            "type" to JwtType.REFRESH.name,
            "iss" to jwtProperties.issuer
        )

        return generateToken(userId, claims, nowDate, jwtProperties.refreshTokenExpiration)
    }

    /**
     *
     * @param subject String 주체 (사용자 ID)
     * @param extraClaims Map<String, Any> 추가 클레임
     * @param nowDate Date 현재 시간
     * @param expiration Long 만료 시간 (밀리초)
     * @return String JWT 토큰
     */
    private fun generateToken(
        subject: String,
        extraClaims: Map<String, Any>,
        nowDate: Date,
        expiration: Long
    ): String {
        val expiryDate = Date(nowDate.time + expiration)

        return Jwts.builder()
            .claims(extraClaims)
            .subject(subject)
            .issuedAt(nowDate)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

} 