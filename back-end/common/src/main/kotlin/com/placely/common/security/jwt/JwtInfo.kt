package com.placely.common.security.jwt

data class JwtInfo(
    val token: String,
    val clientIp: String? = null,
    val clientAgent: String? = null
)
