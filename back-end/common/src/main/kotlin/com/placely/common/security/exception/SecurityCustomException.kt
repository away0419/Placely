package com.placely.common.security.exception

class SecurityCustomException(
    private val securityCustomErrorCode: SecurityCustomErrorCode
) : RuntimeException(securityCustomErrorCode.msg)