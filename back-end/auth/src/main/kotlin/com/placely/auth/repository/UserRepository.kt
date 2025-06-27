package com.placely.auth.repository

import com.placely.auth.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

/**
 * 사용자 Repository
 */
@Repository
interface UserRepository : JpaRepository<User, Long> {
    
    /**
     * 사용자명으로 사용자 조회 (삭제되지 않은 사용자만)
     */
    fun findByUsernameAndIsDeleted(username: String, isDeleted: String = "N"): Optional<User>
    
    /**
     * 이메일로 사용자 조회 (삭제되지 않은 사용자만)
     */
    fun findByEmailAndIsDeleted(email: String, isDeleted: String = "N"): Optional<User>
    
    /**
     * 사용자명 또는 이메일로 사용자 조회 (로그인용)
     */
    @Query("""
        SELECT u FROM User u 
        WHERE (u.username = :loginId OR u.email = :loginId) 
        AND u.isDeleted = 'N' 
        AND u.status = 'ACTIVE'
    """)
    fun findByUsernameOrEmailForLogin(@Param("loginId") loginId: String): Optional<User>
    
    /**
     * 마지막 로그인 시간 업데이트
     */
    @Modifying
    @Query("""
        UPDATE User u 
        SET u.lastLoginAt = :loginTime, u.updatedAt = :loginTime 
        WHERE u.userId = :userId
    """)
    fun updateLastLoginTime(
        @Param("userId") userId: Long, 
        @Param("loginTime") loginTime: LocalDateTime
    ): Int
} 