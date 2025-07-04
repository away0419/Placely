package com.placely.auth.dto

import jakarta.validation.constraints.NotBlank

/**
 * 로그인 요청 DTO
 */
data class LoginRequest(
    @field:NotBlank(message = "사용자명은 필수입니다")
    val username: String,
    
    @field:NotBlank(message = "비밀번호는 필수입니다")
    val password: String
) 