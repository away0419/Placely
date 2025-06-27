package com.placely.auth.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 사용자 엔티티 (AUTH_USERS 테이블)
 */
@Entity
@Table(name = "AUTH_USERS")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_auth_users")
    @SequenceGenerator(name = "seq_auth_users", sequenceName = "SEQ_AUTH_USERS", allocationSize = 1)
    @Column(name = "USER_ID")
    val userId: Long = 0,

    @Column(name = "USERNAME", unique = true, nullable = false, length = 50)
    val username: String = "",

    @Column(name = "EMAIL", unique = true, nullable = false, length = 100)
    val email: String = "",

    @Column(name = "PASSWORD_HASH", nullable = false, length = 255)
    val passwordHash: String = "",

    @Column(name = "PHONE", length = 20)
    val phone: String? = null,

    @Column(name = "FULL_NAME", nullable = false, length = 100)
    val fullName: String = "",

    @Column(name = "BIRTH_DATE")
    val birthDate: LocalDateTime? = null,

    @Column(name = "GENDER", length = 1)
    @Enumerated(EnumType.STRING)
    val gender: Gender? = null,

    @Column(name = "STATUS", length = 20)
    @Enumerated(EnumType.STRING)
    val status: UserStatus = UserStatus.ACTIVE,

    @Column(name = "LAST_LOGIN_AT")
    val lastLoginAt: LocalDateTime? = null,

    @Column(name = "CREATED_AT")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "CREATED_BY")
    val createdBy: Long? = null,

    @Column(name = "UPDATED_AT")
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "UPDATED_BY")
    val updatedBy: Long? = null,

    @Column(name = "IS_DELETED", length = 1)
    val isDeleted: String = "N",

    @Column(name = "DELETED_AT")
    val deletedAt: LocalDateTime? = null,

    @Column(name = "DELETED_BY")
    val deletedBy: Long? = null
) {
    // JPA를 위한 기본 생성자
    constructor() : this(
        userId = 0,
        username = "",
        email = "",
        passwordHash = "",
        phone = null,
        fullName = "",
        birthDate = null,
        gender = null,
        status = UserStatus.ACTIVE,
        lastLoginAt = null,
        createdAt = LocalDateTime.now(),
        createdBy = null,
        updatedAt = LocalDateTime.now(),
        updatedBy = null,
        isDeleted = "N",
        deletedAt = null,
        deletedBy = null
    )
}

/**
 * 사용자 성별
 */
enum class Gender {
    M, F
}

/**
 * 사용자 상태
 */
enum class UserStatus {
    ACTIVE, INACTIVE, SUSPENDED
} 