package com.placely.auth.repository

import com.placely.auth.entity.AuthUser
import com.placely.auth.entity.UserType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

/**
 * AuthUser Repository
 */
@Repository
interface AuthUserRepository : JpaRepository<AuthUser, Long> {
    
    /**
     * 사용자명으로 활성 사용자 조회
     */
    fun findByUsernameAndIsDeleted(username: String, isDeleted: String = "N"): Optional<AuthUser>
    
    /**
     * 이메일로 활성 사용자 조회
     */
    fun findByEmailAndIsDeleted(email: String, isDeleted: String = "N"): Optional<AuthUser>
    
    /**
     * 사용자명 또는 이메일로 활성 사용자 조회
     */
    @Query("""
        SELECT u FROM AuthUser u 
        WHERE (u.username = :identifier OR u.email = :identifier) 
        AND u.isDeleted = 'N'
    """)
    fun findByUsernameOrEmail(@Param("identifier") identifier: String): Optional<AuthUser>
    
    /**
     * 사용자명 중복 확인
     */
    fun existsByUsernameAndIsDeleted(username: String, isDeleted: String = "N"): Boolean
    
    /**
     * 이메일 중복 확인
     */
    fun existsByEmailAndIsDeleted(email: String, isDeleted: String = "N"): Boolean
    
    /**
     * 사용자 타입별 활성 사용자 조회
     */
    fun findByUserTypeAndIsDeleted(userType: UserType, isDeleted: String = "N"): List<AuthUser>
    
    /**
     * 전화번호로 활성 사용자 조회
     */
    fun findByPhoneNumberAndIsDeleted(phoneNumber: String, isDeleted: String = "N"): Optional<AuthUser>
} 