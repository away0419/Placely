package com.placely.common.security.service

import com.placely.common.security.dto.TokenInfo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime

/**
 * 토큰 Redis 관리 서비스
 * - JWT 토큰을 Redis에 저장하고 관리
 * - Common 모듈의 공통 기능으로 @Component 사용
 */
@Component
class TokenRedisService @Autowired constructor(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    
    companion object {
        private const val ACCESS_TOKEN_PREFIX = "access_token:"
        private const val REFRESH_TOKEN_PREFIX = "refresh_token:"
        private const val USER_TOKEN_PREFIX = "user_token:"
        private const val BLACKLIST_PREFIX = "blacklist:"
    }
    
    /**
     * 토큰 정보를 Redis에 저장
     * @param tokenInfo 저장할 토큰 정보
     */
    fun saveTokenInfo(tokenInfo: TokenInfo) {
        val now = LocalDateTime.now()
        
        // Access Token 저장 (키: access_token:토큰값, 값: 토큰정보)
        val accessTokenKey = ACCESS_TOKEN_PREFIX + tokenInfo.accessToken
        val accessTokenTtl = Duration.between(now, tokenInfo.accessTokenExpiresAt)
        redisTemplate.opsForValue().set(accessTokenKey, tokenInfo, accessTokenTtl)
        
        // Refresh Token 저장 (키: refresh_token:토큰값, 값: 토큰정보)
        val refreshTokenKey = REFRESH_TOKEN_PREFIX + tokenInfo.refreshToken
        val refreshTokenTtl = Duration.between(now, tokenInfo.refreshTokenExpiresAt)
        redisTemplate.opsForValue().set(refreshTokenKey, tokenInfo, refreshTokenTtl)
        
        // 사용자별 토큰 저장 (키: user_token:사용자ID, 값: 토큰정보)
        val userTokenKey = USER_TOKEN_PREFIX + tokenInfo.userId
        redisTemplate.opsForValue().set(userTokenKey, tokenInfo, refreshTokenTtl)
    }
    
    /**
     * Access Token으로 토큰 정보 조회
     * @param accessToken 조회할 Access Token
     * @return 토큰 정보 또는 null
     */
    fun getTokenInfoByAccessToken(accessToken: String): TokenInfo? {
        val key = ACCESS_TOKEN_PREFIX + accessToken
        return redisTemplate.opsForValue().get(key) as? TokenInfo
    }
    
    /**
     * Refresh Token으로 토큰 정보 조회
     * @param refreshToken 조회할 Refresh Token
     * @return 토큰 정보 또는 null
     */
    fun getTokenInfoByRefreshToken(refreshToken: String): TokenInfo? {
        val key = REFRESH_TOKEN_PREFIX + refreshToken
        return redisTemplate.opsForValue().get(key) as? TokenInfo
    }
    
    /**
     * 사용자 ID로 토큰 정보 조회
     * @param userId 조회할 사용자 ID
     * @return 토큰 정보 또는 null
     */
    fun getTokenInfoByUserId(userId: String): TokenInfo? {
        val key = USER_TOKEN_PREFIX + userId
        return redisTemplate.opsForValue().get(key) as? TokenInfo
    }
    
    /**
     * 토큰 정보 삭제 (로그아웃 시 사용)
     * @param tokenInfo 삭제할 토큰 정보
     */
    fun deleteTokenInfo(tokenInfo: TokenInfo) {
        val accessTokenKey = ACCESS_TOKEN_PREFIX + tokenInfo.accessToken
        val refreshTokenKey = REFRESH_TOKEN_PREFIX + tokenInfo.refreshToken
        val userTokenKey = USER_TOKEN_PREFIX + tokenInfo.userId
        
        // 모든 토큰 정보 삭제
        redisTemplate.delete(accessTokenKey)
        redisTemplate.delete(refreshTokenKey)
        redisTemplate.delete(userTokenKey)
    }
    
    /**
     * 사용자 ID로 토큰 정보 삭제
     * @param userId 삭제할 사용자 ID
     */
    fun deleteTokenInfoByUserId(userId: String) {
        val tokenInfo = getTokenInfoByUserId(userId)
        tokenInfo?.let { deleteTokenInfo(it) }
    }
    
    /**
     * 토큰을 블랙리스트에 추가 (토큰 무효화)
     * @param token 블랙리스트에 추가할 토큰
     * @param expiration 만료 시간
     */
    fun addToBlacklist(token: String, expiration: LocalDateTime) {
        val key = BLACKLIST_PREFIX + token
        val ttl = Duration.between(LocalDateTime.now(), expiration)
        redisTemplate.opsForValue().set(key, "blacklisted", ttl)
    }
    
    /**
     * 토큰이 블랙리스트에 있는지 확인
     * @param token 확인할 토큰
     * @return 블랙리스트에 있으면 true, 없으면 false
     */
    fun isTokenBlacklisted(token: String): Boolean {
        val key = BLACKLIST_PREFIX + token
        return redisTemplate.hasKey(key)
    }
    
    /**
     * 토큰 존재 여부 확인
     * @param accessToken 확인할 Access Token
     * @return 토큰이 존재하면 true, 없으면 false
     */
    fun isTokenExists(accessToken: String): Boolean {
        val key = ACCESS_TOKEN_PREFIX + accessToken
        return redisTemplate.hasKey(key)
    }
} 