package com.placely.auth.dto

import java.time.LocalDateTime

data class AuthUserUpdateRequest(
    var email: String, // 이메일 주소
    var phone: String?, // 연락처
    var fullName: String, // 실명
    var birthDate: LocalDateTime?, // 생년월일
    var gender: String?, // 성별 (M:남성, F:여성)
) {
}