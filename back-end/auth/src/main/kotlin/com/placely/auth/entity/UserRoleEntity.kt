package com.placely.auth.entity

import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime

/**
 * 사용자-역할 매핑 엔티티 (AUTH_USER_ROLES 테이블)
 */
@Entity
@Table(name = "AUTH_USER_ROLES")
@IdClass(UserRoleId::class)
data class UserRoleEntity(
    @Id
    @Column(name = "USER_ID")
    val userId: Long = 0,

    @Id
    @Column(name = "ROLE_ID")
    val roleId: Long = 0,

    @Column(name = "GRANTED_AT")
    val grantedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "GRANTED_BY")
    val grantedBy: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", insertable = false, updatable = false)
    val user: UserEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROLE_ID", insertable = false, updatable = false)
    val role: RoleEntity? = null
) {
    // JPA를 위한 기본 생성자
    constructor() : this(
        userId = 0,
        roleId = 0,
        grantedAt = LocalDateTime.now(),
        grantedBy = null,
        user = null,
        role = null
    )
}

/**
 * 복합키 클래스
 */
data class UserRoleId(
    val userId: Long = 0,
    val roleId: Long = 0
) : Serializable