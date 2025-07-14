package com.placely.auth.dto

import com.placely.auth.entity.Gender
import com.placely.auth.entity.UserStatus
import java.time.LocalDateTime

/**
 * 유저 DTO
 */
data class AuthUserDTO(
    var userId: Long? = null, // 사용자 ID (Primary Key)
    var username: String? = null, // 사용자명 (로그인용)
    var email: String? = null, // 이메일 주소
    var passwordHash: String? = null, // 암호화된 비밀번호
    var phone: String? = null, // 연락처
    var fullName: String? = null, // 실명
    var birthDate: LocalDateTime? = null, // 생년월일
    var gender: Gender? = null, // 성별 (M:남성, F:여성)
    var status: UserStatus? = null, // 계정 상태 (ACTIVE:활성, INACTIVE:비활성, SUSPENDED:정지)
    var lastLoginAt: LocalDateTime? = null, // 최종 로그인 시간
    var createdAt: LocalDateTime? = null, // 생성 시간
    var createdBy: Long? = null, // 생성자 ID (Foreign Key: AUTH_USERS.USER_ID)
    var updatedAt: LocalDateTime? = null, // 수정 시간
    var updatedBy: Long? = null, // 수정자 ID (Foreign Key: AUTH_USERS.USER_ID)
    var isDeleted: String? = null, // 삭제 여부 (Y:삭제, N:미삭제)
    var deletedAt: LocalDateTime? = null, // 삭제 시간
    var deletedBy: Long? = null, // 삭제자 ID (Foreign Key: AUTH_USERS.USER_ID)
)