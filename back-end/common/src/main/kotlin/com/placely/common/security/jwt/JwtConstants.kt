package com.placely.common.security.jwt

object JwtConstants {
    const val AUTH_HEADER = "Authorization"
    const val TOKEN_TYPE = "Bearer " // 띄어 쓰기가 있어야 한다.
    const val COOKIE_HEADER = "Set-Cookie"
    const val REFRESH_TOKEN_PREFIX = "refresh_token"
}