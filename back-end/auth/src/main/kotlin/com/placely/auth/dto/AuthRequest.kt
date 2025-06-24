package com.placely.auth.dto

import com.placely.auth.entity.UserType
import jakarta.validation.constraints.*

/**
 * 로그인 요청 DTO
 */
data class LoginRequest(
    @field:NotBlank(message = "사용자명 또는 이메일을 입력해주세요")
    val identifier: String, // 사용자명 또는 이메일
    
    @field:NotBlank(message = "비밀번호를 입력해주세요")
    val password: String
)

/**
 * 회원가입 요청 DTO
 */
data class SignUpRequest(
    @field:NotBlank(message = "사용자명을 입력해주세요")
    @field:Size(min = 3, max = 50, message = "사용자명은 3-50자 사이여야 합니다")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9_]+$",
        message = "사용자명은 영문, 숫자, 언더스코어만 사용 가능합니다"
    )
    val username: String,
    
    @field:NotBlank(message = "이메일을 입력해주세요")
    @field:Email(message = "올바른 이메일 형식을 입력해주세요")
    @field:Size(max = 100, message = "이메일은 100자 이하여야 합니다")
    val email: String,
    
    @field:NotBlank(message = "비밀번호를 입력해주세요")
    @field:Size(min = 8, max = 100, message = "비밀번호는 8-100자 사이여야 합니다")
    val password: String,
    
    @field:Pattern(
        regexp = "^[0-9]{2,3}-[0-9]{3,4}-[0-9]{4}$",
        message = "올바른 전화번호 형식을 입력해주세요 (예: 010-1234-5678)"
    )
    val phoneNumber: String? = null,
    
    @field:Size(max = 100, message = "이름은 100자 이하여야 합니다")
    val fullName: String? = null,
    
    @field:NotNull(message = "사용자 타입을 선택해주세요")
    val userType: UserType
)

/**
 * 비밀번호 변경 요청 DTO
 */
data class ChangePasswordRequest(
    @field:NotBlank(message = "현재 비밀번호를 입력해주세요")
    val currentPassword: String,
    
    @field:NotBlank(message = "새 비밀번호를 입력해주세요")
    @field:Size(min = 8, max = 100, message = "비밀번호는 8-100자 사이여야 합니다")
    val newPassword: String,
    
    @field:NotBlank(message = "새 비밀번호 확인을 입력해주세요")
    val confirmPassword: String
) {
    /**
     * 새 비밀번호 확인 검증
     */
    fun isPasswordConfirmed(): Boolean {
        return newPassword == confirmPassword
    }
}

/**
 * 토큰 갱신 요청 DTO
 */
data class RefreshTokenRequest(
    @field:NotBlank(message = "리프레시 토큰을 입력해주세요")
    val refreshToken: String
)

/**
 * 비밀번호 재설정 요청 DTO
 */
data class ResetPasswordRequest(
    @field:NotBlank(message = "이메일을 입력해주세요")
    @field:Email(message = "올바른 이메일 형식을 입력해주세요")
    val email: String
)

/**
 * 새 비밀번호 설정 요청 DTO
 */
data class SetNewPasswordRequest(
    @field:NotBlank(message = "재설정 토큰을 입력해주세요")
    val resetToken: String,
    
    @field:NotBlank(message = "새 비밀번호를 입력해주세요")
    @field:Size(min = 8, max = 100, message = "비밀번호는 8-100자 사이여야 합니다")
    val newPassword: String,
    
    @field:NotBlank(message = "새 비밀번호 확인을 입력해주세요")
    val confirmPassword: String
) {
    /**
     * 새 비밀번호 확인 검증
     */
    fun isPasswordConfirmed(): Boolean {
        return newPassword == confirmPassword
    }
} 