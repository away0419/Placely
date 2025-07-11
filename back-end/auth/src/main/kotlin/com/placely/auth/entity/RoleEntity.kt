package com.placely.auth.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 역할 엔티티 (AUTH_ROLES 테이블)
 */
@Entity
@Table(name = "AUTH_ROLES")
data class RoleEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_auth_roles")
    @SequenceGenerator(name = "seq_auth_roles", sequenceName = "SEQ_AUTH_ROLES", allocationSize = 1)
    @Column(name = "ROLE_ID")
    val roleId: Long = 0,

    @Column(name = "ROLE_NAME", unique = true, nullable = false, length = 50)
    val roleName: String = "",

    @Column(name = "DESCRIPTION", length = 200)
    val description: String? = null,

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
        roleId = 0,
        roleName = "",
        description = null,
        createdAt = LocalDateTime.now(),
        createdBy = null,
        updatedAt = LocalDateTime.now(),
        updatedBy = null,
        isDeleted = "N",
        deletedAt = null,
        deletedBy = null
    )
}