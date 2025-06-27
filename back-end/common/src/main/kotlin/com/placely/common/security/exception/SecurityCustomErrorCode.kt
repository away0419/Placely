package com.placely.common.security.exception

/**
 * 보안 관련 커스텀 에러 코드
 */
enum class SecurityCustomErrorCode(
    val code: String,
    val message: String
) {
    JWT_TOKEN_TYPE_MISMATCH("JWT_001", "토큰 타입이 일치하지 않습니다"),
    JWT_TOKEN_MALFORMED("JWT_002", "잘못된 형식의 토큰입니다"),
    JWT_TOKEN_EXPIRED("JWT_003", "만료된 토큰입니다"),
    JWT_TAMPERED_INVALID("JWT_004", "변조되었거나 유효하지 않은 토큰입니다"),
    JWT_TOKEN_ILLEGAL_ARGUMENT("JWT_005", "토큰 인자가 올바르지 않습니다"),
    JWT_TOKEN_IS_NULL("JWT_006", "토큰이 null입니다"),
    JWT_TOKEN_NOT_FOUND("JWT_007", "토큰을 찾을 수 없습니다"),
    ACCESS_DENIED("AUTH_001", "접근이 거부되었습니다"),
    AUTHENTICATION_FAILED("AUTH_002", "인증에 실패했습니다"),
    INVALID_CREDENTIALS("AUTH_003", "잘못된 사용자 정보입니다"),
    USER_NOT_FOUND("AUTH_004", "사용자를 찾을 수 없습니다")
}