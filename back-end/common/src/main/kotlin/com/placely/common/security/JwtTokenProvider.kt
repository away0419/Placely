package com.placely.common.security

import com.placely.common.config.JwtProperties
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

/**
 * JWT 토큰 생성 및 검증 유틸리티
 */
@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties
) {
    
    private val key: Key = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
    
    /**
     * Access Token 생성
     */
    fun generateAccessToken(userId: Long, userRole: String): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtProperties.accessTokenExpiration)
        
        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("role", userRole)
            .claim("type", "ACCESS")
            .setIssuer(jwtProperties.issuer)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }
    
    /**
     * Refresh Token 생성
     */
    fun generateRefreshToken(userId: Long): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtProperties.refreshTokenExpiration)
        
        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("type", "REFRESH")
            .setIssuer(jwtProperties.issuer)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }
    
    /**
     * 토큰에서 사용자 ID 추출
     */
    fun getUserIdFromToken(token: String): Long {
        val claims = getClaimsFromToken(token)
        return claims.subject.toLong()
    }
    
    /**
     * 토큰에서 사용자 역할 추출
     */
    fun getUserRoleFromToken(token: String): String? {
        val claims = getClaimsFromToken(token)
        return claims["role"] as? String
    }
    
    /**
     * 토큰 타입 확인 (ACCESS/REFRESH)
     */
    fun getTokenType(token: String): String? {
        val claims = getClaimsFromToken(token)
        return claims["type"] as? String
    }
    
    /**
     * 토큰 유효성 검증
     */
    fun validateToken(token: String): Boolean {
        try {
            getClaimsFromToken(token)
            return true
        } catch (ex: JwtException) {
            return false
        } catch (ex: IllegalArgumentException) {
            return false
        }
    }
    
    /**
     * 토큰 만료 여부 확인
     */
    fun isTokenExpired(token: String): Boolean {
        return try {
            val claims = getClaimsFromToken(token)
            claims.expiration.before(Date())
        } catch (ex: Exception) {
            true
        }
    }
    
    /**
     * 토큰에서 Claims 추출
     */
    private fun getClaimsFromToken(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
    }
} 