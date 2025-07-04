package com.placely.common.redis

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime

/**
 * Redis 관리 서비스
 */
@Component
class RedisUtil(
    private val redisTemplate: RedisTemplate<String, Any>
) {

    /**
     * 정보 저장 (영구)
     * @param key String    키
     * @param value Any     저장할 정보
     */
    fun save(key: String, value: Any) {
        redisTemplate.opsForValue().set(key, value)
    }


    /**
     * 정보 저장 (만료)
     * @param key String                키
     * @param value Any                 저장할 정보
     * @param expireAt LocalDateTime    만료 시간
     */
    fun save(key: String, value: Any, expireAt: LocalDateTime) {
        val ttl = Duration.between(LocalDateTime.now(), expireAt)
        require(!ttl.isNegative && !ttl.isZero) {
            "만료 시간은 현재보다 이후여야 합니다."
        }
        redisTemplate.opsForValue().set(key, value, ttl)
    }

    /**
     * 정보 저장 (만료)
     * @param key String                키
     * @param value Any                 저장할 정보
     * @param ttl Duration              만료까지 남은 시간
     */
    fun save(key: String, value: Any, ttl: Duration) {
        require(!ttl.isNegative && !ttl.isZero) {
            "만료까지 남은 시간이 유효해야 합니다."
        }
        redisTemplate.opsForValue().set(key, value, ttl)
    }

    /**
     * 정보 조회
     * @param key String    키
     * @return Any?         정보
     */
    fun select(key: String): Any? {
        return redisTemplate.opsForValue().get(key)
    }

    /**
     * 정보 삭제
     * @param key String    키
     */
    fun delete(key: String) {
        redisTemplate.delete(key)
    }

    /**
     * 키 존재 여부 확인
     * @param key String    키
     * @return Boolean      존재 여부
     */
    fun isExists(key: String): Boolean {
        return redisTemplate.hasKey(key)
    }
} 