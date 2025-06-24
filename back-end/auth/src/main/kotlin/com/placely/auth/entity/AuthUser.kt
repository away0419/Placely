package com.placely.auth.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 사용자 인증 정보 Entity
 */
@Entity
@Table(name = "AUTH_USERS")
class AuthUser(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auth_user_seq")
    @SequenceGenerator(name = "auth_user_seq", sequenceName = "SEQ_AUTH_USERS", allocationSize = 1)
    @Column(name = "USER_ID")
    val userId: Long = 0,
    
    @Column(name = "USERNAME", unique = true, nullable = false, length = 50)
    var username: String,
    
    @Column(name = "EMAIL", unique = true, nullable = false, length = 100)
    var email: String,
    
    @Column(name = "PASSWORD_HASH", nullable = false, length = 255)
    var passwordHash: String,
    
    @Column(name = "PHONE_NUMBER", length = 20)
    var phoneNumber: String? = null,
    
    @Column(name = "FULL_NAME", length = 100)
    var fullName: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "USER_TYPE", nullable = false, length = 20)
    var userType: UserType,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    var status: UserStatus = UserStatus.ACTIVE,
    
    @Column(name = "LAST_LOGIN_AT")
    var lastLoginAt: LocalDateTime? = null,
    
    @Column(name = "PASSWORD_CHANGED_AT")
    var passwordChangedAt: LocalDateTime? = null,
    
    @Column(name = "FAILED_LOGIN_COUNT")
    var failedLoginCount: Int = 0,
    
    @Column(name = "LOCKED_UNTIL")
    var lockedUntil: LocalDateTime? = null,
    
    // 감사 컬럼
    @Column(name = "CREATED_AT", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "CREATED_BY")
    var createdBy: Long? = null,
    
    @Column(name = "UPDATED_AT")
    var updatedAt: LocalDateTime? = null,
    
    @Column(name = "UPDATED_BY")
    var updatedBy: Long? = null,
    
    @Column(name = "IS_DELETED", nullable = false, length = 1)
    var isDeleted: String = "N",
    
    @Column(name = "DELETED_AT")
    var deletedAt: LocalDateTime? = null,
    
    @Column(name = "DELETED_BY")
    var deletedBy: Long? = null
) {
    
    /**
     * 비밀번호 변경 시 호출
     */
    fun changePassword(newPasswordHash: String) {
        this.passwordHash = newPasswordHash
        this.passwordChangedAt = LocalDateTime.now()
        this.failedLoginCount = 0 // 비밀번호 변경 시 실패 카운트 초기화
        this.lockedUntil = null // 잠금 해제
    }
    
    /**
     * 로그인 성공 시 호출
     */
    fun loginSuccess() {
        this.lastLoginAt = LocalDateTime.now()
        this.failedLoginCount = 0
        this.lockedUntil = null
    }
    
    /**
     * 로그인 실패 시 호출
     */
    fun loginFailed() {
        this.failedLoginCount++
        if (this.failedLoginCount >= 5) {
            // 5회 실패 시 30분 잠금
            this.lockedUntil = LocalDateTime.now().plusMinutes(30)
        }
    }
    
    /**
     * 계정 잠금 여부 확인
     */
    fun isLocked(): Boolean {
        return lockedUntil?.isAfter(LocalDateTime.now()) == true
    }
    
    /**
     * 활성 계정 여부 확인
     */
    fun isActive(): Boolean {
        return status == UserStatus.ACTIVE && isDeleted == "N"
    }
}

/**
 * 사용자 타입 열거형
 */
enum class UserType {
    OWNER,      // 점주
    EMPLOYEE,   // 직원
    CUSTOMER,   // 고객
    ADMIN       // 관리자
}

/**
 * 사용자 상태 열거형
 */
enum class UserStatus {
    ACTIVE,     // 활성
    INACTIVE,   // 비활성
    SUSPENDED,  // 정지
    PENDING     // 승인 대기
} 