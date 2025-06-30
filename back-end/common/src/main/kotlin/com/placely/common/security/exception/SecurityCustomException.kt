package com.placely.common.security.exception

/**
 * 보안 관련 커스텀 예외
 */
class SecurityCustomException(
    private val errorCode: SecurityCustomErrorCode,
    message: String? = null
) : RuntimeException(message ?: errorCode.message) {

    constructor(errorCode: SecurityCustomErrorCode) : this(errorCode, null)

    override fun toString(): String {
        return "SecurityCustomException(errorCode=${errorCode.code}, message=${this.message})"
    }
}