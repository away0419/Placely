package com.placely.common.security.jwt

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

/**
 * 비밀번호 암호화 유틸리티
 */
@Component
class PasswordEncoder {
    
    private val bCryptEncoder = BCryptPasswordEncoder(12) // 강도 12 설정
    
    /**
     * 비밀번호 해싱
     */
    fun encode(rawPassword: String): String {
        return bCryptEncoder.encode(rawPassword)
    }
    
    /**
     * 비밀번호 검증
     */
    fun matches(rawPassword: String, encodedPassword: String): Boolean {
        return bCryptEncoder.matches(rawPassword, encodedPassword)
    }
    
    /**
     * 비밀번호 강도 검증
     * - 최소 8자 이상
     * - 대문자, 소문자, 숫자, 특수문자 포함
     */
    fun validatePasswordStrength(password: String): Boolean {
        if (password.length < 8) return false
        
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }
        
        return hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar
    }
    
    /**
     * 비밀번호 강도 메시지 반환
     */
    fun getPasswordStrengthMessage(password: String): String {
        val messages = mutableListOf<String>()
        
        if (password.length < 8) {
            messages.add("최소 8자 이상")
        }
        if (!password.any { it.isUpperCase() }) {
            messages.add("대문자 포함")
        }
        if (!password.any { it.isLowerCase() }) {
            messages.add("소문자 포함")
        }
        if (!password.any { it.isDigit() }) {
            messages.add("숫자 포함")
        }
        if (!password.any { !it.isLetterOrDigit() }) {
            messages.add("특수문자 포함")
        }
        
        return if (messages.isEmpty()) {
            "강력한 비밀번호입니다."
        } else {
            "비밀번호는 ${messages.joinToString(", ")}이 필요합니다."
        }
    }
} 